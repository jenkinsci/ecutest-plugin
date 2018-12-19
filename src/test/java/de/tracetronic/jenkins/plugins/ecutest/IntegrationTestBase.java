/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest;

import static org.junit.Assume.assumeFalse;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 * Base class for all Jenkins related integration tests.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class IntegrationTestBase {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    /**
     * @return the web client
     */
    protected WebClient getWebClient() {
        return jenkins.createWebClient();
    }

    /**
     * Creates a dumb slave and assumes that it runs on a Windows machine.
     *
     * @return the dumb slave
     * @throws Exception
     *             signals that an exception has occurred
     */
    protected DumbSlave assumeWindowsSlave() throws Exception {
        // Windows only
        final DumbSlave slave = jenkins.createOnlineSlave(Label.get("windows"));
        final SlaveComputer computer = slave.getComputer();
        assumeFalse("Test is Windows only!", computer.isUnix());
        return slave;
    }

    /**
     * Loads given pipeline script from class specific test resources.
     *
     * @param name
     *            the file name
     * @return the pipeline content
     */
    protected String loadPipelineScript(final String name) {
        try {
            return new String(IOUtils.toByteArray(
                    getClass().getResourceAsStream(getClass().getSimpleName() + "/" + name)), "UTF-8");
        } catch (final Throwable t) {
            throw new RuntimeException("Could not read resource: [" + name + "].");
        }
    }
}
