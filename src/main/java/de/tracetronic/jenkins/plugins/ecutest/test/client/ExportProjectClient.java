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
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

import java.io.IOException;

import jenkins.security.MasterToSlaveCallable;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;

/**
 * Client to export ECU-TEST projects via COM interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportProjectClient extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to export project.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(6, 5, 0);

    /**
     * Defines the minimum required ECU-TEST version for this client to export project attributes.
     */
    private static final ToolVersion ET_MIN_ATTR_VERSION = new ToolVersion(6, 6, 0);

    private final TMSConfig exportConfig;

    /**
     * Instantiates a new {@link ExportProjectClient}.
     *
     * @param exportConfig
     *            the export configuration
     */
    public ExportProjectClient(final TMSConfig exportConfig) {
        this.exportConfig = exportConfig;
    }

    /**
     * @return the export project configuration
     */
    public TMSConfig getExportConfig() {
        return exportConfig;
    }

    /**
     * Exports a project according to given export configuration.
     *
     * @param project
     *            the project
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean exportProject(final Item project, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        boolean isExported = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = ((ExportProjectConfig) exportConfig)
                        .getCredentials(project);
                if (login(credentials, launcher, listener)) {
                    isExported = exportProjectToTMS(launcher, listener);
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isExported;
    }

    /**
     * Exports project attributes according to given export configuration.
     *
     * @param project
     *            the project
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean exportProjectAttributes(final Item project, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        boolean isExported = false;
        if (isCompatible(ET_MIN_ATTR_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = ((ExportProjectAttributeConfig) exportConfig)
                        .getCredentials(project);
                if (login(credentials, launcher, listener)) {
                    isExported = exportProjectAttributesToTMS(launcher, listener);
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isExported;
    }

    /**
     * Exports a project to test management service.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if export succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean exportProjectToTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ExportProjectCallable((ExportProjectConfig) exportConfig, listener));
    }

    /**
     * Exports project attributes to test management service.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if export succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean exportProjectAttributesToTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ExportProjectAttributeCallable((ExportProjectAttributeConfig) exportConfig, listener));
    }

    /**
     * {@link Callable} providing remote access to export a project to test management system via COM.
     */
    private static final class ExportProjectCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ExportProjectConfig exportConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ExportProjectCallable}.
         *
         * @param exportConfig
         *            the export configuration
         * @param listener
         *            the listener
         */
        ExportProjectCallable(final ExportProjectConfig exportConfig, final TaskListener listener) {
            this.exportConfig = exportConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isExported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Exporting project %s to test management system...",
                    exportConfig.getFilePath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isExported = tm.exportProject(exportConfig.getFilePath(), exportConfig.getExportPath(),
                        exportConfig.isCreateNewPath(), exportConfig.getParsedTimeout())) {
                    logger.logInfo(String.format("-> Project exported successfully to target directory %s.",
                            exportConfig.getExportPath()));
                }
            } catch (final ETComException e) {
                logger.logError("-> Exporting project failed: " + e.getMessage());
            }
            return isExported;
        }
    }

    /**
     * {@link Callable} providing remote access to export project attributes to test management system via COM.
     */
    private static final class ExportProjectAttributeCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ExportProjectAttributeConfig exportConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ExportProjectAttributeCallable}.
         *
         * @param exportConfig
         *            the export configuration
         * @param listener
         *            the listener
         */
        ExportProjectAttributeCallable(final ExportProjectAttributeConfig exportConfig, final TaskListener listener) {
            this.exportConfig = exportConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isExported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Exporting attributes of project %s to test management system...",
                    exportConfig.getFilePath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isExported = tm.exportProjectAttributes(exportConfig.getFilePath(),
                        exportConfig.getParsedTimeout())) {
                    logger.logInfo("-> Project attributes exported successfully.");
                }
            } catch (final ETComException e) {
                logger.logError("-> Exporting project attributes failed: " + e.getMessage());
            }
            return isExported;
        }
    }
}
