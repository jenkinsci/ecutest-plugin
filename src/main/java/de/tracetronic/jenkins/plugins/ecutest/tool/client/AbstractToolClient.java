/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

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
     * @param toolName    the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath the install path
     * @param timeout     the timeout
     */
    public AbstractToolClient(final String toolName, final String installPath, final int timeout) {
        this.toolName = StringUtils.trimToEmpty(toolName);
        this.installPath = StringUtils.trimToEmpty(installPath);
        this.timeout = timeout;
    }

    /**
     * Instantiates a new {@link AbstractToolClient}.
     *
     * @param toolName the tool name identifying the chosen {@link ETInstallation}.
     * @param timeout  the timeout
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
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true} if process invocation succeeded, {@code false} if launching process failed or timeout
     * exceeded
     * @throws InterruptedException if the build gets interrupted
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
            final long endTimeMillis = System.currentTimeMillis() + (long) getTimeout() * 1000L;
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
