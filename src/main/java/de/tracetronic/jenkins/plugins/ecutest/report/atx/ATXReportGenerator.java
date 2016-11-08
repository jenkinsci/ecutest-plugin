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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.remoting.Callable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;

/**
 * Class providing the generation of {@link ATXReport}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXReportGenerator extends AbstractATXReportHandler {

    /**
     * Generates {@link ATXReport}s without uploading them.
     *
     * @param archiveTarget
     *            the archive target directory
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param isArchiving
     *            specifies whether archiving artifacts is enabled
     * @param keepAll
     *            specifies whether to keep all artifacts
     * @param installation
     *            the ATX installation
     * @param run
     *            the run
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if upload succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public boolean generate(final FilePath archiveTarget, final boolean allowMissing, final boolean isArchiving,
            final boolean keepAll, final ATXInstallation installation, final Run<?, ?> run, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<FilePath> reportFiles = new ArrayList<FilePath>();
        final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath testReportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
            final FilePath reportFile = AbstractReportPublisher.getFirstReportFile(testReportDir);
            if (reportFile != null && reportFile.exists()) {
                reportFiles.addAll(Arrays.asList(
                        testReportDir.list(TRFPublisher.TRF_INCLUDES, TRFPublisher.TRF_EXCLUDES)));
            } else {
                if (allowMissing) {
                    continue;
                } else {
                    logger.logError(String.format("Specified TRF file '%s' does not exist.", reportFile));
                    return false;
                }
            }
        }

        if (reportFiles.isEmpty() && !allowMissing) {
            logger.logError("Empty test results are not allowed, setting build status to FAILURE!");
            return false;
        }

        // Generate ATX reports
        final boolean isGenerated = launcher.getChannel().call(
                new GenerateReportCallable(installation.getConfig(), reportFiles, run.getEnvironment(listener),
                        listener));

        if (isArchiving) {
            // Removing old artifacts at project level
            if (!reportFiles.isEmpty() && !keepAll) {
                archiveTarget.deleteRecursive();
                AbstractReportPublisher.removePreviousReports(run, ATXBuildAction.class);
            }

            if (isGenerated && !reportFiles.isEmpty()) {
                final List<ATXZipReport> atxReports = new ArrayList<ATXZipReport>();
                logger.logInfo("- Archiving generated ATX reports...");
                int index = 0;
                for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
                    final FilePath testReportDir = new FilePath(launcher.getChannel(),
                            testEnvAction.getTestReportDir());
                    final FilePath archiveTargetDir = archiveTarget.child(testReportDir.getName());
                    try {
                        final int copiedFiles = testReportDir.copyRecursiveTo(
                                String.format("**/%s/*.zip", ATX_TEMPLATE_NAME), archiveTargetDir);
                        logger.logInfo(String.format("-> Archived %d report(s) for %s.", copiedFiles,
                                testEnvAction.getTestName()));
                        if (copiedFiles == 0) {
                            continue;
                        }
                    } catch (final IOException e) {
                        Util.displayIOException(e, listener);
                        logger.logError("Failed archiving generated ATX reports.");
                        return false;
                    }
                    index = traverseReports(atxReports, archiveTargetDir, index);
                }
                addBuildAction(run, atxReports, keepAll);
            }
        } else {
            logger.logInfo("Archiving ATX reports is disabled.");
        }

        return isGenerated;
    }

    /**
     * Creates the main report and adds the sub-reports by traversing them recursively.
     *
     * @param atxReports
     *            the ATX reports
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
    private int traverseReports(final List<ATXZipReport> atxReports, final FilePath archiveTargetDir, int id)
            throws IOException, InterruptedException {
        final FilePath[] zipFiles = archiveTargetDir.list(String.format("%s/%s.zip", ATX_TEMPLATE_NAME,
                archiveTargetDir.getName()));
        if (zipFiles.length == 1) {
            final FilePath zipFile = zipFiles[0];
            final String relFilePath = archiveTargetDir.getParent().toURI().relativize(zipFile.toURI())
                    .getPath();
            final ATXZipReport atxReport = new ATXZipReport(String.format("%d", ++id),
                    zipFile.getBaseName(), relFilePath, zipFile.length());
            atxReports.add(atxReport);

            // Search for sub-reports
            id = traverseSubReports(atxReport, archiveTargetDir.getParent(), archiveTargetDir, id);
        }
        return id;
    }

    /**
     * Builds a list of report files for ATX report generation without upload.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param atxReport
     *            the ATX report
     * @param testReportDir
     *            the main test report directory
     * @param subTestReportDir
     *            the sub test report directory
     * @param id
     *            the id increment
     * @return the current id increment
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private int traverseSubReports(final ATXZipReport atxReport, final FilePath testReportDir,
            final FilePath subTestReportDir, int id) throws IOException, InterruptedException {
        for (final FilePath subDir : subTestReportDir.listDirectories()) {
            final FilePath[] reportFiles = subDir.list(String.format("%s/%s.zip", ATX_TEMPLATE_NAME,
                    subDir.getName()));
            if (reportFiles.length == 1) {
                // Prepare ATX report information for sub-report
                final FilePath reportFile = reportFiles[0];
                final String fileName = reportFile.getBaseName().replaceFirst("^Report\\s", "");
                final String relFilePath = testReportDir.toURI().relativize(reportFile.toURI()).getPath();
                final ATXZipReport subReport = new ATXZipReport(String.format("%d", ++id), fileName, relFilePath,
                        reportFile.length());
                atxReport.addSubReport(subReport);

                // Search for sub-reports
                id = traverseSubReports(subReport, testReportDir, subDir, id);
            }
        }
        return id;
    }

    /**
     * Adds the {@link ATXBuildAction} to the build holding the found {@link ATXZipReport}s.
     *
     * @param run
     *            the run
     * @param atxReports
     *            the list of {@link ATXZipReport}s to add
     * @param keepAll
     *            specifies whether to keep all artifacts
     */
    @SuppressWarnings("unchecked")
    private void addBuildAction(final Run<?, ?> run, final List<ATXZipReport> atxReports, final boolean keepAll) {
        ATXBuildAction<ATXZipReport> action = run.getAction(ATXBuildAction.class);
        if (action == null) {
            action = new ATXBuildAction<ATXZipReport>(!keepAll);
            run.addAction(action);
        }
        action.addAll(atxReports);
    }

    /**
     * {@link Callable} enabling generating ATX reports remotely.
     */
    private static final class GenerateReportCallable extends AbstractReportCallable {

        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new {@link GenerateReportCallable}.
         *
         * @param config
         *            the ATX configuration
         * @param reportFiles
         *            the list of TRF files
         * @param envVars
         *            the environment variables
         * @param listener
         *            the listener
         */
        GenerateReportCallable(final ATXConfig config, final List<FilePath> reportFiles, final EnvVars envVars,
                final TaskListener listener) {
            super(config, reportFiles, envVars, listener);
        }

        @Override
        public Boolean call() throws IOException {
            boolean isGenerated = true;
            final TTConsoleLogger logger = new TTConsoleLogger(getListener());
            final Map<String, String> configMap = getConfigMap(false);
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                final List<FilePath> reportFiles = getReportFiles();
                if (reportFiles.isEmpty()) {
                    logger.logInfo("-> No report files found to generate!");
                } else {
                    for (final FilePath reportFile : reportFiles) {
                        logger.logInfo(String.format("-> Generating ATX report: %s", reportFile.getRemote()));
                        final FilePath outDir = reportFile.getParent().child(ATX_TEMPLATE_NAME);
                        testEnv.generateTestReportDocumentFromDB(reportFile.getRemote(),
                                outDir.getRemote(), ATX_TEMPLATE_NAME, true, configMap);
                        comClient.waitForIdle(0);
                    }
                }
            } catch (final ETComException e) {
                isGenerated = false;
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isGenerated;
        }
    }
}
