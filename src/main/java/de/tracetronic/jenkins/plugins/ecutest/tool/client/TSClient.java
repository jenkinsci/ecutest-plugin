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

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

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
     * @param toolName
     *            the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath
     *            the Tool-Server install path
     * @param timeout
     *            the timeout
     * @param toolLibsIniPath
     *            the alternative ToolLibs.ini path
     * @param tcpPort
     *            the alternative TCP port
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
     * @param toolName
     *            the tool name identifying the chosen {@link ETInstallation}.
     * @param timeout
     *            the timeout
     */
    public TSClient(final String toolName, final int timeout) {
        super(toolName, timeout);
        toolLibsIniPath = "";
        tcpPort = DEFAULT_TCP_PORT;
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

        // Launch Tool-Server process
        return launchProcess(launcher, listener);
    }

    @Override
    public boolean stop(final boolean checkProcesses, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        return launcher.getChannel().call(new StopCallable(getTimeout(), listener));
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

        args.add("--port", String.valueOf(getTcpPort()));

        if (!getToolLibsIniPath().isEmpty()) {
            args.add("--toollibsini", getToolLibsIniPath());
        }

        return args;
    }

    /**
     * Checks already opened Tool-Server instances.
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
    public static List<String> checkProcesses(final Launcher launcher, final boolean kill) throws IOException,
    InterruptedException {
        return launcher.getChannel().call(new CheckProcessCallable(kill));
    }

    /**
     * {@link Callable} providing remote access to close Tool-Server processes.
     */
    private static final class StopCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final int timeout;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link StopCallable}.
         *
         * @param timeout
         *            the timeout
         * @param listener
         *            the listener
         */
        StopCallable(final int timeout, final BuildListener listener) {
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
                    final long endTimeMillis = System.currentTimeMillis() + Long.valueOf(timeout) * 1000L;
                    while (timeout <= 0 || System.currentTimeMillis() < endTimeMillis) {
                        if (ProcessUtil.checkTSProcess(true).isEmpty()) {
                            isTerminated = true;
                            break;
                        } else {
                            Thread.sleep(1000L);
                        }
                    }
                }
            } catch (final IOException | InterruptedException e) {
                logger.logError(e.getMessage());
            }
            return isTerminated;
        }
    }

    /**
     * {@link Callable} providing remote access to check open Tool-Server processes.
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
            return ProcessUtil.checkTSProcess(kill);
        }
    }
}
