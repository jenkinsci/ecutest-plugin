/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
import hudson.model.TaskListener;
import hudson.remoting.Callable;

import java.io.IOException;

import jenkins.security.MasterToSlaveCallable;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;

/**
 * Client to import ECU-TEST projects via COM interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectClient extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to import project.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(6, 5, 0, 0);

    /**
     * Defines the minimum required ECU-TEST version for this client to import project attributes.
     */
    private static final ToolVersion ET_MIN_ATTR_VERSION = new ToolVersion(6, 6, 0, 0);

    private final TMSConfig importConfig;

    /**
     * Instantiates a new {@link ImportProjectClient}.
     *
     * @param importConfig
     *            the import configuration
     */
    public ImportProjectClient(final TMSConfig importConfig) {
        this.importConfig = importConfig;
    }

    /**
     * @return the import project configuration
     */
    public TMSConfig getImportConfig() {
        return importConfig;
    }

    /**
     * Imports a project according to given import configuration.
     *
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
    public boolean importProject(final FilePath workspace, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        boolean isImported = false;
        if (importConfig instanceof ImportProjectArchiveConfig) {
            isImported = importProjectArchive(launcher, listener);
        } else if (importConfig instanceof ImportProjectConfig
                && isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = ((ImportProjectConfig) importConfig)
                        .getCredentials();
                if (login(credentials, launcher, listener)) {
                    if (importConfig instanceof ImportProjectDirConfig) {
                        isImported = importProjectDirFromTMS(launcher, listener);
                    } else {
                        isImported = importProjectFromTMS(launcher, listener);
                    }
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isImported;
    }

    /**
     * Imports a project according to given import configuration.
     *
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
    public boolean importProjectAttributes(final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        boolean isImported = false;
        if (isCompatible(ET_MIN_ATTR_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = ((ImportProjectAttributeConfig) importConfig)
                        .getCredentials();
                if (login(credentials, launcher, listener)) {
                    isImported = importProjectAttributesFromTMS(launcher, listener);
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isImported;
    }

    /**
     * Imports a project from an archive.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean importProjectArchive(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ImportProjectArchiveCallable((ImportProjectArchiveConfig) importConfig, listener));
    }

    /**
     * Imports a project from test management service.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean importProjectFromTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ImportProjectTMSCallable((ImportProjectConfig) importConfig, listener));
    }

    /**
     * Imports a project directory from test management service.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean importProjectDirFromTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ImportProjectDirTMSCallable((ImportProjectDirConfig) importConfig, listener));
    }

    /**
     * Imports a project attributes from test management service.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if import succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean importProjectAttributesFromTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ImportProjectAttributeCallable((ImportProjectAttributeConfig) importConfig, listener));
    }

    /**
     * {@link Callable} providing remote access to import a project from archive via COM.
     */
    private static final class ImportProjectArchiveCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportProjectArchiveConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportProjectArchiveCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportProjectArchiveCallable(final ImportProjectArchiveConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing project from archive %s...", importConfig.getTmsPath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                if (isImported = comClient.importProject(importConfig.getTmsPath(), importConfig.getImportPath(),
                        importConfig.getImportConfigPath(), importConfig.isReplaceFiles())) {
                    logger.logInfo(String.format("-> Project imported successfully to target directory %s.",
                            importConfig.getImportPath()));
                }
            } catch (final ETComException e) {
                logger.logError("-> Importing project failed: " + e.getMessage());
            }
            return isImported;
        }
    }

    /**
     * {@link Callable} providing remote access to import a project from test management system via COM.
     */
    private static final class ImportProjectTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportProjectConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportProjectTMSCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportProjectTMSCallable(final ImportProjectConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing project %s from test management system...",
                    importConfig.getTmsPath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isImported = tm.importProject(importConfig.getTmsPath(), importConfig.getImportPath(),
                        importConfig.isImportMissingPackages(), importConfig.getParsedTimeout())) {
                    logger.logInfo(String.format("-> Project imported successfully to target directory %s.",
                            importConfig.getImportPath()));
                }
            } catch (final ETComException e) {
                logger.logError("-> Importing project failed: " + e.getMessage());
            }
            return isImported;
        }
    }

    /**
     * {@link Callable} providing remote access to import a project directory from test management system via COM.
     */
    private static final class ImportProjectDirTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportProjectDirConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportProjectTMSCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportProjectDirTMSCallable(final ImportProjectDirConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing project directory %s from test management system...",
                    importConfig.getTmsPath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                isImported = tm.importProjectDirectory(importConfig.getTmsPath(), importConfig.getImportPath(),
                        importConfig.getParsedTimeout());
                logger.logInfo(String.format("-> Project directory imported successfully to target directory %s.",
                        importConfig.getImportPath()));
            } catch (final ETComException e) {
                logger.logError("-> Importing project directory failed: " + e.getMessage());
            }
            return isImported;
        }
    }

    /**
     * {@link Callable} providing remote access to import a project attributes from test management system via COM.
     */
    private static final class ImportProjectAttributeCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportProjectAttributeConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportPackageCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportProjectAttributeCallable(final ImportProjectAttributeConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing attributes of project %s from test management system...",
                    importConfig.getFilePath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                isImported = tm.importProjectAttributes(importConfig.getFilePath(), importConfig.getParsedTimeout());
                logger.logInfo(String.format("-> Project attributes imported successfully.",
                        importConfig.getFilePath()));
            } catch (final ETComException e) {
                logger.logError("-> Importing project attributes failed: " + e.getMessage());
            }
            return isImported;
        }
    }
}
