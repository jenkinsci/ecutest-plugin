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
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;

/**
 * The Class JUnitReportGenerator.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitReportGenerator {

    /**
     * Defines the path name containing the UNIT reports inside of the test report directory.
     */
    protected static final String UNIT_TEMPLATE_NAME = "UNIT";

    /**
     * Generates UNIT reports by invoking the startup of ECU-TEST if not already running, otherwise using the current
     * instance without closing when finished.
     *
     * @param installation
     *            the installation
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if generation succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean generate(final AbstractToolInstallation installation, final AbstractBuild<?, ?> build,
            final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        boolean isGenerated = false;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<FilePath> reportFiles = getReportFiles(build, launcher);
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, false);
        final boolean isETRunning = !foundProcesses.isEmpty();

        // Start ECU-TEST if necessary and generate the UNIT reports
        if (isETRunning) {
            isGenerated = generateReports(reportFiles, launcher, listener);
        } else {
            if (installation instanceof ETInstallation) {
                final String toolName = build.getEnvironment(listener).expand(installation.getName());
                final String installPath = installation.getExecutable(launcher);
                final String workspaceDir = getWorkspaceDir(build);
                final String settingsDir = getSettingsDir(build);
                final ETClient etClient = new ETClient(toolName, installPath, workspaceDir, settingsDir,
                        StartETBuilder.DEFAULT_TIMEOUT, false);
                logger.logInfo(String.format("Starting %s...", toolName));
                if (etClient.start(false, launcher, listener)) {
                    logger.logInfo(String.format("%s started successfully.", toolName));
                    isGenerated = generateReports(reportFiles, launcher, listener);
                } else {
                    logger.logError(String.format("Starting %s failed.", toolName));
                }
                logger.logInfo(String.format("Stopping %s...", toolName));
                if (etClient.stop(true, launcher, listener)) {
                    logger.logInfo(String.format("%s stopped successfully.", toolName));
                } else {
                    logger.logError(String.format("Stopping %s failed.", toolName));
                }
            } else {
                logger.logError("Selected ECU-TEST installation is not configured for this node!");
                isGenerated = false;
            }
        }

        return isGenerated;
    }

    /**
     * Generate UNIT reports by calling the {@link GenerateUnitReportCallable}.
     *
     * @param reportFiles
     *            the report files
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if generation succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean generateReports(final List<FilePath> reportFiles, final Launcher launcher,
            final BuildListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("- Generating UNIT test reports...");
        return launcher.getChannel().call(
                new GenerateUnitReportCallable(reportFiles, listener));
    }

    /**
     * Builds a list of report files for UNIT report generation.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @return the list of report files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getReportFiles(final AbstractBuild<?, ?> build, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> reportFiles = new ArrayList<FilePath>();
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath testReportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
            if (testReportDir.exists()) {
                reportFiles.addAll(Arrays.asList(testReportDir.list("**/" + TRFPublisher.TRF_FILE_NAME)));
            }
        }
        Collections.reverse(reportFiles);
        return reportFiles;
    }

    /**
     * Gets the workspace directory, either previous ECU-TEST workspace or default one.
     *
     * @param build
     *            the build
     * @return the workspace directory
     */
    private String getWorkspaceDir(final AbstractBuild<?, ?> build) {
        String workspaceDir = "";
        final ToolEnvInvisibleAction toolEnvAction = build.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspaceDir = toolEnvAction.getToolWorkspace();
        }
        return workspaceDir;
    }

    /**
     * Gets the settings directory, either previous ECU-TEST settings or default one.
     *
     * @param build
     *            the build
     * @return the settings directory
     */
    private String getSettingsDir(final AbstractBuild<?, ?> build) {
        String settingsDir = "";
        final ToolEnvInvisibleAction toolEnvAction = build.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            settingsDir = toolEnvAction.getToolSettings();
        }
        return settingsDir;
    }

    /**
     * {@link Callable} enabling generation of UNIT reports remotely.
     */
    private static final class GenerateUnitReportCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final List<FilePath> dbFiles;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link GenerateUnitReportCallable}.
         *
         * @param dbFiles
         *            the list of TRF files
         * @param listener
         *            the listener
         */
        GenerateUnitReportCallable(final List<FilePath> dbFiles, final BuildListener listener) {
            this.dbFiles = dbFiles;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isGenerated = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            try (ETComClient comClient = new ETComClient()) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                for (final FilePath dbFile : dbFiles) {
                    logger.logInfo(String.format("-> Generating UNIT report: %s", dbFile.getRemote()));
                    final File outDir = new File(dbFile.getParent().getRemote(), UNIT_TEMPLATE_NAME);
                    if (!testEnv.generateTestReportDocumentFromDB(dbFile.getRemote(),
                            outDir.getAbsolutePath(), UNIT_TEMPLATE_NAME, true)) {
                        isGenerated = false;
                        logger.logError("Generating UNIT report failed!");
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
