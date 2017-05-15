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

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirTMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageTMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;

/**
 * Client to import ECU-TEST packages via COM interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportPackageClient extends AbstractTMSClient {

    private final ImportPackageConfig importConfig;

    /**
     * Instantiates a new {@link ImportPackageClient}.
     *
     * @param importConfig
     *            the import configuration
     */
    public ImportPackageClient(final ImportPackageConfig importConfig) {
        this.importConfig = importConfig;
    }

    /**
     * @return the import package configuration
     */
    public ImportPackageConfig getImportConfig() {
        return importConfig;
    }

    /**
     * Imports a package according to given import configuration.
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
    public boolean importPackage(final FilePath workspace, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Load JACOB library
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }

        boolean isImported = false;
        if (isTMSAvailable(launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = ((ImportPackageTMSConfig) importConfig)
                        .getCredentials();
                if (login(credentials, launcher, listener)) {
                    if (importConfig instanceof ImportPackageDirTMSConfig) {
                        isImported = importPackageDirFromTMS(launcher, listener);
                    } else {
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
     * Imports a package from test management service.
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
    public boolean importPackageFromTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ImportPackageTMSCallable((ImportPackageTMSConfig) importConfig, listener));
    }

    /**
     * Imports a package directory from test management service.
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
    public boolean importPackageDirFromTMS(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(
                new ImportPackageDirTMSCallable((ImportPackageDirTMSConfig) importConfig, listener));
    }

    /**
     * {@link Callable} providing remote access to import a package from test management system via COM.
     */
    private static final class ImportPackageTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportPackageTMSConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportPackageTMSCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportPackageTMSCallable(final ImportPackageTMSConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing package %s from test management system...",
                    importConfig.getPackagePath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isImported = tm.importPackage(importConfig.getPackagePath(), importConfig.getImportPath(),
                        importConfig.getParsedTimeout())) {
                    logger.logInfo(String.format("-> Package imported successfully to target directory %s.",
                            importConfig.getImportPath()));
                }
            } catch (final ETComException e) {
                logger.logError("-> Importing package failed: " + e.getMessage());
            }
            return isImported;
        }
    }

    /**
     * {@link Callable} providing remote access to import a package directory from test management system via COM.
     */
    private static final class ImportPackageDirTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ImportPackageDirTMSConfig importConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ImportPackageTMSCallable}.
         *
         * @param importConfig
         *            the import configuration
         * @param listener
         *            the listener
         */
        ImportPackageDirTMSCallable(final ImportPackageDirTMSConfig importConfig, final TaskListener listener) {
            this.importConfig = importConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isImported = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("- Importing package directory %s from test management system...",
                    importConfig.getPackagePath()));
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                isImported = tm.importPackageDirectory(importConfig.getPackagePath(), importConfig.getImportPath(),
                        importConfig.getParsedTimeout());
                logger.logInfo(String.format("-> Package directory imported successfully to target directory %s.",
                        importConfig.getImportPath()));
            } catch (final ETComException e) {
                logger.logError("-> Importing package directory failed: " + e.getMessage());
            }
            return isImported;
        }
    }
}
