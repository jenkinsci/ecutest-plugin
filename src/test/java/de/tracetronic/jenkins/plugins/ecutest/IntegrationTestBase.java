/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest;

import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import java.nio.charset.StandardCharsets;

import static org.junit.Assume.assumeFalse;

/**
 * Base class for all Jenkins related integration tests.
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
     * @throws Exception signals that an exception has occurred
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
     * @param name the file name
     * @return the pipeline content
     */
    protected String loadPipelineScript(final String name) {
        try {
            return new String(IOUtils.toByteArray(
                getClass().getResourceAsStream(getClass().getSimpleName() + "/" + name)), StandardCharsets.UTF_8);
        } catch (final Throwable t) {
            throw new RuntimeException("Could not read resource: [" + name + "].");
        }
    }
}
