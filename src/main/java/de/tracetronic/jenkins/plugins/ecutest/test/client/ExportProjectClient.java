/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;

/**
 * Client to export ECU-TEST projects via COM interface.
 */
public class ExportProjectClient extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to export project.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(2021, 1, 0);

    /**
     * Defines the minimum required ECU-TEST version for this client to export project attributes.
     */
    private static final ToolVersion ET_MIN_ATTR_VERSION = new ToolVersion(2021, 1, 0);

    private final TMSConfig exportConfig;

    /**
     * Instantiates a new {@link ExportProjectClient}.
     *
     * @param exportConfig the export project configuration
     */
    public ExportProjectClient(final TMSConfig exportConfig) {
        this.exportConfig = exportConfig;
    }

    public TMSConfig getExportConfig() {
        return exportConfig;
    }

    /**
     * Exports a project according to given export configuration.
     *
     * @param project   the project
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean exportProject(final Item project, final FilePath workspace, final Launcher launcher,
                                 final TaskListener listener) throws IOException, InterruptedException {
        boolean isExported = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = exportConfig.getCredentials(project);
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
     * @param project   the project
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean exportProjectAttributes(final Item project, final FilePath workspace, final Launcher launcher,
                                           final TaskListener listener) throws IOException, InterruptedException {
        boolean isExported = false;
        if (isCompatible(ET_MIN_ATTR_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = exportConfig.getCredentials(project);
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
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if export succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean exportProjectToTMS(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(
            new ExportProjectCallable((ExportProjectConfig) exportConfig, listener));
    }

    /**
     * Exports project attributes to test management service.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if export succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
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
         * @param exportConfig the export configuration
         * @param listener     the listener
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
                logger.logComException("-> Exporting project failed", e);
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
         * @param exportConfig the export configuration
         * @param listener     the listener
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
                logger.logComException("-> Exporting project attributes failed", e);
            }
            return isExported;
        }
    }
}
