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
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Client to start and stop ECU-TEST by either COM or XML-RPC communication.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETClient extends AbstractToolClient {

    private final String workspaceDir;
    private final String settingsDir;
    private final boolean debug;
    private String version;

    /**
     * Instantiates a new {@link ETClient}.
     *
     * @param toolName
     *            the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath
     *            the ECU-TEST install path
     * @param workspaceDir
     *            the ECU-TEST workspace directory
     * @param settingsDir
     *            the ECU-TEST settings directory
     * @param timeout
     *            the timeout to start ECU-TEST
     * @param debug
     *            the debug mode
     */
    public ETClient(final String toolName, final String installPath, final String workspaceDir,
            final String settingsDir, final int timeout, final boolean debug) {
        super(toolName, installPath, timeout);
        this.workspaceDir = StringUtils.trimToEmpty(workspaceDir);
        this.settingsDir = StringUtils.trimToEmpty(settingsDir);
        this.debug = debug;
        version = "";
    }

    /**
     * Instantiates a new {@link ETClient}.
     *
     * @param toolName
     *            the tool name identifying the chosen {@link ETInstallation}.
     * @param timeout
     *            the timeout to start ECU-TEST
     */
    public ETClient(final String toolName, final int timeout) {
        super(toolName, timeout);
        workspaceDir = "";
        settingsDir = "";
        debug = false;
        version = "";
    }

    /**
     * @return the workspace directory
     */
    public String getWorkspaceDir() {
        return workspaceDir;
    }

    /**
     * @return the settings directory
     */
    public String getSettingsDir() {
        return settingsDir;
    }

    /**
     * @return the debug mode
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    @Override
    public boolean start(final boolean checkProcesses, final Launcher launcher, final BuildListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check open processes
        if (checkProcesses) {
            final List<String> foundProcesses = checkProcesses(launcher, true);
            if (!foundProcesses.isEmpty()) {
                logger.logInfo(String.format("Terminated running processes: %s", foundProcesses));
            }
        }

        // Launch ECU-TEST process
        if (!launchProcess(launcher, listener)) {
            return false;
        }

        // Initialize COM connection
        if (!DllUtil.loadLibrary()) {
            logger.logError("Could not load JACOB library!");
            return false;
        }
        final String comVersion = launcher.getChannel().call(new StartCallable(getTimeout(), listener));
        if (comVersion.isEmpty()) {
            logger.logError("Could not determine ECU-TEST version!");
            return false;
        } else {
            version = comVersion;
        }

        // Check ECU-TEST version
        final ToolVersion comToolVersion = ToolVersion.parse(comVersion);
        if (comToolVersion.compareWithoutQualifierTo(ETPlugin.ET_MAX_VERSION) > 0) {
            logger.logWarn(String.format(
                    "The configured ECU-TEST version %s might be incompatible with this plugin. "
                            + "Currently supported versions: %s up to %s", comVersion,
                            ETPlugin.ET_MIN_VERSION.toShortString(), ETPlugin.ET_MAX_VERSION.toShortString()));
        } else if (comToolVersion.compareTo(ETPlugin.ET_MIN_VERSION) < 0) {
            logger.logError(String.format(
                    "The configured ECU-TEST version %s is not compatible with this plugin. "
                            + "Please use at least ECU-TEST %s!", comVersion, ETPlugin.ET_MIN_VERSION.toShortString()));
            // Close ECU-TEST
            stop(checkProcesses, launcher, listener);
            return false;
        }

        return true;
    }

    @Override
    public boolean stop(final boolean checkProcesses, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check open processes
        if (checkProcesses) {
            final List<String> foundProcesses = checkProcesses(launcher, false);
            if (foundProcesses.isEmpty()) {
                logger.logWarn("No running ECU-TEST instance found!");
                return true;
            }
        }

        // Close COM connection and stop ECU-TEST
        if (!DllUtil.loadLibrary()) {
            logger.logError("Could not load JACOB library!");
            return false;
        }
        return launcher.getChannel().call(new StopCallable(getTimeout(), checkProcesses, listener));
    }

    @Override
    public boolean restart(final boolean checkProcesses, final Launcher launcher, final BuildListener listener)
            throws IOException, InterruptedException {
        if (stop(checkProcesses, launcher, listener) && start(checkProcesses, launcher, listener)) {
            return true;
        }
        return false;
    }

    @Override
    protected ArgumentListBuilder createCmdLine() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getInstallPath());

        if (!getWorkspaceDir().isEmpty()) {
            args.add("--workspaceDir", getWorkspaceDir());
            args.add("-s", getSettingsDir());
        }

        if (isDebug()) {
            args.add("-d");
        }

        // Create full workspace automatically
        args.add("--startupAutomated=CreateDirs");

        return args;
    }

    /**
     * Checks already opened ECU-TEST instances.
     *
     * @param launcher
     *            the launcher
     * @param kill
     *            specifies whether to task-kill the running processes
     * @return list of found processes, can be empty but never {@code null}
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    public static List<String> checkProcesses(final Launcher launcher, final boolean kill)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(new CheckProcessCallable(kill));
    }

    /**
     * Closes already opened ECU-TEST instances.
     *
     * @param kill
     *            specifies whether to task-kill the running processes
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if ECU-TEST instance has been stopped successfully
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    public static boolean stopProcesses(final Launcher launcher, final BuildListener listener, final boolean kill)
            throws IOException, InterruptedException {
        return launcher.getChannel().call(new StopCallable(StartETBuilder.DEFAULT_TIMEOUT, kill, listener));
    }

    /**
     * {@link Callable} providing remote access to establish a COM connection.
     */
    private static final class StartCallable implements Callable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final int timeout;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link StartCallable}.
         *
         * @param timeout
         *            the timeout
         * @param listener
         *            the listener
         */
        StartCallable(final int timeout, final BuildListener listener) {
            this.timeout = timeout;
            this.listener = listener;
        }

        @Override
        public String call() throws IOException {
            String version = "";
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            try (ETComClient comClient = new ETComClient(timeout)) {
                if (comClient.isApplicationRunning()) {
                    version = comClient.getVersion();
                }
            } catch (final ETComException e) {
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return version;
        }
    }

    /**
     * {@link Callable} providing remote access to close ECU-TEST via COM.
     */
    private static final class StopCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final int timeout;
        private final boolean checkProcesses;
        private final BuildListener listener;

        /**
         * Instantiates a {@link StopCallable}.
         *
         * @param timeout
         *            the timeout
         * @param checkProcesses
         *            specifies whether to check open processes after closing
         * @param listener
         *            the listener
         */
        StopCallable(final int timeout, final boolean checkProcesses, final BuildListener listener) {
            this.timeout = timeout;
            this.checkProcesses = checkProcesses;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isTerminated = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            try (ETComClient comClient = new ETComClient(timeout)) {
                if (comClient.isApplicationRunning()) {
                    isTerminated = comClient.quit() || comClient.exit();
                } else {
                    logger.logError("ECU-TEST COM instance is not ready to use!");
                }
            } catch (final ETComException e) {
                logger.logError("Caught ComException: " + e.getMessage());
            } finally {
                if (checkProcesses) {
                    final List<String> foundProcesses = ProcessUtil.checkETProcesses(true);
                    if (!foundProcesses.isEmpty()) {
                        logger.logInfo(String.format("Terminated running processes: %s", foundProcesses));
                    }
                }
            }
            return isTerminated;
        }
    }

    /**
     * {@link Callable} providing remote access to check open ECU-TEST processes.
     */
    private static final class CheckProcessCallable implements Callable<List<String>, IOException> {

        private static final long serialVersionUID = 1L;

        private final boolean kill;

        /**
         * Instantiates a new {@link CheckProcessCallable}.
         *
         * @param kill
         *            specifies whether to task-kill running processes
         */
        CheckProcessCallable(final boolean kill) {
            this.kill = kill;
        }

        @Override
        public List<String> call() throws IOException {
            return ProcessUtil.checkETProcesses(kill);
        }
    }
}
