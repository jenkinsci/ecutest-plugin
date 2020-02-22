/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractToolPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class holding the trace analysis configuration.
 */
public class TraceAnalysisPublisher extends AbstractToolPublisher {

    /**
     * Defines the default timeout running each trace analysis.
     */
    protected static final int DEFAULT_TIMEOUT = 3600;

    /**
     * The URL name to {@link TraceAnalysisReport}s holding by {@link AbstractTraceAnalysisAction}.
     */
    protected static final String URL_NAME = "trace-analysis";

    /**
     * Specifies whether to merge analysis job reports.
     */
    private boolean mergeReports = true;
    /**
     * Specifies whether to create a new report directory.
     */
    private boolean createReportDir = false;
    private String timeout = String.valueOf(getDefaultTimeout());

    /**
     * Instantiates a new {@link TraceAnalysisPublisher}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public TraceAnalysisPublisher(@Nonnull final String toolName) {
        super(toolName);
    }

    /**
     * Parses a string-based parameter to integer.
     *
     * @param param the parameter string
     * @return the parsed integer value represented by the String parameter,
     * defaults to {@link #DEFAULT_TIMEOUT} if null or invalid value
     */
    public static int parse(final String param) {
        try {
            return Integer.parseInt(param);
        } catch (final NumberFormatException e) {
            return DEFAULT_TIMEOUT;
        }
    }

    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public boolean isMergeReports() {
        return mergeReports;
    }

    @DataBoundSetter
    public void setMergeReports(final boolean mergeReports) {
        this.mergeReports = mergeReports;
    }

    public boolean isCreateReportDir() {
        return createReportDir;
    }

    @DataBoundSetter
    public void setCreateReportDir(final boolean createReportDir) {
        this.createReportDir = createReportDir;
    }

    public int getParsedTimeout() {
        return parse(getTimeout());
    }

    public String getTimeout() {
        return timeout;
    }

    @DataBoundSetter
    public void setTimeout(@CheckForNull final String timeout) {
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(getDefaultTimeout()));
    }

    @Override
    protected void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                                 final TaskListener listener)
        throws InterruptedException, IOException, ETPluginException {
        final TTConsoleLogger logger = getLogger();
        logger.logInfo("Publishing trace analysis...");

        if (isSkipped(true, run, launcher)) {
            return;
        }

        final Map<FilePath, List<FilePath>> analysisFiles = getAnalysisFiles(run, workspace, launcher);
        if (analysisFiles.isEmpty() && !isAllowMissing()) {
            throw new ETPluginException("Empty analysis results are not allowed, setting build status to FAILURE!");
        }

        boolean isPublished = false;
        final List<TraceAnalysisReport> reports = new ArrayList<>();
        if (isETRunning(launcher, listener)) {
            reports.addAll(performAnalysis(analysisFiles, run, launcher, listener));
            isPublished = true;
        } else {
            final ETClient etClient = getToolClient(run, workspace, launcher, listener);
            if (etClient.start(false, workspace, launcher, listener)) {
                reports.addAll(performAnalysis(analysisFiles, run, launcher, listener));
                isPublished = true;
            } else {
                logger.logError(String.format("Starting %s failed.", getToolName()));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", getToolName()));
            }
        }

        if (isArchiving()) {
            addBuildAction(run, reports);
        } else {
            logger.logInfo("Archiving trace analysis reports is disabled.");
        }

        if (isPublished) {
            logger.logInfo("Trace analysis reports published successfully.");
        } else {
            logger.logInfo("Failed publishing trace analysis reports.");
            run.setResult(Result.FAILURE);
        }
    }

    /**
     * Performs the trace analysis.
     *
     * @param analysisFiles the analysis files
     * @param run           the run
     * @param launcher      the launcher
     * @param listener      the listener
     * @return the list of trace analysis reports
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private List<TraceAnalysisReport> performAnalysis(final Map<FilePath, List<FilePath>> analysisFiles,
                                                      final Run<?, ?> run, final Launcher launcher,
                                                      final TaskListener listener)
        throws IOException, InterruptedException {
        final TTConsoleLogger logger = getLogger();
        final List<TraceAnalysisReport> reports = new ArrayList<>();

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
                logger.logError("-> Empty analysis results are not allowed, setting build status to FAILURE!");
                run.setResult(Result.FAILURE);
            }

            if (isMergeReports()) {
                // Merge reports
                final FilePath mainReport = getFirstReportFile(reportDir);
                final boolean isMerged = runner.mergeReports(mainReport, reportFiles, launcher, listener);

                if (!isMerged) {
                    logger.logError("-> Failed merging analysis reports, setting build status to FAILURE!");
                    run.setResult(Result.FAILURE);
                }

                if (isArchiving()) {
                    logger.logInfo(String.format("- Archiving main report: %s", mainReport));
                    archiveReport(mainReport, archiveTargetDir, run, logger);
                    addReport(reports, reportDir, mainReport);
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
                    addReport(reports, reportDir, reportFile);
                }
            }
        }

        return reports;
    }

    /**
     * Prepares the target archive directory and removes old artifacts
     * at project level when keeping the most recent artifacts only.
     *
     * @param run the run
     * @return the archive file path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
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
     * @param reportFile    the report file
     * @param archiveTarget the archive target
     * @param run           the run
     * @param logger        the logger
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
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
     * @param reports   the reports
     * @param targetDir the target directory
     * @param report    the report to add
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    private void addReport(final List<TraceAnalysisReport> reports, final FilePath targetDir,
                           final FilePath report) throws IOException, InterruptedException {
        final String relFilePath = targetDir.getParent().toURI().relativize(report.toURI()).getPath();
        final TraceAnalysisReport trfReport = new TraceAnalysisReport(randomId(), report.getParent().getName(),
            relFilePath, report.length());
        reports.add(trfReport);
    }

    /**
     * Collects the analysis job files from all report directories.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @return the analysis files
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    private Map<FilePath, List<FilePath>> getAnalysisFiles(final Run<?, ?> run, final FilePath workspace,
                                                           final Launcher launcher)
        throws IOException, InterruptedException {
        final Map<FilePath, List<FilePath>> analysisFiles = new LinkedHashMap<>();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        for (final FilePath reportDir : reportDirs) {
            final List<FilePath> jobFiles = Arrays.asList(reportDir.list("**/*.ajob"));
            Collections.reverse(jobFiles);
            analysisFiles.put(reportDir, jobFiles);
        }
        return analysisFiles;
    }

    /**
     * Adds the {@link TraceAnalysisBuildAction} to the build holding the found {@link TraceAnalysisReport}s.
     *
     * @param run             the run
     * @param analysisReports the list of {@link TraceAnalysisReport}s to add
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

        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * Validates the timeout.
         *
         * @param value the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return testValidator.validateTimeout(value, getDefaultTimeout());
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.TraceAnalysisPublisher_DisplayName();
        }
    }
}
