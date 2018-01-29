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
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;

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
    private boolean createReportDir = false;
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
        final TTConsoleLogger logger = getLogger();
        logger.logInfo("Publishing trace analysis...");

        if (isSkipped(run, launcher)) {
            return;
        }

        final Map<FilePath, List<FilePath>> analysisFiles = getAnalysisFiles(run, workspace, launcher);
        if (analysisFiles.isEmpty() && !isAllowMissing()) {
            throw new ETPluginException("Empty analysis results are not allowed, setting build status to FAILURE!");
        }

        final List<TraceAnalysisReport> reports = new ArrayList<TraceAnalysisReport>();
        if (isETRunning(launcher)) {
            reports.addAll(performAnalysis(analysisFiles, run, launcher, listener));
        } else {
            final ETClient etClient = getToolClient(toolName, run, workspace, launcher, listener);
            if (etClient.start(false, workspace, launcher, listener)) {
                reports.addAll(performAnalysis(analysisFiles, run, launcher, listener));
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

    /**
     * Performs the trace analysis.
     *
     * @param analysisFiles
     *            the analysis files
     * @param run
     *            the run
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the list of trace analysis reports
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<TraceAnalysisReport> performAnalysis(final Map<FilePath, List<FilePath>> analysisFiles,
            final Run<?, ?> run, final Launcher launcher, final TaskListener listener)
                    throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<TraceAnalysisReport> reports = new ArrayList<TraceAnalysisReport>();

        int index = 0;
        final FilePath archiveTarget = prepareArchive(run);
        for (final Entry<FilePath, List<FilePath>> analysisEntry : analysisFiles.entrySet()) {
            final FilePath reportDir = analysisEntry.getKey();
            final List<FilePath> jobFiles = analysisEntry.getValue();
            final FilePath archiveTargetDir = archiveTarget.child(reportDir.getBaseName());

            // Run trace analysis
            final TraceAnalysisRunner runner = new TraceAnalysisRunner();
            final List<FilePath> reportFiles = runner.runAnalysis(jobFiles, isCreateReportDir(),
                    getParsedTimeout(), launcher, listener);

            if (reportFiles.isEmpty() && !isAllowMissing()) {
                logger.logError(String
                        .format("-> Empty analysis results are not allowed, setting build status to FAILURE!"));
                run.setResult(Result.FAILURE);
            }

            if (isMergeReports()) {
                // Merge reports
                final FilePath mainReport = getFirstReportFile(reportDir);
                final boolean isMerged = runner.mergeReports(mainReport, reportFiles, launcher, listener);

                if (!isMerged) {
                    logger.logError(String
                            .format("-> Failed merging analysis reports, setting build status to FAILURE!"));
                    run.setResult(Result.FAILURE);
                }

                if (isArchiving()) {
                    logger.logInfo(String.format("- Archiving main report: %s", mainReport));
                    archiveReport(mainReport, archiveTargetDir, run, logger);
                    index = addReport(reports, index, reportDir, mainReport);
                }
            } else if (isArchiving()) {
                for (final FilePath reportFile : reportFiles) {
                    logger.logInfo(String.format("- Archiving analysis report: %s", reportFile));
                    FilePath targetDir;
                    if (isCreateReportDir()) {
                        targetDir = archiveTargetDir.getParent().child(reportFile.getParent().getName());
                    } else {
                        targetDir = archiveTargetDir.child(reportFile.getParent().getName());
                    }
                    archiveReport(reportFile, targetDir, run, logger);
                    index = addReport(reports, index, reportDir, reportFile);
                }
            }
        }

        return reports;
    }

    /**
     * Prepares the target archive directory and removes old artifacts
     * at project level when keeping the most recent artifacts only.
     *
     * @param run
     *            the run
     * @return the archive file path
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private FilePath prepareArchive(final Run<?, ?> run) throws IOException, InterruptedException {
        final FilePath archiveTarget = getArchiveTarget(run);
        archiveTarget.mkdirs();
        if (!isKeepAll()) {
            archiveTarget.deleteRecursive();
            removePreviousReports(run, TraceAnalysisBuildAction.class);
        }
        return archiveTarget;
    }

    /**
     * Archives the analysis report on master.
     *
     * @param reportFile
     *            the report file
     * @param archiveTarget
     *            the archive target
     * @param run
     *            the run
     * @param logger
     *            the logger
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private void archiveReport(final FilePath reportFile, final FilePath archiveTarget, final Run<?, ?> run,
            final TTConsoleLogger logger) throws IOException, InterruptedException {
        if (reportFile.exists()) {
            reportFile.copyTo(archiveTarget.child(reportFile.getName()));
        } else if (!isAllowMissing()) {
            logger.logError(String.format("-> Specified report file '%s' does not exist.",
                    reportFile.getName()));
            run.setResult(Result.FAILURE);
        }
    }

    /**
     * Adds an analysis report to list of reports.
     *
     * @param reports
     *            the reports
     * @param index
     *            the current report index
     * @param targetDir
     *            the target directory
     * @param report
     *            the report to add
     * @return the increased report index
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private int addReport(final List<TraceAnalysisReport> reports, int index, final FilePath targetDir,
            final FilePath report) throws IOException, InterruptedException {
        final String relFilePath = targetDir.getParent().toURI().relativize(report.toURI()).getPath();
        final TraceAnalysisReport trfReport = new TraceAnalysisReport(String.format("%d", ++index),
                report.getParent().getName(), relFilePath, report.length());
        reports.add(trfReport);
        return index;
    }

    /**
     * Collects the analysis job files from all report directories.
     *
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @return the analysis files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private Map<FilePath, List<FilePath>> getAnalysisFiles(final Run<?, ?> run, final FilePath workspace,
            final Launcher launcher)
            throws IOException, InterruptedException {
        final Map<FilePath, List<FilePath>> analysisFiles = new LinkedHashMap<FilePath, List<FilePath>>();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        for (final FilePath reportDir : reportDirs) {
            final List<FilePath> jobFiles = Arrays.asList(reportDir.list("**/Job_*.ajob"));
            Collections.reverse(jobFiles);
            analysisFiles.put(reportDir, jobFiles);
        }
        return analysisFiles;
    }

    /**
     * Adds the {@link TraceAnalysisBuildAction} to the build holding the found {@link TraceAnalysisReport}s.
     *
     * @param run
     *            the run
     * @param analysisReports
     *            the list of {@link TraceAnalysisReport}s to add
     */
    private void addBuildAction(final Run<?, ?> run, final List<TraceAnalysisReport> analysisReports) {
        TraceAnalysisBuildAction action = run.getAction(TraceAnalysisBuildAction.class);
        if (action == null) {
            action = new TraceAnalysisBuildAction(!isKeepAll());
            run.addAction(action);
        }
        action.addAll(analysisReports);
    }

    @Override
    protected String getUrlName() {
        return URL_NAME;
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
}
