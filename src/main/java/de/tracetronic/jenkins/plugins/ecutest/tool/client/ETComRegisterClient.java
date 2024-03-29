/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

/**
 * Client to register the ecu.test COM server via command line execution.
 */
public class ETComRegisterClient extends AbstractToolClient {

    /**
     * Instantiates a new {@link ETComRegisterClient}.
     *
     * @param toolName    the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath the Tool-Server install path
     */
    public ETComRegisterClient(final String toolName, final String installPath) {
        super(toolName, installPath, StartETBuilder.DEFAULT_TIMEOUT);
    }

    @Override
    public boolean start(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                         final TaskListener listener) throws InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(String.format("Registering COM server of %s...", getToolName()));

        if (StringUtils.isEmpty(getInstallPath())) {
            logger.logError("ecu.test COM server executable could not be found!");
        } else if (launchProcess(launcher, listener)) {
            logger.logInfo("ecu.test COM server registered successfully.");
            return true;
        }
        return false;
    }

    @Override
    public boolean stop(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                        final TaskListener listener) {
        throw new NotImplementedException();
    }

    @Override
    public boolean restart(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                           final TaskListener listener) {
        throw new NotImplementedException();
    }

    @Override
    protected ArgumentListBuilder createCmdLine() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getInstallPath());
        args.add("/register");
        args.add("/peruser");

        return args;
    }
}
