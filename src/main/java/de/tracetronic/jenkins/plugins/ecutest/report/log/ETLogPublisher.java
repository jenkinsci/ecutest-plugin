/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
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

    private final boolean unstableOnWarning;
    private final boolean failedOnError;

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
     */
    @DataBoundConstructor
    public ETLogPublisher(final boolean allowMissing, final boolean runOnFailed,
            final boolean unstableOnWarning, final boolean failedOnError) {
        super(allowMissing, runOnFailed);
        this.unstableOnWarning = unstableOnWarning;
        this.failedOnError = failedOnError;
    }

    /**
     * @return the unstableOnWarning
     */
    public boolean isUnstableOnWarning() {
        return unstableOnWarning;
    }

    /**
     * @return the failedOnError
     */
    public boolean isFailedOnError() {
        return failedOnError;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing ECU-TEST logs...");

        final Result buildResult = build.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            logger.logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return true;
        }

        final List<ETLogReport> logReports = new ArrayList<ETLogReport>();
        final FilePath archiveTargetDir = getArchiveTarget(build);
        final List<FilePath> logFiles = getLogFiles(build, launcher);
        for (final FilePath logFile : logFiles) {
            final FilePath targetFile = archiveTargetDir.child(logFile.getName());
            try {
                if (logFile.exists()) {
                    logger.logInfo(String.format("- Archiving %s", logFile));
                    logFile.copyTo(targetFile);
                } else {
                    if (isAllowMissing()) {
                        continue;
                    } else {
                        logger.logError(String.format("Specified ECU-TEST log file '%s' does not exist.",
                                logFile));
                        build.setResult(Result.FAILURE);
                        return true;
                    }
                }
            } catch (final IOException e) {
                Util.displayIOException(e, listener);
                logger.logError("Failed publishing ECU-TEST logs.");
                build.setResult(Result.FAILURE);
                return true;
            }

            final ETLogReport logReport = parseLogFile(logFile, logReports.size() + 1);
            logReports.add(logReport);
        }

        if (logReports.isEmpty()) {
            logger.logInfo("No log results found.");
            if (!isAllowMissing()) {
                logger.logError("Empty log results are not allowed, setting build status to FAILURE!");
                build.setResult(Result.FAILURE);
                return true;
            }
        } else {
            addBuildAction(build, logReports);
            setBuildResult(build, listener, logReports);
        }

        logger.logInfo("ECU-TEST logs published successfully.");
        return true;
    }

    /**
     * Parses the ECU-TEST log file.
     *
     * @param logFile
     *            the log file
     * @param id
     *            the report id
     * @return the parsed {@link ETLogReport}
     * @throws IOException
     *             signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private ETLogReport parseLogFile(final FilePath logFile, final int id) throws IOException, InterruptedException {
        final ETLogParser logParser = new ETLogParser(logFile);
        final List<ETLogAnnotation> logs = logParser.parse();
        final int warningLogCount = logParser.parseLogCount(Severity.WARNING);
        final int errorLogCount = logParser.parseLogCount(Severity.ERROR);

        final ETLogReport logReport = new ETLogReport(String.format("%d", id), logFile.getName(), logFile.getName(),
                logFile.length(), logs, warningLogCount, errorLogCount);
        return logReport;
    }

    /**
     * Adds the {@link ETLogBuildAction} to the build holding the found {@link ETLogReport}s.
     *
     * @param build
     *            the build
     * @param logReports
     *            the list of {@link ETLogReport}s to add
     */
    private void addBuildAction(final AbstractBuild<?, ?> build, final List<ETLogReport> logReports) {
        ETLogBuildAction action = build.getAction(ETLogBuildAction.class);
        if (action == null) {
            action = new ETLogBuildAction();
            build.addAction(action);
        }
        action.addAll(logReports);
    }

    /**
     * Sets the build result in case of errors or warnings.
     *
     * @param build
     *            the build
     * @param listener
     *            the listener
     * @param logReports
     *            the log reports
     */
    private void setBuildResult(final AbstractBuild<?, ?> build, final BuildListener listener,
            final List<ETLogReport> logReports) {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        int totalWarnings = 0;
        int totalErrors = 0;
        for (final ETLogReport logReport : logReports) {
            totalWarnings += logReport.getWarningLogCount();
            totalErrors += logReport.getErrorLogCount();
        }
        logger.logInfo("- Parsing log files...");
        if (totalErrors > 0 && isFailedOnError()) {
            logger.logInfo(String.format(
                    "-> %d error(s) found in the ECU-TEST logs, setting build status to FAILURE!",
                    totalErrors));
            build.setResult(Result.FAILURE);
        } else if (totalWarnings > 0 && isUnstableOnWarning()) {
            logger.logInfo(String.format(
                    "-> %d warning(s) found in the ECU-TEST logs, setting build status to UNSTABLE!",
                    totalWarnings));
            build.setResult(Result.UNSTABLE);
        } else {
            logger.logInfo(String.format("-> %d warning(s) and %d error(s) found in the ECU-TEST logs.",
                    totalWarnings, totalErrors));
        }
    }

    /**
     * Gets the archive target.
     *
     * @param build
     *            the build
     * @return the archive target
     */
    private FilePath getArchiveTarget(final AbstractBuild<?, ?> build) {
        return new FilePath(new File(build.getRootDir(), URL_NAME));
    }

    /**
     * Builds a list of ECU-TEST log files for archiving.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @return the list of log files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getLogFiles(final AbstractBuild<?, ?> build, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> archiveFiles = new ArrayList<FilePath>();
        FilePath workspace;
        final ToolEnvInvisibleAction toolEnvAction = build.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspace = new FilePath(launcher.getChannel(), toolEnvAction.getToolSettings());
        } else {
            workspace = build.getWorkspace();
        }
        if (workspace != null && workspace.exists()) {
            final String includes = String.format("%s,%s", INFO_LOG_NAME, ERROR_LOG_NAME);
            for (final String includeFile : workspace.act(new ListFilesCallable(includes, ""))) {
                final FilePath archiveFile = new FilePath(launcher.getChannel(), includeFile);
                archiveFiles.add(archiveFile);
            }
        }
        return archiveFiles;
    }

    /**
     * {@link FileCallable} providing remote file access to list included files.
     */
    private static final class ListFilesCallable implements FilePath.FileCallable<List<String>> {

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
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new ETLogProjectAction();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * DescriptorImpl for {@link ETLogPublisher}.
     */
    @Extension(ordinal = 1000)
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

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
                    if (!(listener instanceof BuildListener)) {
                        throw new AssertionError("Unexpected type: " + listener);
                    }
                    final TTConsoleLogger logger = new TTConsoleLogger((BuildListener) listener);
                    logger.logWarn("Failed deleting ECU-TEST log files: " + e.getMessage());
                }
            }
        }
    }
}
