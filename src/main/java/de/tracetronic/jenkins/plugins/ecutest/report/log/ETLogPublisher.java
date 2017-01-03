/**
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jenkins.MasterToSlaveFileCallable;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction.TestType;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogAnnotation.Severity;

/**
 * Publisher parsing the ECU-TEST log files and providing links to saved {@link ETLogReport}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogPublisher extends AbstractReportPublisher {

    /**
     * File name of the standard ECU-TEST log file.
     */
    public static final String INFO_LOG_NAME = "ECU_TEST_OUT.log";

    /**
     * File name of the error ECU-TEST log file.
     */
    public static final String ERROR_LOG_NAME = "ECU_TEST_ERR.log";

    /**
     * The URL name to {@link ETLogReport}s holding by {@link AbstractETLogAction}.
     */
    protected static final String URL_NAME = "ecutest-logs";

    private boolean unstableOnWarning;
    private boolean failedOnError;
    /**
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

    /**
     * Instantiates a new {@link ETLogPublisher}.
     *
     * @param unstableOnWarning
     *            specifies whether to mark the build as unstable if warnings found
     * @param failedOnError
     *            specifies whether to mark the build as failed if errors found
     * @param testSpecific
     *            specifies whether to parse the test-specific log files
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     * @param archiving
     *            specifies whether archiving artifacts is enabled
     * @param keepAll
     *            specifies whether artifacts are archived for all successful builds,
     *            otherwise only the most recent
     * @deprecated since 1.11 use {@link #ETLogPublisher()}
     */
    @Deprecated
    public ETLogPublisher(final boolean unstableOnWarning, final boolean failedOnError, final boolean testSpecific,
            final boolean allowMissing, final boolean runOnFailed, final boolean archiving, final boolean keepAll) {
        super(allowMissing, runOnFailed, archiving, keepAll);
        this.unstableOnWarning = unstableOnWarning;
        this.failedOnError = failedOnError;
        this.testSpecific = testSpecific;
    }

    /**
     * Instantiates a new {@link ETLogPublisher}.
     *
     * @param unstableOnWarning
     *            specifies whether to mark the build as unstable if warnings found
     * @param failedOnError
     *            specifies whether to mark the build as failed if errors found
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     * @param archiving
     *            specifies whether archiving artifacts is enabled
     * @param keepAll
     *            specifies whether artifacts are archived for all successful builds,
     *            otherwise only the most recent
     * @deprecated since 1.10, use {@link #ETLogPublisher(boolean, boolean, boolean, boolean, boolean,boolean, boolean)}
     */
    @Deprecated
    public ETLogPublisher(final boolean unstableOnWarning, final boolean failedOnError, final boolean allowMissing,
            final boolean runOnFailed, final boolean archiving, final boolean keepAll) {
        this(unstableOnWarning, failedOnError, false, allowMissing, runOnFailed, archiving, keepAll);
    }

    /**
     * Instantiates a new {@link ETLogPublisher}.
     *
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     * @param unstableOnWarning
     *            specifies whether to mark the build as unstable if warnings found
     * @param failedOnError
     *            specifies whether to mark the build as failed if errors found
     * @deprecated since 1.9, use {@link #ETLogPublisher(boolean, boolean, boolean, boolean,boolean, boolean)}
     */
    @Deprecated
    public ETLogPublisher(final boolean unstableOnWarning, final boolean failedOnError, final boolean allowMissing,
            final boolean runOnFailed) {
        this(unstableOnWarning, failedOnError, allowMissing, runOnFailed, true, true);
    }

    /**
     * Convert legacy configuration into the new class structure.
     *
     * @return an instance of this class with all the new fields transferred from the old structure to the new one
     */
    public final Object readResolve() {
        return new ETLogPublisher(unstableOnWarning, failedOnError, isTestSpecific(), isAllowMissing(),
                isRunOnFailed(),
                isArchiving() == null ? true : isArchiving(), isKeepAll() == null ? true : isKeepAll());
    }

    /**
     * @return whether to mark the build as unstable if warnings found
     */
    public boolean isUnstableOnWarning() {
        return unstableOnWarning;
    }

    /**
     * @return whether to mark the build as failed if errors found
     */
    public boolean isFailedOnError() {
        return failedOnError;
    }

    /**
     * @return whether to parse the test-specific log files
     */
    public boolean isTestSpecific() {
        return testSpecific;
    }

    /**
     * @param unstableOnWarning
     *            specifies whether to mark the build as unstable if warnings found
     */
    @DataBoundSetter
    public void setUnstableOnWarning(final boolean unstableOnWarning) {
        this.unstableOnWarning = unstableOnWarning;
    }

    /**
     * @param failedOnError
     *            specifies whether to mark the build as failed if errors found
     */
    @DataBoundSetter
    public void setFailedOnError(final boolean failedOnError) {
        this.failedOnError = failedOnError;
    }

    /**
     * @param testSpecific
     *            specifies whether to parse the test-specific log files
     */
    @DataBoundSetter
    public void setTestSpecific(final boolean testSpecific) {
        this.testSpecific = testSpecific;
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    @Override
    public void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing ECU-TEST logs...");

        final Result buildResult = run.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            logger.logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return;
        }

        if (isArchiving()) {
            final List<ETLogReport> logReports = new ArrayList<ETLogReport>();
            final FilePath archiveTarget = getArchiveTarget(run);

            // Removing old artifacts at project level
            if (!isKeepAll()) {
                archiveTarget.deleteRecursive();
                removePreviousReports(run, ETLogBuildAction.class);
            }

            if (isTestSpecific()) {
                int index = 0;
                final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
                for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
                    final FilePath testReportDir = new FilePath(launcher.getChannel(),
                            testEnvAction.getTestReportDir());
                    final FilePath archiveTargetDir = archiveTarget.child(testReportDir.getName());
                    if (testReportDir.exists()) {
                        try {
                            logger.logInfo(String.format("- Archiving log files: %s", testReportDir));
                            final int copiedFiles = testReportDir.copyRecursiveTo(
                                    String.format("**/%s,**/%s", ERROR_LOG_NAME, INFO_LOG_NAME), archiveTargetDir);
                            if (copiedFiles == 0) {
                                continue;
                            } else if (copiedFiles > 2) {
                                logger.logInfo(String.format("-> Archived %d sub-report(s).", copiedFiles / 2 - 1));
                            }
                        } catch (final IOException e) {
                            Util.displayIOException(e, listener);
                            logger.logError("Failed publishing ECU-TEST logs.");
                            run.setResult(Result.FAILURE);
                            return;
                        }
                        index = traverseReports(logReports, archiveTargetDir, index);
                    }
                }
            } else {
                final List<FilePath> logFiles = getLogFiles(run, workspace, launcher);
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
                                logger.logError(String.format("Specified ECU-TEST log file '%s' does not exist.",
                                        logFile));
                                run.setResult(Result.FAILURE);
                                return;
                            }
                        }
                    } catch (final IOException e) {
                        Util.displayIOException(e, listener);
                        logger.logError("Failed publishing ECU-TEST logs.");
                        run.setResult(Result.FAILURE);
                        return;
                    }
                    final ETLogReport logReport = parseLogFile(logFile, logFile.getParent(), logReports.size() + 1);
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
                setBuildResult(run, listener, logReports);
            }
        } else {
            logger.logInfo("Archiving ECU-TEST logs is disabled.");
        }

        logger.logInfo("ECU-TEST logs published successfully.");
    }

    /**
     * Parses the ECU-TEST log file.
     *
     * @param logFile
     *            the log file
     * @param archiveTargetDir
     *            the archive target directory
     * @param id
     *            the report id
     * @return the parsed {@link ETLogReport}
     * @throws IOException
     *             signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private ETLogReport parseLogFile(final FilePath logFile, final FilePath archiveTargetDir, final int id)
            throws IOException, InterruptedException {
        final ETLogParser logParser = new ETLogParser(logFile);
        final List<ETLogAnnotation> logs = logParser.parse();
        final int warningLogCount = logParser.parseLogCount(Severity.WARNING);
        final int errorLogCount = logParser.parseLogCount(Severity.ERROR);

        String logTitle;
        final String relLogFile = archiveTargetDir.toURI().relativize(logFile.toURI()).getPath();
        if (isTestSpecific() && !logFile.getParent().getParent().getName().equals(archiveTargetDir.getName())) {
            logTitle = logFile.getParent().getName().replaceFirst("^Report\\s", "") + "/" + logFile.getName();
        } else {
            logTitle = logFile.getName();
        }
        final ETLogReport logReport = new ETLogReport(String.format("%d", id), logTitle, relLogFile,
                logFile.length(), logs, warningLogCount, errorLogCount);
        return logReport;
    }

    /**
     * Gets the total size of given directory recursively.
     *
     * @param directory
     *            the directory
     * @return the file size
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private long getFileSize(final FilePath directory) throws IOException, InterruptedException {
        long size = 0;
        final FilePath[] files = directory.list("**");
        for (final FilePath file : files) {
            size += file.length();
        }
        return size;
    }

    /**
     * Creates the main report and adds the sub-reports by traversing them recursively.
     *
     * @param logReports
     *            the TRF reports
     * @param archiveTargetDir
     *            the archive target directory
     * @param id
     *            the report id
     * @return the current report id
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private int traverseReports(final List<ETLogReport> logReports, final FilePath archiveTargetDir, int id)
            throws IOException, InterruptedException {
        final ETLogReport logReport = new ETLogReport(String.format("%d", ++id),
                archiveTargetDir.getName(), archiveTargetDir.getName(), getFileSize(archiveTargetDir),
                Collections.<ETLogAnnotation> emptyList(), 0, 0);
        logReports.add(logReport);

        final FilePath errorLogFile = archiveTargetDir.child(ERROR_LOG_NAME);
        final FilePath infoLogFile = archiveTargetDir.child(INFO_LOG_NAME);
        if (errorLogFile.exists() && infoLogFile.exists()) {
            final ETLogReport errorlogReport = parseLogFile(errorLogFile, archiveTargetDir.getParent(), ++id);
            logReport.addSubReport(errorlogReport);
            final ETLogReport infoLogReport = parseLogFile(infoLogFile, archiveTargetDir.getParent(), ++id);
            logReport.addSubReport(infoLogReport);
        }

        // Search for sub-reports
        id = traverseSubReports(logReport, archiveTargetDir.getParent(), archiveTargetDir, id);
        return id;
    }

    /**
     * Traverses the sub-report directories recursively and searches for TRF reports.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param logReport
     *            the TRF report
     * @param testReportDir
     *            the main test report directory
     * @param subTestReportDir
     *            the sub test report directory
     * @param id
     *            the report id
     * @return the current report id
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private int traverseSubReports(final ETLogReport logReport, final FilePath testReportDir,
            final FilePath subTestReportDir, int id)
            throws IOException, InterruptedException {
        for (final FilePath subDir : subTestReportDir.listDirectories()) {
            FilePath logFile = subDir.child(ERROR_LOG_NAME);
            if (logFile.exists()) {
                final ETLogReport subReport = parseLogFile(logFile, testReportDir, ++id);
                logReport.addSubReport(subReport);

            }
            logFile = subDir.child(INFO_LOG_NAME);
            if (logFile.exists()) {
                final ETLogReport subReport = parseLogFile(logFile, testReportDir, ++id);
                logReport.addSubReport(subReport);
                id = traverseSubReports(subReport, testReportDir, subDir, id);
            }
        }
        return id;
    }

    /**
     * Adds the {@link ETLogBuildAction} to the build holding the found {@link ETLogReport}s.
     *
     * @param run
     *            the run
     * @param logReports
     *            the list of {@link ETLogReport}s to add
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
     * @param run
     *            the run
     * @param listener
     *            the listener
     * @param logReports
     *            the log reports
     */
    private void setBuildResult(final Run<?, ?> run, final TaskListener listener,
            final List<ETLogReport> logReports) {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        int totalWarnings = 0;
        int totalErrors = 0;
        for (final ETLogReport logReport : logReports) {
            totalWarnings += logReport.getTotalWarningCount();
            totalErrors += logReport.getTotalErrorCount();
        }
        logger.logInfo("- Parsing log files...");
        if (totalErrors > 0 && isFailedOnError()) {
            logger.logInfo(String.format(
                    "-> %d error(s) found in the ECU-TEST logs, setting build status to FAILURE!",
                    totalErrors));
            run.setResult(Result.FAILURE);
        } else if (totalWarnings > 0 && isUnstableOnWarning()) {
            logger.logInfo(String.format(
                    "-> %d warning(s) found in the ECU-TEST logs, setting build status to UNSTABLE!",
                    totalWarnings));
            run.setResult(Result.UNSTABLE);
        } else {
            logger.logInfo(String.format("-> %d warning(s) and %d error(s) found in the ECU-TEST logs.",
                    totalWarnings, totalErrors));
        }
    }

    /**
     * Builds a list of ECU-TEST log files for archiving, either all test-specific logs or the complete logs.
     *
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @return the list of log files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getLogFiles(final Run<?, ?> run, final FilePath workspace, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> archiveFiles = new ArrayList<FilePath>();
        if (isTestSpecific()) {
            archiveFiles.addAll(getTestSpecificLogFiles(run, launcher));
        } else {
            archiveFiles.addAll(getCompleteLogFiles(run, workspace, launcher));
        }
        return archiveFiles;
    }

    /**
     * Builds a list of the entire ECU-TEST log files for archiving.
     *
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @return the complete log files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getCompleteLogFiles(final Run<?, ?> run, final FilePath workspace, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> archiveFiles = new ArrayList<FilePath>();
        FilePath workspacePath;
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspacePath = new FilePath(launcher.getChannel(), toolEnvAction.getToolSettings());
        } else {
            workspacePath = workspace;
        }
        if (workspacePath != null && workspacePath.exists()) {
            final String includes = String.format("%s,%s", INFO_LOG_NAME, ERROR_LOG_NAME);
            for (final String includeFile : workspacePath.act(new ListFilesCallable(includes, ""))) {
                final FilePath archiveFile = new FilePath(launcher.getChannel(), includeFile);
                archiveFiles.add(archiveFile);
            }
        }
        return archiveFiles;
    }

    /**
     * Builds a list of all test-specific ECU-TEST log files for archiving.
     *
     * @param run
     *            the run
     * @param launcher
     *            the launcher
     * @return the list of log files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getTestSpecificLogFiles(final Run<?, ?> run, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> archiveFiles = new ArrayList<FilePath>();
        final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath testReportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
            if (testReportDir.exists()) {
                String includes;
                if (testEnvAction.getTestType() == TestType.PACKAGE) {
                    includes = String.format("%s/%s,%s/%s", testEnvAction.getTestName(), INFO_LOG_NAME,
                            testEnvAction.getTestName(), ERROR_LOG_NAME);
                } else {
                    includes = String.format("%s,%s", INFO_LOG_NAME, ERROR_LOG_NAME);
                }
                for (final String includeFile : testReportDir.act(new ListFilesCallable(includes, ""))) {
                    final FilePath archiveFile = new FilePath(launcher.getChannel(), includeFile);
                    archiveFiles.add(archiveFile);
                }
            }
        }
        return archiveFiles;
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
         * @param includes
         *            the inclusion file pattern
         * @param excludes
         *            the exclusion file pattern
         */
        ListFilesCallable(final String includes, final String excludes) {
            this.includes = includes;
            this.excludes = excludes;
        }

        @Override
        public List<String> invoke(final File baseDir, final VirtualChannel channel) throws IOException,
                InterruptedException {
            final List<String> files = new ArrayList<String>();
            for (final String includedFile : Util.createFileSet(baseDir, includes, excludes)
                    .getDirectoryScanner().getIncludedFiles()) {
                final File file = new File(baseDir, includedFile);
                files.add(file.getPath());
            }
            return files;
        }
    }

    @Override
    protected String getUrlName() {
        return URL_NAME;
    }

    /**
     * DescriptorImpl for {@link ETLogPublisher}.
     */
    @Symbol("publishETLogs")
    @Extension(ordinal = 1001)
    public static final class DescriptorImpl extends AbstractReportDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.ETLogPublisher_DisplayName();
        }
    }

    /**
     * Listener that can be notified when a build is started to delete previous ECU-TEST log files.
     */
    public static final class RunListenerImpl {

        /**
         * Deletes previous ECU-TEST log files.
         *
         * @param workspace
         *            the ECU-TEST workspace containing log files
         * @param listener
         *            the listener
         */
        public static void onStarted(final FilePath workspace, final TaskListener listener) {
            if (workspace != null && listener != null) {
                try {
                    final FilePath infoLogFile = workspace.child(INFO_LOG_NAME);
                    final FilePath errorLogFile = workspace.child(ERROR_LOG_NAME);
                    if (infoLogFile.exists()) {
                        infoLogFile.delete();
                    }
                    if (errorLogFile.exists()) {
                        errorLogFile.delete();
                    }
                } catch (IOException | InterruptedException e) {
                    final TTConsoleLogger logger = new TTConsoleLogger(listener);
                    logger.logWarn("Failed deleting ECU-TEST log files: " + e.getMessage());
                }
            }
        }
    }
}
