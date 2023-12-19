/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.agent;

import hudson.Extension;

/**
 * JNLP agent restarter based on Windows Task Scheduler.
 * <p>
 * This extension point will workaround the problem of already loaded libraries after reconnecting <br>
 * to the master (see <a href="https://issues.jenkins-ci.org/browse/JENKINS-31961">JENKINS-31961</a>).
 * </p>
 * <p>
 * In order to work a new task in the Windows Task Scheduler has to be created named <br>
 * <i>RESTART_JENKINS_AGENT</i> or renamed individually by system property <i>ecutest.taskName</i>.
 * This task should be configured with actions how to restart the agent.
 * </p>
 *
 * @since 2.17
 */
@Extension
public class WindowsTaskAgentRestarter extends AbstractTaskAgentRestarter {

    private static final long serialVersionUID = 1L;

    /**
     * Change the task name by invoking -Decutest.taskName={@code taskName} to Jenkins agent JVM
     * or setting system property ecutest.taskName directly.
     */
    private static final String TASKNAME = System.getProperty("ecutest.taskName", "RESTART_JENKINS_AGENT");


    @Override
    protected String getTaskName() {
        return TASKNAME;
    }
}
