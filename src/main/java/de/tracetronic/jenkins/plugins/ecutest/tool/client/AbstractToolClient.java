/*
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
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * Common base class for {@link ETClient} and {@link TSClient}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractToolClient implements ToolClient {

    private final String toolName;
    private final String installPath;
    private final int timeout;

    /**
     * Instantiates a new {@link AbstractToolClient}.
     *
     * @param toolName
     *            the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath
     *            the install path
     * @param timeout
     *            the timeout
     */
    public AbstractToolClient(final String toolName, final String installPath, final int timeout) {
        this.toolName = StringUtils.trimToEmpty(toolName);
        this.installPath = StringUtils.trimToEmpty(installPath);
        this.timeout = timeout;
    }

    /**
     * Instantiates a new {@link AbstractToolClient}.
     *
     * @param toolName
     *            the tool name identifying the chosen {@link ETInstallation}.
     * @param timeout
     *            the timeout
     */
    public AbstractToolClient(final String toolName, final int timeout) {
        this.toolName = StringUtils.trimToEmpty(toolName);
        this.timeout = timeout;
        installPath = "";
    }

    /**
     * @return the tool name
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the installation path
     */
    public String getInstallPath() {
        return installPath;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Creates the command line string for a process invocation.
     *
     * @return the {@link ArgumentListBuilder}
     */
    protected abstract ArgumentListBuilder createCmdLine();

    /**
     * Launches a process by using {@link ArgumentListBuilder} and waits for start up within a given timeout.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if process invocation succeeded, {@code false} if launching process failed or timeout
     *         exceeded
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    protected boolean launchProcess(final Launcher launcher, final TaskListener listener) throws InterruptedException {
        // Create command line
        final ArgumentListBuilder args = createCmdLine();
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(args.toString());

        boolean isStarted = false;
        try {
            // Launch tool process
            final Proc process = launcher.launch().cmds(args).quiet(true).start();

            // Wait for process start up
            final long endTimeMillis = System.currentTimeMillis() + Long.valueOf(getTimeout()) * 1000L;
            while (getTimeout() <= 0 || System.currentTimeMillis() < endTimeMillis) {
                if (process.isAlive()) {
                    isStarted = true;
                    break;
                } else {
                    Thread.sleep(1000L);
                }
            }
            if (!isStarted) {
                logger.logError(String.format("-> Timeout of %d seconds reached!", getTimeout()));
            }
        } catch (final IOException e) {
            logger.logError("-> Command line execution failed: " + e.getMessage());
        }
        return isStarted;
    }
}
