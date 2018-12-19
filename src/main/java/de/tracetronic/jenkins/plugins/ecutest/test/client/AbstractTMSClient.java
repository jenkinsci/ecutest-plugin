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

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;

/**
 * Abstract client providing common used functions to interact with a test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractTMSClient {

    /**
     * Logs in to preconfigured test management service in ECU-TEST.
     *
     * @param credentials
     *            the credentials
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if login succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean login(final StandardUsernamePasswordCredentials credentials, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        return launcher.getChannel().call(new LoginTMSCallable(credentials, listener));
    }

    /**
     * Logs out from preconfigured test management service in ECU-TEST.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true}, if logout succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean logout(final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(new LogoutTMSCallable(listener));
    }

    /**
     * Checks the currently running ECU-TEST version for compatibility reasons and
     * tests whether the test management module is available.
     *
     * @param minVersion
     *            the minimum required ECU-TEST version
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if compatible, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    protected boolean isCompatible(final ToolVersion minVersion, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        // Load JACOB library
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }
        return launcher.getChannel().call(new CompatibleTMSCallable(minVersion, listener));
    }

    /**
     * {@link Callable} providing remote access to determine whether the test management module is available in
     * ECU-TEST.
     */
    private static final class CompatibleTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;
        private final ToolVersion minVersion;

        /**
         * Instantiates a {@link CompatibleTMSCallable}.
         *
         * @param minVersion
         *            the minimum required ECU-TEST version
         * @param listener
         *            the listener
         */
        CompatibleTMSCallable(final ToolVersion minVersion, final TaskListener listener) {
            this.listener = listener;
            this.minVersion = minVersion;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isAvailable = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();

            // Check ECU-TEST version and TMS module
            try (ETComClient comClient = new ETComClient(progId)) {
                final String comVersion = comClient.getVersion();
                final ToolVersion comToolVersion = ToolVersion.parse(comVersion);
                if (comToolVersion.compareTo(minVersion) < 0) {
                    logger.logError(String.format(
                            "The configured ECU-TEST version %s does not support this test management method. "
                                    + "Please use at least ECU-TEST %s!", comVersion, minVersion.toMicroString()));
                } else if (comClient.getTestManagement() != null) {
                    isAvailable = true;
                }
            } catch (final ETComException e) {
                logger.logError("The test management module is not available in running ECU-TEST instance! "
                        + "Enable it by setting the feature flag 'TEST-MANAGEMENT-SERVICE'.");
                logger.logComException(e.getMessage());
            }
            return isAvailable;
        }
    }

    /**
     * {@link Callable} providing remote access to log in to test management system via COM.
     */
    private static final class LoginTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final StandardUsernamePasswordCredentials credentials;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link LoginTMSCallable}.
         *
         * @param credentials
         *            the credentials
         * @param listener
         *            the listener
         */
        LoginTMSCallable(final StandardUsernamePasswordCredentials credentials, final TaskListener listener) {
            this.credentials = credentials;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isLogin = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Log in to test management system...");
            if (credentials == null) {
                logger.logError("-> No credentials provided!");
            } else {
                final String progId = ETComProperty.getInstance().getProgId();
                try (ETComClient comClient = new ETComClient(progId)) {
                    final TestManagement tm = (TestManagement) comClient.getTestManagement();
                    logger.logInfo("-- Authenticating with user name: " + credentials.getUsername());
                    if (isLogin = tm.login(credentials.getUsername(), credentials.getPassword().getPlainText())) {
                        logger.logInfo("-> Logged in successfully.");
                    } else {
                        logger.logError("-> Login failed due to invalid credentials!");
                    }
                } catch (final ETComException e) {
                    logger.logError("-> Login failed: " + e.getMessage());
                }
            }
            return isLogin;
        }
    }

    /**
     * {@link Callable} providing remote access to log out from test management system via COM.
     */
    private static final class LogoutTMSCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;

        /**
         * Instantiates a new {@link LogoutTMSCallable}.
         *
         * @param listener
         *            the listener
         */
        LogoutTMSCallable(final TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isLogout = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Log out from test management system...");
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                if (isLogout = tm.logout()) {
                    logger.logInfo("-> Logged out successfully.");
                } else {
                    logger.logError("-> Logout failed!");
                }
            } catch (final ETComException e) {
                logger.logError("-> Logout failed: " + e.getMessage());
            }
            return isLogout;
        }
    }
}
