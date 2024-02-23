/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogAnnotation.Severity;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Publisher parsing the ecu.test log files and providing links to saved {@link ETLogReport}s.
 */
public class ETLogPublisher extends AbstractReportPublisher {

    /**
     * File name variations of the standard ecu.test log file.
     */
    static final List<String> INFO_LOG_NAMES = Arrays.asList("ecu.test_out.log", "ECU_TEST_OUT.log");

    /**
     * File name variations of the error ecu.test log file.
     */
    static final List<String> ERROR_LOG_NAMES = Arrays.asList("ecu.test_err.log", "ECU_TEST_ERR.log");

    /**
     * The URL name to {@link ETLogReport}s holding by {@link AbstractETLogAction}.
     */
    protected static final String URL_NAME = "ecutest-logs";

    /**
     * Specifies hether to mark the build as unstable if warnings found.
     */
    private boolean unstableOnWarning;
    /**
     * Specifies whether to mark the build as failed if errors found.
     */
    private boolean failedOnError;
    /**
     * Specifies whether to parse the test-specific log files.
     *
     * @since 1.10
     */
    private boolean testSpecific;

    /**
     * Instantiates a new {@link ETLogPublisher}.
     */
    @DataBoundConstructor
    public ETLogPublisher() {
        super();
    }

    public boolean isUnstableOnWarning() {
        return unstableOnWarning;
    }

    @DataBoundSetter
    public void setUnstableOnWarning(final boolean unstableOnWarning) {
        this.unstableOnWarning = unstableOnWarning;
    }

    public boolean isFailedOnError() {
        return failedOnError;
    }

    @DataBoundSetter
    public void setFailedOnError(final boolean failedOnError) {
        this.failedOnError = failedOnError;
    }

    public boolean isTestSpecific() {
        return testSpecific;
    }

    @DataBoundSetter
    public void setTestSpecific(final boolean testSpecific) {
        this.testSpecific = testSpecific;
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    @Override
    public void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                              final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final TTConsoleLogger logger = getLogger();
        logger.logInfo("Publishing ecu.test logs...");

        if (isSkipped(false, run, launcher)) {
            return;
        }

        if (isArchiving()) {
            final List<ETLogReport> logReports = new ArrayList<>();
            final FilePath archiveTarget = getArchiveTarget(run);

            // Removing old artifacts at project level
            if (!isKeepAll()) {
                archiveTarget.deleteRecursive();
                removePreviousReports(run, ETLogBuildAction.class);
            }

            if (isTestSpecific()) {
                final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
                for (final FilePath reportDir : reportDirs) {
                    final FilePath archiveTargetDir = archiveTarget.child(reportDir.getName());
                    if (reportDir.exists()) {
                        try {
                            logger.logInfo(String.format("- Archiving log files: %s", reportDir));
                            final String mask = Stream.concat(ERROR_LOG_NAMES.stream(), INFO_LOG_NAMES.stream())
                                .map(s -> String.format("**/%s", s))
                                .reduce((s, s2) -> String.format("%s,%s", s, s2))
                                .orElse("");
                            final int copiedFiles = reportDir.copyRecursiveTo(mask, archiveTargetDir);
                            if (copiedFiles == 0) {
                                continue;
                            } else if (copiedFiles > 2) {
                                logger.logInfo(String.format("-> Archived %d sub-report(s).", copiedFiles / 2 - 1));
                            }
                        } catch (final IOException e) {
                            Util.displayIOException(e, listener);
                            logger.logError("Failed publishing ecu.test logs.");
                            run.setResult(Result.FAILURE);
                            return;
                        }
                        traverseReports(logReports, archiveTargetDir);
                    }
                }
            } else {
                final List<FilePath> logFiles = getCompleteLogFiles(run, workspace, launcher);
                for (final FilePath logFile : logFiles) {
                    final FilePath targetFile = archiveTarget.child(logFile.getName());
                    try {
                        if (logFile.exists()) {
                            logger.logInfo(String.format("- Archiving log file: %s", logFile));
                            logFile.copyTo(targetFile);
                        } else {
                            if (isAllowMissing()) {
                                continue;
                            } else {
                                logger.logError(String.format("Specified ecu.test log file '%s' does not exist.",
                                    logFile));
                                run.setResult(Result.FAILURE);
                                return;
                            }
                        }
                    } catch (final IOException e) {
                        Util.displayIOException(e, listener);
                        logger.logError("Failed publishing ecu.test logs.");
                        run.setResult(Result.FAILURE);
                        return;
                    }
                    final ETLogReport logReport = parseLogFile(logFile, logFile.getParent());
                    logReports.add(logReport);
                }
            }

            if (logReports.isEmpty()) {
                logger.logInfo("No log results found.");
                if (!isAllowMissing()) {
                    logger.logError("Empty log results are not allowed, setting build status to FAILURE!");
                    run.setResult(Result.FAILURE);
                    return;
                }
            } else {
                addBuildAction(run, logReports);
                setBuildResult(run, logReports);
            }
        } else {
            logger.logInfo("Archiving ecu.test logs is disabled.");
        }

        logger.logInfo("ecu.test logs published successfully.");
    }

    /**
     * Parses the ecu.test log file.
     *
     * @param logFile          the log file
     * @param archiveTargetDir the archive target directory
     * @return the parsed {@link ETLogReport}
     * @throws IOException          signals that an I/O exception has occurred.
     * @throws InterruptedException if the build gets interrupted
     */
    private ETLogReport parseLogFile(final FilePath logFile, final FilePath archiveTargetDir)
        throws IOException, InterruptedException {
        final ETLogParser logParser = new ETLogParser(logFile);
        final List<ETLogAnnotation> logs = logParser.parse();
        final int warningLogCount = logParser.parseLogCount(Severity.WARNING);
        final int errorLogCount = logParser.parseLogCount(Severity.ERROR);

        final String logTitle;
        final String relLogFile = archiveTargetDir.toURI().relativize(logFile.toURI()).getPath();
        if (isTestSpecific() && !logFile.getParent().getParent().getName().equals(archiveTargetDir.getName())) {
            logTitle = logFile.getParent().getName().replaceFirst("^Report\\s", "") + "/" + logFile.getName();
        } else {
            logTitle = logFile.getName();
        }
        return new ETLogReport(randomId(), logTitle, relLogFile, logFile.length(), logs,
            warningLogCount, errorLogCount);
    }

    /**
     * Creates the main report and adds the sub-reports by traversing them recursively.
     *
     * @param logReports       the log reports
     * @param archiveTargetDir the archive target directory
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private void traverseReports(final List<ETLogReport> logReports, final FilePath archiveTargetDir)
        throws IOException, InterruptedException {
        final ETLogReport logReport = new ETLogReport(randomId(), archiveTargetDir.getName(),
            archiveTargetDir.getName(), getDirectorySize(archiveTargetDir), Collections.emptyList(), 0, 0);
        logReports.add(logReport);

        FilePath errorLogFile = null;
        FilePath infoLogFile = null;
        for (final String logFileName: ERROR_LOG_NAMES) {
            errorLogFile = archiveTargetDir.child(logFileName);
            if (errorLogFile.exists()) {
                break;
            }
        }
        for (final String logFileName: INFO_LOG_NAMES) {
            infoLogFile = archiveTargetDir.child(logFileName);
            if (infoLogFile.exists()) {
                break;
            }
        }
        if (errorLogFile != null && errorLogFile.exists() && infoLogFile != null && infoLogFile.exists()) {
            final ETLogReport errorLogReport = parseLogFile(errorLogFile, archiveTargetDir.getParent());
            logReport.addSubReport(errorLogReport);
            final ETLogReport infoLogReport = parseLogFile(infoLogFile, archiveTargetDir.getParent());
            logReport.addSubReport(infoLogReport);
        }

        // Search for sub-reports
        traverseSubReports(logReport, archiveTargetDir.getParent(), archiveTargetDir);
    }

    /**
     * Traverses the sub-report directories recursively and searches for TRF reports.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param logReport        the log report
     * @param testReportDir    the main test report directory
     * @param subTestReportDir the sub test report directory
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private void traverseSubReports(final ETLogReport logReport, final FilePath testReportDir,
                                    final FilePath subTestReportDir) throws IOException, InterruptedException {
        for (final FilePath subDir : subTestReportDir.listDirectories()) {
            FilePath logFile;
            for (final String logFileName: ERROR_LOG_NAMES) {
                logFile = subDir.child(logFileName);
                if (logFile.exists()) {
                    final ETLogReport subReport = parseLogFile(logFile, testReportDir);
                    logReport.addSubReport(subReport);

                }
            }
            for (final String logFileName: INFO_LOG_NAMES) {
                logFile = subDir.child(logFileName);
                if (logFile.exists()) {
                    final ETLogReport subReport = parseLogFile(logFile, testReportDir);
                    logReport.addSubReport(subReport);
                    traverseSubReports(subReport, testReportDir, subDir);
                }
            }
        }
    }

    /**
     * Adds the {@link ETLogBuildAction} to the build holding the found {@link ETLogReport}s.
     *
     * @param run        the run
     * @param logReports the list of {@link ETLogReport}s to add
     */
    private void addBuildAction(final Run<?, ?> run, final List<ETLogReport> logReports) {
        ETLogBuildAction action = run.getAction(ETLogBuildAction.class);
        if (action == null) {
            action = new ETLogBuildAction(!isKeepAll());
            run.addAction(action);
        }
        action.addAll(logReports);
    }

    /**
     * Sets the build result in case of errors or warnings.
     *
     * @param run        the run
     * @param logReports the log reports
     */
    private void setBuildResult(final Run<?, ?> run, final List<ETLogReport> logReports) {
        final TTConsoleLogger logger = getLogger();
        int totalWarnings = 0;
        int totalErrors = 0;
        for (final ETLogReport logReport : logReports) {
            totalWarnings += logReport.getTotalWarningCount();
            totalErrors += logReport.getTotalErrorCount();
        }
        logger.logInfo("- Parsing log files...");
        if (totalErrors > 0 && isFailedOnError()) {
            logger.logInfo(String.format(
                "-> %d error(s) found in the ecu.test logs, setting build status to FAILURE!",
                totalErrors));
            run.setResult(Result.FAILURE);
        } else if (totalWarnings > 0 && isUnstableOnWarning()) {
            logger.logInfo(String.format(
                "-> %d warning(s) found in the ecu.test logs, setting build status to UNSTABLE!",
                totalWarnings));
            run.setResult(Result.UNSTABLE);
        } else {
            logger.logInfo(String.format("-> %d warning(s) and %d error(s) found in the ecu.test logs.",
                totalWarnings, totalErrors));
        }
    }

    /**
     * Builds a list of the entire ecu.test log files for archiving.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @return the complete log files
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private List<FilePath> getCompleteLogFiles(final Run<?, ?> run, final FilePath workspace, final Launcher launcher)
        throws IOException, InterruptedException {
        final List<FilePath> logFiles = new ArrayList<>();
        final FilePath workspacePath;
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (isDownstream()) {
            workspacePath = workspace.child(getWorkspace());
        } else if (toolEnvAction != null) {
            workspacePath = new FilePath(launcher.getChannel(), toolEnvAction.getToolSettings());
        } else {
            workspacePath = workspace;
        }
        if (workspacePath != null && workspacePath.exists()) {
            final String includes = Stream.concat(ERROR_LOG_NAMES.stream(), INFO_LOG_NAMES.stream())
                .reduce((s, s2) -> String.format("%s,%s", s, s2))
                .orElse("");
            for (final String includeFile : workspacePath.act(new ListFilesCallable(includes, ""))) {
                final FilePath logFile = new FilePath(launcher.getChannel(), includeFile);
                logFiles.add(logFile);
            }
        }
        return logFiles;
    }

    @Override
    protected String getUrlName() {
        return URL_NAME;
    }

    /**
     * {@link FileCallable} providing remote file access to list included files.
     */
    private static final class ListFilesCallable extends MasterToSlaveFileCallable<List<String>> {

        private static final long serialVersionUID = 1;

        private final String includes;
        private final String excludes;

        /**
         * Instantiates a new {@link ListFilesCallable}.
         *
         * @param includes the inclusion file pattern
         * @param excludes the exclusion file pattern
         */
        ListFilesCallable(final String includes, final String excludes) {
            this.includes = includes;
            this.excludes = excludes;
        }

        @Override
        public List<String> invoke(final File baseDir, final VirtualChannel channel) {
            final List<String> files = new ArrayList<>();
            for (final String includedFile : Util.createFileSet(baseDir, includes, excludes)
                    .getDirectoryScanner().getIncludedFiles()) {
                final File file = new File(baseDir, includedFile);
                files.add(file.getPath());
            }
            return files;
        }
    }

    /**
     * DescriptorImpl for {@link ETLogPublisher}.
     */
    @Symbol("publishETLogs")
    @Extension(ordinal = 10003)
    public static final class DescriptorImpl extends AbstractReportDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ETLogPublisher_DisplayName();
        }
    }

    /**
     * Listener that can be notified when a build is started to delete previous ecu.test log files.
     */
    public static final class RunListenerImpl {

        /**
         * Deletes previous ecu.test log files.
         *
         * @param workspace the ecu.test workspace containing log files
         * @param listener  the listener
         */
        public static void onStarted(final FilePath workspace, final TaskListener listener) {
            if (workspace != null && listener != null) {
                try {
                    for (final String logFileName: ERROR_LOG_NAMES) {
                        final FilePath logFile = workspace.child(logFileName);
                        if (logFile.exists()) {
                            logFile.delete();
                        }
                    }
                    for (final String logFileName: INFO_LOG_NAMES) {
                        final FilePath logFile = workspace.child(logFileName);
                        if (logFile.exists()) {
                            logFile.delete();
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    final TTConsoleLogger logger = new TTConsoleLogger(listener);
                    logger.logWarn("Failed deleting ecu.test log files: " + e.getMessage());
                }
            }
        }
    }
}
