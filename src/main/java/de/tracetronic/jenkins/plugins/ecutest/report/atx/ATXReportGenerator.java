/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class providing the generation of {@link ATXReport}s.
 */
public class ATXReportGenerator extends AbstractATXReportHandler {

    /**
     * Instantiates a new {@code ATXReportGenerator}.
     *
     * @param installation the ATX installation
     */
    public ATXReportGenerator(final ATXInstallation installation) {
        super(installation);
    }

    /**
     * Generates {@link ATXReport}s without uploading them.
     *
     * @param archiveTarget        the archive target directory
     * @param reportDirs           the report directories
     * @param usePersistedSettings specifies whether to use read settings from persisted configurations file
     * @param injectBuildVars      specifies whether to inject common build variables as ATX constants
     * @param allowMissing         specifies whether missing reports are allowed
     * @param isArchiving          specifies whether archiving artifacts is enabled
     * @param keepAll              specifies whether to keep all artifacts
     * @param run                  the run
     * @param launcher             the launcher
     * @param listener             the listener
     * @return {@code true} if upload succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:npathcomplexity"})
    public boolean generate(final FilePath archiveTarget, final List<FilePath> reportDirs,
                            final boolean usePersistedSettings, final boolean injectBuildVars,
                            final boolean allowMissing, final boolean isArchiving, final boolean keepAll,
                            final Run<?, ?> run, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<FilePath> reportFiles = new ArrayList<>();
        for (final FilePath reportDir : reportDirs) {
            final FilePath reportFile = AbstractReportPublisher.getFirstReportFile(reportDir);
            if (reportFile != null && reportFile.exists()) {
                reportFiles.addAll(Arrays.asList(
                        reportDir.list(TRFPublisher.TRF_INCLUDES, TRFPublisher.TRF_EXCLUDES)));
            } else {
                if (!allowMissing) {
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
                new GenerateReportCallable(getInstallation().getConfig(), reportFiles,
                        usePersistedSettings, injectBuildVars, run.getEnvironment(listener), listener));

        if (isArchiving) {
            // Removing old artifacts at project level
            if (!reportFiles.isEmpty() && !keepAll) {
                archiveTarget.deleteRecursive();
                AbstractReportPublisher.removePreviousReports(run, ATXBuildAction.class);
            }
            if (isGenerated && !reportFiles.isEmpty()) {
                final List<ATXZipReport> atxReports = new ArrayList<>();
                logger.logInfo("- Archiving generated ATX reports...");
                for (final FilePath reportDir : reportDirs) {
                    final FilePath archiveTargetDir = archiveTarget.child(reportDir.getName());
                    try {
                        final int copiedFiles = reportDir.copyRecursiveTo(
                            String.format("**/%s/*.zip", ATX_TEMPLATE_NAME), archiveTargetDir);
                        logger.logInfo(String.format("-> Archived %d report(s).", copiedFiles));
                        if (copiedFiles == 0) {
                            continue;
                        }
                    } catch (final IOException e) {
                        Util.displayIOException(e, listener);
                        logger.logError("Failed archiving generated ATX reports.");
                        return false;
                    }
                    traverseReports(atxReports, archiveTargetDir);
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
     * @param atxReports       the ATX reports
     * @param archiveTargetDir the archive target directory
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private void traverseReports(final List<ATXZipReport> atxReports, final FilePath archiveTargetDir)
        throws IOException, InterruptedException {
        final FilePath[] zipFiles = archiveTargetDir.list(String.format("%s/%s.zip", ATX_TEMPLATE_NAME,
            archiveTargetDir.getName()));
        if (zipFiles.length == 1) {
            final FilePath zipFile = zipFiles[0];
            final String relFilePath = archiveTargetDir.getParent().toURI().relativize(zipFile.toURI()).getPath();
            final ATXZipReport atxReport = new ATXZipReport(AbstractReportPublisher.randomId(),
                zipFile.getBaseName(), relFilePath, zipFile.length());
            atxReports.add(atxReport);

            // Search for sub-reports
            traverseSubReports(atxReport, archiveTargetDir.getParent(), archiveTargetDir);
        }
    }

    /**
     * Builds a list of report files for ATX report generation without upload.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param atxReport        the ATX report
     * @param testReportDir    the main test report directory
     * @param subTestReportDir the sub test report directory
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private void traverseSubReports(final ATXZipReport atxReport, final FilePath testReportDir,
                                    final FilePath subTestReportDir) throws IOException, InterruptedException {
        for (final FilePath subDir : subTestReportDir.listDirectories()) {
            final FilePath[] reportFiles = subDir.list(String.format("%s/%s.zip", ATX_TEMPLATE_NAME,
                subDir.getName()));
            if (reportFiles.length == 1) {
                // Prepare ATX report information for sub-report
                final FilePath reportFile = reportFiles[0];
                final String fileName = reportFile.getBaseName().replaceFirst("^Report\\s", "");
                final String relFilePath = testReportDir.toURI().relativize(reportFile.toURI()).getPath();
                final ATXZipReport subReport = new ATXZipReport(AbstractReportPublisher.randomId(),
                    fileName, relFilePath, reportFile.length());
                atxReport.addSubReport(subReport);

                // Search for sub-reports
                traverseSubReports(subReport, testReportDir, subDir);
            }
        }
    }

    /**
     * Adds the {@link ATXBuildAction} to the build holding the found {@link ATXZipReport}s.
     *
     * @param run        the run
     * @param atxReports the list of {@link ATXZipReport}s to add
     * @param keepAll    specifies whether to keep all artifacts
     */
    @SuppressWarnings("unchecked")
    private void addBuildAction(final Run<?, ?> run, final List<ATXZipReport> atxReports, final boolean keepAll) {
        ATXBuildAction<ATXZipReport> action = run.getAction(ATXBuildAction.class);
        if (action == null) {
            action = new ATXBuildAction<>(!keepAll);
            run.addAction(action);
        }
        action.addAll(atxReports);
    }

    /**
     * {@link Callable} enabling generating ATX reports remotely.
     */
    private static final class GenerateReportCallable extends AbstractReportCallable<Boolean> {

        private static final long serialVersionUID = 1L;

        private final boolean usePersistedSettings;
        private final boolean injectBuildVars;

        /**
         * Instantiates a new {@link GenerateReportCallable}.
         *
         * @param config               the ATX configuration
         * @param reportFiles          the list of TRF files
         * @param usePersistedSettings specifies whether to use read settings from persisted configurations file
         * @param injectBuildVars      specifies whether to inject common build variables as ATX constants
         * @param envVars              the environment variables
         * @param listener             the listener
         */
        GenerateReportCallable(final ATXConfig config, final List<FilePath> reportFiles,
                               final boolean usePersistedSettings, final boolean injectBuildVars,
                               final EnvVars envVars, final TaskListener listener) {
            super(config, reportFiles, envVars, listener);
            this.usePersistedSettings = usePersistedSettings;
            this.injectBuildVars = injectBuildVars;
        }

        @Override
        public Boolean call() {
            boolean isGenerated = true;
            final TTConsoleLogger logger = new TTConsoleLogger(getListener());
            final Map<String, String> configMap = getConfigMap(false, injectBuildVars);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                final List<FilePath> reportFiles = getReportFiles();
                if (reportFiles.isEmpty()) {
                    logger.logInfo("-> No report files found to generate!");
                } else {
                    for (final FilePath reportFile : reportFiles) {
                        logger.logInfo(String.format("-> Generating ATX report: %s", reportFile.getRemote()));
                        final FilePath outDir = reportFile.getParent().child(ATX_TEMPLATE_NAME);
                        if (usePersistedSettings) {
                            final FilePath reportDir = reportFile.getParent();
                            final FilePath configPath = reportDir.child(ATX_TEMPLATE_NAME + ".xml");
                            logger.logInfo(String.format("- Using persisted settings from configuration: %s",
                                    configPath.getRemote()));
                            testEnv.generateTestReportDocument(reportFile.getRemote(),
                                    reportDir.getRemote(), configPath.getRemote(), true);
                        } else {
                            testEnv.generateTestReportDocumentFromDB(reportFile.getRemote(),
                                    outDir.getRemote(), ATX_TEMPLATE_NAME, true, configMap);
                        }
                        comClient.waitForIdle(0);
                    }
                }
            } catch (final ETComException e) {
                isGenerated = false;
                logger.logComException(e);
            }
            return isGenerated;
        }
    }
}
