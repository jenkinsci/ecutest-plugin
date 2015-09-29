/**
 * Copyright (c) 2015 TraceTronic GmbH
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

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
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
     * File name of the TRF file.
     */
    private static final String TRF_NAME = "report.trf";

    /**
     * Generates UNIT reports.
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
        boolean isGenerated = true;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (installation instanceof ETInstallation) {
            final String toolName = installation.getName();
            final String installPath = installation.getExecutable(launcher);
            final String workspace = getWorkspace(build);
            final ETClient etClient = new ETClient(toolName, installPath, workspace,
                    StartETBuilder.DEFAULT_TIMEOUT, false);
            logger.logInfo(String.format("Starting %s...", toolName));
            if (etClient.start(false, launcher, listener)) {
                logger.logInfo(String.format("%s started successfully.", toolName));
                final List<FilePath> reportFiles = getReportFiles(build, launcher);
                logger.logInfo("- Generating UNIT test reports...");
                isGenerated = launcher.getChannel().call(
                        new GenerateUnitReportCallable(reportFiles, listener));
            } else {
                logger.logError(String.format("Starting %s failed.", toolName));
                isGenerated = false;
            }
            logger.logInfo(String.format("Stopping %s...", toolName));
            if (etClient.stop(true, launcher, listener)) {
                logger.logInfo(String.format("%s stopped successfully.", toolName));
            } else {
                logger.logError(String.format("Stopping %s failed.", toolName));
                isGenerated = false;
            }
        } else {
            throw new AbortException(de.tracetronic.jenkins.plugins.ecutest.Messages.ET_NoInstallation());
        }
        return isGenerated;
    }

    /**
     * Builds a list of report files for UNIT report generation.
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
            final FilePath reportFilePath = new FilePath(launcher.getChannel(), new File(
                    testEnvAction.getTestReportDir(), TRF_NAME).getPath());
            if (reportFilePath.exists()) {
                reportFiles.add(reportFilePath);
            }
        }
        return reportFiles;
    }

    /**
     * Gets the workspace, either previous ECU-TEST workspace or default one.
     *
     * @param build
     *            the build
     * @return the workspace
     */
    private String getWorkspace(final AbstractBuild<?, ?> build) {
        String workspace = "";
        final ToolEnvInvisibleAction toolEnvAction = build.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspace = toolEnvAction.getToolWorkspace();
        }
        return workspace;
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
        public GenerateUnitReportCallable(final List<FilePath> dbFiles, final BuildListener listener) {
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
                    logger.logInfo(String.format("-> Generating UNIT report for: %s", dbFile.getRemote()));
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
