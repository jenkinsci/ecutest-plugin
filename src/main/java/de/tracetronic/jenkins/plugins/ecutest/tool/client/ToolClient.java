/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * Client interface defining tool start up and tear down controls.
 */
public interface ToolClient {

    /**
     * Starts a tool.
     *
     * @param checkProcesses specifies whether to check open processes after tear down
     * @param workspace      the workspace
     * @param launcher       the launcher
     * @param listener       the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    boolean start(boolean checkProcesses, FilePath workspace, Launcher launcher, TaskListener listener)
        throws IOException, InterruptedException;

    /**
     * Stops a tool.
     *
     * @param checkProcesses specifies whether to check open processes after tear down
     * @param workspace      the workspace
     * @param launcher       the launcher
     * @param listener       the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    boolean stop(boolean checkProcesses, FilePath workspace, Launcher launcher, TaskListener listener)
        throws IOException, InterruptedException;

    /**
     * Restarts a tool.
     *
     * @param checkProcesses specifies whether to check open processes after tear down
     * @param workspace      the workspace
     * @param launcher       the launcher
     * @param listener       the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    boolean restart(boolean checkProcesses, FilePath workspace, Launcher launcher, TaskListener listener)
        throws IOException, InterruptedException;

}
