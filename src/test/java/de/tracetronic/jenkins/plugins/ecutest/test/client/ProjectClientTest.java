/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ProjectClient}.
 */
public class ProjectClientTest {

    @Test
    public void testBlankConstructor() {
        final TestConfig testConfig = new TestConfig("", "");
        final ProjectConfig projectConfig = new ProjectConfig(false, "", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(0, true, true);
        final ProjectClient client = new ProjectClient("", testConfig, projectConfig, executionConfig);
        assertEquals("", client.getTestFile());
        assertEquals("", client.getTestConfig().getTbcFile());
        assertEquals("", client.getTestConfig().getTcfFile());
        assertTrue(client.getTestConfig().getConstants().isEmpty());
        assertFalse(client.getProjectConfig().isExecInCurrentPkgDir());
        assertEquals("", client.getProjectConfig().getFilterExpression());
        assertEquals(JobExecutionMode.SEQUENTIAL_EXECUTION, client.getProjectConfig().getJobExecMode());
    }

    @Test
    public void testNullConstructor() {
        final TestConfig testConfig = new TestConfig(null, null, false, false, false, null);
        final ProjectConfig projectConfig = new ProjectConfig(false, null, JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(30, true, true);
        final ProjectClient client = new ProjectClient(null, testConfig, projectConfig, executionConfig);
        assertNotNull(client);
        assertEquals("", client.getTestFile());
        assertEquals("", client.getTestConfig().getTbcFile());
        assertEquals("", client.getTestConfig().getTcfFile());
        assertFalse(client.getTestConfig().isForceReload());
        assertFalse(client.getTestConfig().isLoadOnly());
        assertFalse(client.getTestConfig().isKeepConfig());
        assertTrue(client.getTestConfig().getConstants().isEmpty());
        assertEquals("", client.getTestName());
        assertEquals("", client.getTestDescription());
        assertEquals("", client.getTestReportDir());
        assertEquals("", client.getTestResult());
        assertFalse(client.getProjectConfig().isExecInCurrentPkgDir());
        assertEquals("", client.getProjectConfig().getFilterExpression());
        assertEquals(JobExecutionMode.SEQUENTIAL_EXECUTION, client.getProjectConfig().getJobExecMode());
        assertEquals("Check timeout", 30, client.getExecutionConfig().getParsedTimeout());
        assertTrue("Check stop mode", client.getExecutionConfig().isStopOnError());
        assertTrue(client.getExecutionConfig().isCheckTestFile());
    }
}
