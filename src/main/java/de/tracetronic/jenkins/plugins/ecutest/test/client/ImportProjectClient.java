/**
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

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirTMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectTMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;

/**
 * Client to import ECU-TEST projects via COM interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectClient extends TMSClient {

    private final ImportProjectConfig importConfig;

    /**
     * Instantiates a new {@link ImportProjectClient}.
     *
     * @param importConfig
     *            the import configuration
     */
    public ImportProjectClient(final ImportProjectConfig importConfig) {
        this.importConfig = importConfig;
    }

    /**
     * @return the import project configuration
     */
    public ImportProjectConfig getImportConfig() {
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
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Load JACOB library
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }

        boolean isImported = false;
        if (importConfig instanceof ImportProjectArchiveConfig) {
            isImported = importProjectArchive(launcher, listener);
        } else if (importConfig instanceof ImportProjectTMSConfig) {
            if (isTMSAvailable(launcher, listener)) {
                try {
                    final StandardUsernamePasswordCredentials credentials = ((ImportProjectTMSConfig) importConfig)
                            .getCredentials();
                    if (login(credentials, launcher, listener)) {
                        if (importConfig instanceof ImportProjectDirTMSConfig) {
                            isImported = importProjectDirFromTMS(launcher, listener);
                        } else {
                            isImported = importProjectFromTMS(launcher, listener);
                        }
                    }
                } finally {
                    logout(launcher, listener);
                }
            }
        } else {
            logger.logError("Unsupported import configuration of type:" + importConfig.getClass());
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
    public boolean importProjectArchive(final Launcher launcher, final TaskListener listener) throws IOException,
            InterruptedException {
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
    public boolean importProjectFromTMS(final Launcher launcher, final TaskListener listener) throws IOException,
            InterruptedException {
        return launcher.getChannel().call(
                new ImportProjectTMSCallable((ImportProjectTMSConfig) importConfig, listener));
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
    public boolean importProjectDirFromTMS(final Launcher launcher, final TaskListener listener) throws IOException,
            InterruptedException {
        return launcher.getChannel().call(
                new ImportProjectDirTMSCallable((ImportProjectDirTMSConfig) importConfig, listener));
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
            logger.logInfo(String.format("- Importing project from archive %s...", importConfig.getProjectPath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                if (isImported = comClient.importProject(importConfig.getProjectPath(), importConfig.getImportPath(),
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

        private final ImportProjectTMSConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportProjectTMSCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportProjectTMSCallable(final ImportProjectTMSConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing project %s from test management system...",
                    importConfig.getProjectPath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isImported = tm.importProject(importConfig.getProjectPath(), importConfig.getImportPath(),
                        importConfig.getParsedTimeout())) {
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

        private final ImportProjectDirTMSConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportProjectTMSCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportProjectDirTMSCallable(final ImportProjectDirTMSConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing project directory %s from test management system...",
                    importConfig.getProjectPath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                isImported = tm.importProjectDirectory(importConfig.getProjectPath(), importConfig.getImportPath(),
                        importConfig.getParsedTimeout());
                logger.logInfo(String.format("-> Project directory imported successfully to target directory %s.",
                        importConfig.getImportPath()));
            } catch (final ETComException e) {
                logger.logError("-> Importing project directory failed: " + e.getMessage());
            }
            return isImported;
        }
    }
}
