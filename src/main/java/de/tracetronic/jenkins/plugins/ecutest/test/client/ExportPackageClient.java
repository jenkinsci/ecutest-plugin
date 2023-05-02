/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
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
 * Client to export ECU-TEST packages via COM interface.
 */
public class ExportPackageClient extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to work properly.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(2021, 1, 0);

    private final TMSConfig exportConfig;

    /**
     * Instantiates a new {@link ExportPackageClient}.
     *
     * @param exportConfig the export configuration
     */
    public ExportPackageClient(final TMSConfig exportConfig) {
        this.exportConfig = exportConfig;
    }

    public TMSConfig getExportConfig() {
        return exportConfig;
    }

    /**
     * Exports a package according to given export configuration.
     *
     * @param project   the project
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean exportPackage(final Item project, final FilePath workspace, final Launcher launcher,
                                 final TaskListener listener) throws IOException, InterruptedException {
        boolean isExported = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = exportConfig.getCredentials(project);
                if (login(credentials, launcher, listener)) {
                    isExported = exportPackageToTMS(launcher, listener);
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isExported;
    }

    /**
     * Exports package attributes according to given export configuration.
     *
     * @param project   the project
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean exportPackageAttributes(final Item project, final FilePath workspace, final Launcher launcher,
                                           final TaskListener listener) throws IOException, InterruptedException {
        boolean isExported = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = exportConfig.getCredentials(project);
                if (login(credentials, launcher, listener)) {
                    isExported = exportPackageAttributesToTMS(launcher, listener);
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isExported;
    }

    /**
     * Exports a package to test management service.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if export succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean exportPackageToTMS(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(
            new ExportPackageCallable((ExportPackageConfig) exportConfig, listener));
    }

    /**
     * Exports package attributes to test management service.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if export succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean exportPackageAttributesToTMS(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(
            new ExportPackageAttributeCallable((ExportPackageAttributeConfig) exportConfig, listener));
    }

    /**
     * {@link Callable} providing remote access to export a package to test management system via COM.
     */
    private static final class ExportPackageCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ExportPackageConfig exportConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ExportPackageCallable}.
         *
         * @param exportConfig the export configuration
         * @param listener     the listener
         */
        ExportPackageCallable(final ExportPackageConfig exportConfig, final TaskListener listener) {
            this.exportConfig = exportConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isExported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Exporting package %s to test management system...",
                exportConfig.getFilePath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isExported = tm.exportPackage(exportConfig.getFilePath(), exportConfig.getExportPath(),
                    exportConfig.isCreateNewPath(), exportConfig.getParsedTimeout())) {
                    logger.logInfo(String.format("-> Package exported successfully to target directory %s.",
                        exportConfig.getExportPath()));
                }
            } catch (final ETComException e) {
                logger.logComException("-> Exporting package failed", e);
            }
            return isExported;
        }
    }

    /**
     * {@link Callable} providing remote access to export package attributes to test management system via COM.
     */
    private static final class ExportPackageAttributeCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ExportPackageAttributeConfig exportConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ExportPackageAttributeCallable}.
         *
         * @param exportConfig the export configuration
         * @param listener     the listener
         */
        ExportPackageAttributeCallable(final ExportPackageAttributeConfig exportConfig, final TaskListener listener) {
            this.exportConfig = exportConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isExported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Exporting attributes of package %s to test management system...",
                exportConfig.getFilePath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isExported = tm.exportPackageAttributes(exportConfig.getFilePath(),
                    exportConfig.getParsedTimeout())) {
                    logger.logInfo("-> Package attributes exported successfully.");
                }
            } catch (final ETComException e) {
                logger.logComException("-> Exporting package attributes failed", e);
            }
            return isExported;
        }
    }
}
