/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.slave;

import hudson.Extension;
import jenkins.slaves.restarter.SlaveRestarter;

import java.io.IOException;

/**
 * JNLP slave restarter based on Windows Task Scheduler.
 * <p>
 * This extension point will workaround the problem of already loaded libraries after reconnecting <br>
 * to the master (see <a href="https://issues.jenkins-ci.org/browse/JENKINS-31961">JENKINS-31961</a>).
 * </p>
 * <p>
 * In order to work a new task in the Windows Task Scheduler has to be created named <br>
 * <i>RESTART_JENKINS_SLAVE</i> and configured with actions how to restart the slave.
 * </p>
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 * @since 1.8
 */
@Extension
public class WindowsTaskSlaveRestarter extends SlaveRestarter {

    private static final long serialVersionUID = 1L;

    private static final String TASKNAME = "RESTART_JENKINS_SLAVE";

    @Override
    public boolean canWork() {
        try {
            final int ret = queryTask();
            return ret == 0;
        } catch (InterruptedException | IOException ignored) {
        }
        return false;
    }

    @Override
    public void restart() throws Exception {
        final int ret = execTask();
        throw new IOException("Failed restarting slave!\n"
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
        final ProcessBuilder procBuilder = new ProcessBuilder("schtasks.exe", option, "/tn", TASKNAME);
        final Process proc = procBuilder.start();
        return proc.waitFor();
    }
}
