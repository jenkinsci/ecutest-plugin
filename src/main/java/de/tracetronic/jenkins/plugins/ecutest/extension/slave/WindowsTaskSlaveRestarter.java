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
package de.tracetronic.jenkins.plugins.ecutest.extension.slave;

import hudson.Extension;

import java.io.IOException;

import jenkins.slaves.restarter.SlaveRestarter;

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
 * @since 1.8
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
        } catch (InterruptedException | IOException e) {
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
     * @throws InterruptedException
     *             the interrupted exception
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    private int queryTask() throws InterruptedException, IOException {
        return runProcess("/query");
    }

    /**
     * Executes the task scheduler.
     *
     * @return the process exit value
     * @throws InterruptedException
     *             the interrupted exception
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    private int execTask() throws InterruptedException, IOException {
        return runProcess("/run");
    }

    /**
     * Runs the task process with appropriate arguments.
     *
     * @param option
     *            the option
     * @return the process exit value
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private int runProcess(final String option) throws IOException, InterruptedException {
        final ProcessBuilder procBuilder = new ProcessBuilder("schtasks.exe", option, "/tn", TASKNAME);
        final Process proc = procBuilder.start();
        return proc.waitFor();
    }
}
