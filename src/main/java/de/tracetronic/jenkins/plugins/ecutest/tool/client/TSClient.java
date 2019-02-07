/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.util.ArgumentListBuilder;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * Client to start and stop the Tool-Server via command line execution.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TSClient extends AbstractToolClient {

    /**
     * Defines the default port used for TCP communication with the Tool-Server.
     */
    public static final int DEFAULT_TCP_PORT = 5017;

    private final String toolLibsIniPath;
    private final int tcpPort;

    /**
     * Instantiates a new {@link TSClient}.
     *
     * @param toolName        the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath     the Tool-Server install path
     * @param timeout         the timeout
     * @param toolLibsIniPath the alternative ToolLibs.ini path
     * @param tcpPort         the alternative TCP port
     */
    public TSClient(final String toolName, final String installPath, final int timeout, final String toolLibsIniPath,
                    final int tcpPort) {
        super(toolName, installPath, timeout);
        this.toolLibsIniPath = StringUtils.trimToEmpty(toolLibsIniPath);
        this.tcpPort = tcpPort == 0 ? DEFAULT_TCP_PORT : tcpPort;
    }

    /**
     * Instantiates a new {@link TSClient}.
     *
     * @param toolName the tool name identifying the chosen {@link ETInstallation}.
     * @param timeout  the timeout
     */
    public TSClient(final String toolName, final int timeout) {
        super(toolName, timeout);
        toolLibsIniPath = "";
        tcpPort = DEFAULT_TCP_PORT;
    }

    /**
     * Checks already opened Tool-Server instances.
     *
     * @param launcher the launcher
     * @param kill     specifies whether to task-kill the running processes
     * @return list of found processes, can be empty but never {@code null}
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static List<String> checkProcesses(final Launcher launcher, final boolean kill) throws IOException,
        InterruptedException {
        return launcher.getChannel().call(new CheckProcessCallable(kill));
    }

    /**
     * @return the ToolLibs.ini path
     */
    public String getToolLibsIniPath() {
        return toolLibsIniPath;
    }

    /**
     * @return the TCP port
     */
    public int getTcpPort() {
        return tcpPort;
    }

    @Override
    public boolean start(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                         final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Starting Tool-Server...");

        // Check open processes
        if (checkProcesses) {
            final List<String> foundProcesses = checkProcesses(launcher, true);
            if (!foundProcesses.isEmpty()) {
                logger.logInfo(String.format("Terminated running processes: %s", foundProcesses));
            }
        }

        // Check Tool-Server location and launch process
        if (StringUtils.isEmpty(getInstallPath())) {
            logger.logError("Tool-Server executable could not be found!");
        } else if (launchProcess(launcher, listener)) {
            logger.logInfo("Tool-Server started successfully.");
            return true;
        }
        return false;
    }

    @Override
    public boolean stop(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                        final TaskListener listener) throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Stopping Tool-Server...");

        // Stop Tool-Server
        if (launcher.getChannel().call(new StopCallable(getTimeout(), listener))) {
            logger.logInfo("Tool-Server stopped successfully.");
            return true;
        }
        return false;
    }

    @Override
    public boolean restart(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                           final TaskListener listener) throws IOException, InterruptedException {
        return stop(checkProcesses, workspace, launcher, listener)
            && start(checkProcesses, workspace, launcher, listener);
    }

    @Override
    protected ArgumentListBuilder createCmdLine() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getInstallPath());

        args.add("--port", String.valueOf(getTcpPort()));

        if (!getToolLibsIniPath().isEmpty()) {
            args.add("--toollibsini", getToolLibsIniPath());
        }

        return args;
    }

    /**
     * {@link Callable} providing remote access to close Tool-Server processes.
     */
    private static final class StopCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final int timeout;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link StopCallable}.
         *
         * @param timeout  the timeout
         * @param listener the listener
         */
        StopCallable(final int timeout, final TaskListener listener) {
            this.timeout = timeout;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isTerminated = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            try {
                // Check open processes
                final List<String> foundProcesses = ProcessUtil.checkTSProcess(false);
                if (foundProcesses.isEmpty()) {
                    logger.logWarn("No running Tool-Server instance found!");
                    isTerminated = true;
                } else {
                    // Wait for closing
                    final long endTimeMillis = System.currentTimeMillis() + (long) timeout * 1000L;
                    while (timeout <= 0 || System.currentTimeMillis() < endTimeMillis) {
                        if (ProcessUtil.checkTSProcess(true).isEmpty()) {
                            isTerminated = true;
                            break;
                        } else {
                            Thread.sleep(1000L);
                        }
                    }
                    if (!isTerminated) {
                        logger.logError(String.format("-> Timeout of %d seconds reached!", timeout));
                    }
                }
            } catch (final InterruptedException e) {
                logger.logError(e.getMessage());
            }
            return isTerminated;
        }
    }

    /**
     * {@link Callable} providing remote access to check open Tool-Server processes.
     */
    private static final class CheckProcessCallable extends MasterToSlaveCallable<List<String>, IOException> {

        private static final long serialVersionUID = 1L;

        private final boolean kill;

        /**
         * Instantiates a new {@link CheckProcessCallable}.
         *
         * @param kill specifies whether to task-kill running processes
         */
        CheckProcessCallable(final boolean kill) {
            this.kill = kill;
        }

        @Override
        public List<String> call() throws IOException {
            return ProcessUtil.checkTSProcess(kill);
        }
    }
}
