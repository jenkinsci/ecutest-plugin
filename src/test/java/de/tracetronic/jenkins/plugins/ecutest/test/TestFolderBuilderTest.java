/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TestFolderBuilderTest}.
 */
public class TestFolderBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ProjectConfig projectConfig = new ProjectConfig(false, "", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig("", true, true, false);
        final TestFolderBuilder builder = new TestFolderBuilder("");
        builder.setTestConfig(testConfig);
        builder.setPackageConfig(packageConfig);
        builder.setProjectConfig(projectConfig);
        builder.setExecutionConfig(executionConfig);
        assertBuilder(builder);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final TestFolderBuilder builder = new TestFolderBuilder(null);
        builder.setTestConfig(null);
        builder.setPackageConfig(null);
        builder.setProjectConfig(null);
        builder.setExecutionConfig(null);
        assertBuilder(builder);
    }

    /**
     * Asserts the builder properties.
     *
     * @param builder the builder
     */
    private void assertBuilder(final TestFolderBuilder builder) {
        assertNotNull(builder);
        assertNotNull(builder.getTestFile());
        assertTrue(builder.getTestFile().isEmpty());
        assertFalse(builder.isRecursiveScan());
        assertEquals(TestFolderBuilder.DEFAULT_SCANMODE, builder.getScanMode());
        assertTrue(builder.isFailFast());
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
        assertFalse(builder.getProjectConfig().isExecInCurrentPkgDir());
        assertNotNull(builder.getProjectConfig().getFilterExpression());
        assertTrue(builder.getProjectConfig().getFilterExpression().isEmpty());
        assertEquals(JobExecutionMode.SEQUENTIAL_EXECUTION, builder.getProjectConfig().getJobExecMode());
        assertEquals(ExecutionConfig.getDefaultTimeout(), builder.getExecutionConfig().getParsedTimeout());
        assertTrue(builder.getExecutionConfig().isStopOnError());
        assertTrue(builder.getExecutionConfig().isCheckTestFile());
        assertFalse(builder.getExecutionConfig().isRecordWarnings());
    }
}
