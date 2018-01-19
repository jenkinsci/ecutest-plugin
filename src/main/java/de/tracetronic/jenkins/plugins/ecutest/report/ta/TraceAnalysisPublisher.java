/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.remoting.Callable;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import jenkins.security.MasterToSlaveCallable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.AnalysisEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.AnalysisExecutionInfo;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;

/**
 * Class holding the trace analysis configuration.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TraceAnalysisPublisher extends AbstractReportPublisher {

    /**
     * Defines the default timeout running each trace analysis.
     */
    protected static final int DEFAULT_TIMEOUT = 3600;

    /**
     * The URL name to {@link TraceAnalysisReport}s holding by {@link AbstractTraceAnalysisAction}.
     */
    protected static final String URL_NAME = "trace-analysis";

    @Nonnull
    private final String toolName;
    private boolean mergeReports = true;
    private boolean createReportDir = true;
    private String timeout = String.valueOf(getDefaultTimeout());

    /**
     * Instantiates a new {@link TraceAnalysisPublisher}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public TraceAnalysisPublisher(@Nonnull final String toolName) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
    }

    /**
     * @return the {@link ETInstallation} name
     */
    @Nonnull
    public String getToolName() {
        return toolName;
    }

    /**
     * @return whether to merge analysis job reports, defaults to {@code true}
     */
    public boolean isMergeReports() {
        return mergeReports;
    }

    /**
     * @return whether to create a new report directory, defaults to {@code true}
     */
    public boolean isCreateReportDir() {
        return createReportDir;
    }

    /**
     * Parses a string-based parameter to integer.
     *
     * @param param
     *            the parameter string
     * @return the parsed integer value represented by the String parameter,
     *         defaults to {@link #DEFAULT_TIMEOUT} if null or invalid value
     */
    public static int parse(final String param) {
        try {
            return Integer.parseInt(param);
        } catch (final NumberFormatException e) {
            return DEFAULT_TIMEOUT;
        }
    }

    /**
     * @return the timeout as integer
     */
    public int getParsedTimeout() {
        return parse(getTimeout());
    }

    /**
     * @return the timeout as string
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @return the default timeout
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @param mergeReports
     *            specifies whether to merge reports of
     *            analysis job executions into a main report.
     */
    @DataBoundSetter
    public void setMergeReports(final boolean mergeReports) {
        this.mergeReports = mergeReports;
    }

    /**
     * @param createReportDir
     *            specifies whether a new report directory is created
     *            or whether the report should be stored next to the job
     */
    @DataBoundSetter
    public void setCreateReportDir(final boolean createReportDir) {
        this.createReportDir = createReportDir;
    }

    /**
     * @param timeout
     *            the timeout running each trace analysis
     */
    @DataBoundSetter
    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    @Override
    protected void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing trace analysis...");
        ProcessUtil.checkOS(launcher);

        final Result buildResult = run.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            logger.logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return;
        }

        final List<FilePath> analysisFiles = getAnalysisFiles(run, workspace, launcher);
        if (analysisFiles.isEmpty() && !isAllowMissing()) {
            throw new ETPluginException("Empty test results are not allowed, setting build status to FAILURE!");
        }

        final List<TraceAnalysisReport> reports = new ArrayList<TraceAnalysisReport>();
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, false);
        final boolean isETRunning = !foundProcesses.isEmpty();

        // Start ECU-TEST if necessary
        if (isETRunning) {
            reports.addAll(performAnalysis(run, workspace, launcher, listener, analysisFiles));
        } else {
            // Get selected ECU-TEST installation
            final ETInstallation installation = configureToolInstallation(toolName, workspace.toComputer(), listener,
                    run.getEnvironment(listener));
            final String installPath = installation.getExecutable(launcher);
            final String workspaceDir = getWorkspaceDir(run);
            final String settingsDir = getSettingsDir(run);
            final String expandedToolName = run.getEnvironment(listener).expand(installation.getName());
            final ETClient etClient = new ETClient(expandedToolName, installPath, workspaceDir, settingsDir,
                    StartETBuilder.DEFAULT_TIMEOUT, false);
            if (etClient.start(false, workspace, launcher, listener)) {
                reports.addAll(performAnalysis(run, workspace, launcher, listener, analysisFiles));
            } else {
                logger.logError(String.format("Starting %s failed.", toolName));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", toolName));
            }
        }

        if (isArchiving()) {
            addBuildAction(run, reports);
        } else {
            logger.logInfo("Archiving trace analysis reports is disabled.");
        }

        logger.logInfo("Trace analysis reports published successfully.");
    }

    private List<TraceAnalysisReport> performAnalysis(final Run<?, ?> run, final FilePath workspace,
            final Launcher launcher,
            final TaskListener listener, final List<FilePath> analysisFiles) throws IOException, InterruptedException {
        runAnalysis(analysisFiles, launcher, listener);
        if (isMergeReports()) {
            final Map<FilePath, List<FilePath>> reportFiles = getReportFileMap(run, workspace, launcher);
            mergeReports(reportFiles, launcher, listener);
        }
        return Collections.emptyList();
    }

    private boolean runAnalysis(final List<FilePath> analysisFiles, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new TraceAnalysisCallable(analysisFiles, createReportDir, getParsedTimeout(), listener));
    }

    private boolean mergeReports(final Map<FilePath, List<FilePath>> reportFileMap, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        boolean isMerged = true;
        for (final Entry<FilePath, List<FilePath>> reportFiles : reportFileMap.entrySet()) {
            isMerged = launcher.getChannel().call(
                    new MergeReportsCallable(reportFiles.getKey(), reportFiles.getValue(), listener));
        }
        return isMerged;
    }

    private List<FilePath> getAnalysisFiles(final Run<?, ?> run,
            final FilePath workspace, final Launcher launcher) throws IOException, InterruptedException {
        final List<FilePath> analysisFiles = new ArrayList<FilePath>();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        for (final FilePath reportDir : reportDirs) {
            analysisFiles.addAll(Arrays.asList(reportDir.list("**/Job_*.ajob")));
        }
        Collections.reverse(analysisFiles);
        return analysisFiles;
    }

    private Map<FilePath, List<FilePath>> getReportFileMap(final Run<?, ?> run,
            final FilePath workspace, final Launcher launcher) throws IOException, InterruptedException {
        final Map<FilePath, List<FilePath>> reportFileMap = new LinkedHashMap<FilePath, List<FilePath>>();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        for (final FilePath reportDir : reportDirs) {
            final FilePath mainReport = getFirstReportFile(reportDir);
            final List<FilePath> reportFiles = new ArrayList<FilePath>();
            reportFiles.addAll(Arrays.asList(reportDir.list("**/Job_*.trf")));
            Collections.reverse(reportFiles);
            reportFileMap.put(mainReport, reportFiles);
        }
        return reportFileMap;
    }

    /**
     * Adds the {@link TraceAnalysisBuildAction} to the build holding the found {@link TraceAnalysisReport}s.
     *
     * @param run
     *            the run
     * @param analysisReports
     *            the list of {@link TraceAnalysisReport}s to add
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    private void addBuildAction(final Run<?, ?> run, final List<TraceAnalysisReport> analysisReports)
            throws IOException {
        TraceAnalysisBuildAction action = run.getAction(TraceAnalysisBuildAction.class);
        if (action == null) {
            action = new TraceAnalysisBuildAction(!isKeepAll());
            run.addAction(action);
        }
        action.addAll(analysisReports);
    }

    @Override
    protected String getUrlName() {
        throw new NotImplementedException();
    }

    /**
     * DescriptorImpl for {@link TraceAnalysisPublisher}.
     */
    @Symbol("publishTraceAnalysis")
    @Extension(ordinal = 10002)
    public static final class DescriptorImpl extends AbstractReportDescriptor {

        /**
         * Validator to check form fields.
         */
        private final TestValidator testValidator = new TestValidator();

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * Validates the timeout.
         *
         * @param value
         *            the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return testValidator.validateTimeout(value, getDefaultTimeout());
        }

        @Override
        public String getDisplayName() {
            return Messages.TraceAnalysisPublisher_DisplayName();
        }
    }

    /**
     * {@link Callable} enabling executing the trace analysis of job files remotely.
     */
    private static final class TraceAnalysisCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final List<FilePath> jobFiles;
        private final boolean createReportDir;
        private final int timeout;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link TraceAnalysisCallable}.
         *
         * @param jobFiles
         *            the list of analysis files
         * @param createReportDir
         *            specifies whether to create a new report directory
         * @param timeout
         *            the timeout running each trace analysis
         * @param listener
         *            the listener
         */
        TraceAnalysisCallable(final List<FilePath> jobFiles, final boolean createReportDir,
                final int timeout, final TaskListener listener) {
            this.jobFiles = jobFiles;
            this.createReportDir = createReportDir;
            this.timeout = timeout;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isAnalyzed = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId);
                    AnalysisEnvironment analysisEnv = (AnalysisEnvironment) comClient.getAnalysisEnvironment()) {
                for (final FilePath jobFile : jobFiles) {
                    logger.logInfo(String.format("- Running trace analysis: %s", jobFile.getRemote()));
                    final AnalysisExecutionInfo execInfo =
                            (AnalysisExecutionInfo) analysisEnv.executeJob(jobFile.getRemote(), createReportDir);
                    int tickCounter = 0;
                    final long endTimeMillis = System.currentTimeMillis() + Long.valueOf(timeout) * 1000L;
                    while ("RUNNING".equals(execInfo.getState())) {
                        if (tickCounter % 60 == 0) {
                            logger.logInfo("-- tick...");
                        }
                        if (timeout > 0 && System.currentTimeMillis() > endTimeMillis) {
                            logger.logWarn(String.format("-> Analysis execution timeout of %d seconds reached! "
                                    + "Aborting trace analysis now...", timeout));
                            execInfo.abort();
                            break;
                        }
                        Thread.sleep(1000L);
                        tickCounter++;
                    }
                    getTestInfo(execInfo, logger);
                }
            } catch (final ETComException | InterruptedException e) {
                isAnalyzed = false;
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isAnalyzed;
        }

        /**
         * Gets the information of the executed package.
         *
         * @param execInfo
         *            the execution info
         * @param logger
         *            the logger
         * @throws ETComException
         *             in case of a COM exception
         */
        private void getTestInfo(final AnalysisExecutionInfo execInfo, final TTConsoleLogger logger)
                throws ETComException {
            final String testResult = execInfo.getResult();
            logger.logInfo(String.format("-> Analysis execution completed with result: %s", testResult));
            final String testReportDir = new File(execInfo.getReportDb()).getParentFile().getAbsolutePath();
            logger.logInfo(String.format("-> Test report directory: %s", testReportDir));
        }
    }

    /**
     * {@link Callable} enabling merging the analysis reports into the main report remotely.
     */
    private static final class MergeReportsCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final FilePath mainReport;
        private final List<FilePath> jobReports;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link MergeReportsCallable}.
         *
         * @param mainReport
         *            the main report
         * @param jobReports
         *            the job reports
         * @param listener
         *            the listener
         */
        MergeReportsCallable(final FilePath mainReport, final List<FilePath> jobReports, final TaskListener listener) {
            this.mainReport = mainReport;
            this.jobReports = jobReports;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isMerged = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId);
                    AnalysisEnvironment analysisEnv = (AnalysisEnvironment) comClient.getAnalysisEnvironment()) {
                final List<String> jobFiles = getJobFiles(jobReports);
                logger.logInfo(String.format("- Merging analysis reports into main report: %s",
                        mainReport.getRemote()));
                isMerged = analysisEnv.mergeJobReports(mainReport.getRemote(), jobFiles);
            } catch (final ETComException e) {
                isMerged = false;
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isMerged;
        }

        /**
         * Gets the list job files with their absolute file paths.
         *
         * @param jobReports
         *            the job reports
         * @return the list of job files
         */
        private List<String> getJobFiles(final List<FilePath> jobReports) {
            final List<String> jobFiles = new ArrayList<String>();
            for (final FilePath jobReport : jobReports) {
                jobFiles.add(jobReport.getRemote());
            }
            return jobFiles;
        }
    }
}
