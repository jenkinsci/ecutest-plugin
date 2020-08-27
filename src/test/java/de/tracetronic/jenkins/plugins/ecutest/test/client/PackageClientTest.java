/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PackageClient}.
 */
public class PackageClientTest {

    @Test
    public void testBlankConstructor() {
        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true, false);
        final PackageClient client = new PackageClient("", testConfig, packageConfig, executionConfig);
        assertEquals("", client.getTestFile());
        assertEquals("", client.getTestConfig().getTbcFile());
        assertEquals("", client.getTestConfig().getTcfFile());
        assertTrue(client.getTestConfig().getConstants().isEmpty());
        assertTrue(client.getPackageConfig().isRunTest());
        assertTrue(client.getPackageConfig().isRunTraceAnalysis());
        assertTrue(client.getPackageConfig().getParameters().isEmpty());
    }

    @Test
    public void testNullConstructor() {
        final TestConfig testConfig = new TestConfig(null, null, false, false, false, null);
        final PackageConfig packageConfig = new PackageConfig(true, true, null);
        final ExecutionConfig executionConfig = new ExecutionConfig(30, true, true, false);
        final PackageClient client = new PackageClient(null, testConfig, packageConfig, executionConfig);
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
        assertTrue(client.getPackageConfig().isRunTest());
        assertTrue(client.getPackageConfig().isRunTraceAnalysis());
        assertTrue(client.getPackageConfig().getParameters().isEmpty());
        assertEquals("Check timeout", 30, client.getExecutionConfig().getParsedTimeout());
        assertTrue("Check stop mode", client.getExecutionConfig().isStopOnError());
        assertTrue(client.getExecutionConfig().isCheckTestFile());
    }
}
