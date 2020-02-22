/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETComRegisterClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;
import java.util.List;

/**
 * Class providing the generation of JUnit reports with ECU-TEST.
 */
public class JUnitReportGenerator {

    /**
     * Generates UNIT reports by invoking the startup of ECU-TEST if not already running, otherwise using the current
     * instance without closing when finished.
     *
     * @param installation the installation
     * @param reportFiles  the report files
     * @param run          the run
     * @param workspace    the workspace
     * @param launcher     the launcher
     * @param listener     the listener
     * @return {@code true} if generation succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean generate(final ETInstallation installation, final List<FilePath> reportFiles,
                            final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                            final TaskListener listener) throws IOException, InterruptedException {
        boolean isGenerated = false;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        final boolean isETRunning = !foundProcesses.isEmpty();

        // Start ECU-TEST if necessary and generate the UNIT reports
        if (isETRunning) {
            isGenerated = generateReports(reportFiles, launcher, listener);
        } else {
            // Register ECU-TEST COM server
            final String toolName = run.getEnvironment(listener).expand(installation.getName());
            if (installation.isRegisterComServer()) {
                final String installPath = installation.getComExecutable(launcher);
                ETComRegisterClient comClient = new ETComRegisterClient(toolName, installPath);
                comClient.start(false, workspace, launcher, listener);
            }

            final String installPath = installation.getExecutable(launcher);
            final String workspaceDir = getWorkspaceDir(run);
            final String settingsDir = getSettingsDir(run);
            final ETClient etClient = new ETClient(toolName, installPath, workspaceDir, settingsDir,
                StartETBuilder.DEFAULT_TIMEOUT, false);
            if (etClient.start(false, workspace, launcher, listener)) {
                isGenerated = generateReports(reportFiles, launcher, listener);
            } else {
                logger.logError(String.format("Starting %s failed.", toolName));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", toolName));
            }
        }

        return isGenerated;
    }

    /**
     * Generate UNIT reports by calling the {@link GenerateUnitReportCallable}.
     *
     * @param reportFiles the report files
     * @param launcher    the launcher
     * @param listener    the listener
     * @return {@code true} if generation succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean generateReports(final List<FilePath> reportFiles, final Launcher launcher,
                                    final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("- Generating UNIT test reports...");
        return launcher.getChannel().call(
            new GenerateUnitReportCallable(reportFiles, listener));
    }

    /**
     * Gets the workspace directory, either previous ECU-TEST workspace or default one.
     *
     * @param run the run
     * @return the workspace directory
     */
    private String getWorkspaceDir(final Run<?, ?> run) {
        String workspaceDir = "";
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspaceDir = toolEnvAction.getToolWorkspace();
        }
        return workspaceDir;
    }

    /**
     * Gets the settings directory, either previous ECU-TEST settings or default one.
     *
     * @param run the run
     * @return the settings directory
     */
    private String getSettingsDir(final Run<?, ?> run) {
        String settingsDir = "";
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            settingsDir = toolEnvAction.getToolSettings();
        }
        return settingsDir;
    }

    /**
     * {@link Callable} enabling generation of UNIT reports remotely.
     */
    private static final class GenerateUnitReportCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final List<FilePath> dbFiles;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link GenerateUnitReportCallable}.
         *
         * @param dbFiles  the list of TRF files
         * @param listener the listener
         */
        GenerateUnitReportCallable(final List<FilePath> dbFiles, final TaskListener listener) {
            this.dbFiles = dbFiles;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isGenerated = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                for (final FilePath dbFile : dbFiles) {
                    logger.logInfo(String.format("-> Generating UNIT report: %s", dbFile.getRemote()));
                    final FilePath outDir = dbFile.getParent().child(JUnitPublisher.UNIT_TEMPLATE_NAME);
                    if (!testEnv.generateTestReportDocumentFromDB(dbFile.getRemote(),
                        outDir.getRemote(), JUnitPublisher.UNIT_TEMPLATE_NAME, true)) {
                        isGenerated = false;
                        logger.logError("Generating UNIT report failed!");
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
