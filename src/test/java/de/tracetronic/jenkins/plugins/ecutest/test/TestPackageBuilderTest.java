/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TestPackageBuilder}.
 */
public class TestPackageBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig("", true, true, false);
        final TestPackageBuilder builder = new TestPackageBuilder("");
        builder.setTestConfig(testConfig);
        builder.setPackageConfig(packageConfig);
        builder.setExecutionConfig(executionConfig);
        assertBuilder(builder);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final TestPackageBuilder builder = new TestPackageBuilder(null);
        builder.setTestConfig(null);
        builder.setPackageConfig(null);
        builder.setExecutionConfig(null);
        assertBuilder(builder);
    }

    /**
     * Asserts the builder properties.
     *
     * @param builder the builder
     */
    private void assertBuilder(final TestPackageBuilder builder) {
        assertNotNull(builder);
        assertNotNull(builder.getTestFile());
        assertTrue(builder.getTestFile().isEmpty());
        assertNotNull(builder.getTestConfig().getTbcFile());
        assertTrue(builder.getTestConfig().getTbcFile().isEmpty());
        assertNotNull(builder.getTestConfig().getTcfFile());
        assertTrue(builder.getTestConfig().getTcfFile().isEmpty());
        assertFalse(builder.getTestConfig().isForceReload());
        assertFalse(builder.getTestConfig().isLoadOnly());
        assertTrue(builder.getTestConfig().getConstants().isEmpty());
        assertTrue(builder.getPackageConfig().isRunTest());
        assertTrue(builder.getPackageConfig().isRunTraceAnalysis());
        assertNotNull(builder.getPackageConfig().getParameters());
        assertNotNull(builder.getPackageConfig().getOutputParameters());
        assertEquals(ExecutionConfig.getDefaultTimeout(), builder.getExecutionConfig().getParsedTimeout());
        assertTrue(builder.getExecutionConfig().isStopOnError());
        assertTrue(builder.getExecutionConfig().isCheckTestFile());
        assertFalse(builder.getExecutionConfig().isRecordWarnings());
    }
}
