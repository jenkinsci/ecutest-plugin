/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
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
 * Client to import ECU-TEST packages via COM interface.
 */
public class ImportPackageClient extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to work properly.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(2021, 1, 0);

    private final TMSConfig importConfig;

    /**
     * Instantiates a new {@link ImportPackageClient}.
     *
     * @param importConfig the import package configuration
     */
    public ImportPackageClient(final TMSConfig importConfig) {
        this.importConfig = importConfig;
    }

    public TMSConfig getImportConfig() {
        return importConfig;
    }

    /**
     * Imports a package according to given import configuration.
     *
     * @param project   the project
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean importPackage(final Item project, final FilePath workspace, final Launcher launcher,
                                 final TaskListener listener) throws IOException, InterruptedException {
        boolean isImported = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = importConfig.getCredentials(project);
                if (login(credentials, launcher, listener)) {
                    if (importConfig instanceof ImportPackageDirConfig) {
                        isImported = importPackageDirFromTMS(launcher, listener);
                    } else if (importConfig instanceof ImportPackageConfig) {
                        isImported = importPackageFromTMS(launcher, listener);
                    }
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isImported;
    }

    /**
     * Imports a package attributes according to given import configuration.
     *
     * @param project   the project
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean importPackageAttributes(final Item project, final FilePath workspace, final Launcher launcher,
                                           final TaskListener listener) throws IOException, InterruptedException {
        boolean isImported = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = importConfig.getCredentials(project);
                if (login(credentials, launcher, listener)) {
                    isImported = importPackageAttributesFromTMS(launcher, listener);
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isImported;
    }

    /**
     * Imports a package from test management service.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean importPackageFromTMS(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(
            new ImportPackageCallable((ImportPackageConfig) importConfig, listener));
    }

    /**
     * Imports a package directory from test management service.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean importPackageDirFromTMS(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(
            new ImportPackageDirCallable((ImportPackageDirConfig) importConfig, listener));
    }

    /**
     * Imports a package attributes from test management service.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean importPackageAttributesFromTMS(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(
            new ImportPackageAttributeCallable((ImportPackageAttributeConfig) importConfig, listener));
    }

    /**
     * {@link Callable} providing remote access to import a package from test management system via COM.
     */
    private static final class ImportPackageCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportPackageConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportPackageCallable}.
         *
         * @param importConfig the import configuration
         * @param listener     the listener
         */
        ImportPackageCallable(final ImportPackageConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing package %s from test management system...",
                importConfig.getTmsPath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isImported = tm.importPackage(importConfig.getTmsPath(), importConfig.getImportPath(),
                    importConfig.getParsedTimeout())) {
                    logger.logInfo(String.format("-> Package imported successfully to target directory %s.",
                        importConfig.getImportPath()));
                }
            } catch (final ETComException e) {
                logger.logComException("-> Importing package failed", e);
            }
            return isImported;
        }
    }

    /**
     * {@link Callable} providing remote access to import a package directory from test management system via COM.
     */
    private static final class ImportPackageDirCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportPackageDirConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportPackageCallable}.
         *
         * @param importConfig the import configuration
         * @param listener     the listener
         */
        ImportPackageDirCallable(final ImportPackageDirConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing package directory %s from test management system...",
                importConfig.getTmsPath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isImported = tm.importPackageDirectory(importConfig.getTmsPath(), importConfig.getImportPath(),
                    importConfig.getParsedTimeout())) {
                    logger.logInfo(String.format("-> Package directory imported successfully to target directory %s.",
                        importConfig.getImportPath()));
                }
            } catch (final ETComException e) {
                logger.logComException("-> Importing package directory failed", e);
            }
            return isImported;
        }
    }

    /**
     * {@link Callable} providing remote access to import a package attributes from test management system via COM.
     */
    private static final class ImportPackageAttributeCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportPackageAttributeConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportPackageCallable}.
         *
         * @param importConfig the import configuration
         * @param listener     the listener
         */
        ImportPackageAttributeCallable(final ImportPackageAttributeConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing attributes of package %s from test management system...",
                importConfig.getFilePath()));
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isImported = tm.importPackageAttributes(importConfig.getFilePath(),
                    importConfig.getParsedTimeout())) {
                    logger.logInfo("-> Package attributes imported successfully.");
                }
            } catch (final ETComException e) {
                logger.logComException("-> Importing package attributes failed", e);
            }
            return isImported;
        }
    }
}
