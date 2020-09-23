/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.agent;

import jenkins.slaves.restarter.SlaveRestarter;

import java.io.IOException;

/**
 * Abstract JNLP agent restarter based on Windows Task Scheduler.
 */
public abstract class AbstractTaskAgentRestarter extends SlaveRestarter {

    protected abstract String getTaskName();

    @Override
    public boolean canWork() {
        try {
            final int ret = queryTask();
            return ret == 0;
        } catch (InterruptedException | IOException ignored) {
            // no-op
        }
        return false;
    }

    @Override
    public void restart() throws Exception {
        final int ret = execTask();
        throw new IOException("Failed restarting agent!\n"
                + "Task completed with exit value: " + ret);
    }

    /**
     * Queries the task scheduler.
     *
     * @return the process exit value
     * @throws InterruptedException the interrupted exception
     * @throws IOException          signals that an I/O exception has occurred
     */
    private int queryTask() throws InterruptedException, IOException {
        return runProcess("/query");
    }

    /**
     * Executes the task scheduler.
     *
     * @return the process exit value
     * @throws InterruptedException the interrupted exception
     * @throws IOException          signals that an I/O exception has occurred
     */
    private int execTask() throws InterruptedException, IOException {
        return runProcess("/run");
    }

    /**
     * Runs the task process with appropriate arguments.
     *
     * @param option the option
     * @return the process exit value
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    private int runProcess(final String option) throws IOException, InterruptedException {
        final ProcessBuilder procBuilder = new ProcessBuilder("schtasks.exe", option, "/tn", getTaskName());
        final Process proc = procBuilder.start();
        return proc.waitFor();
    }
}
