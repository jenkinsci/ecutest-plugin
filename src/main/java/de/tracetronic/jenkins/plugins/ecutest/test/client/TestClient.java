/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * Client interface defining test execution controls.
 */
public interface TestClient {

    /**
     * Runs a test case, can be either an ecu.test package or project.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    boolean runTestCase(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException;

}
