/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestFolderBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ProjectConfig projectConfig = new ProjectConfig(false, "", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig("", true, true);
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
        assertNotNull(builder.getExecutionConfig().getParsedTimeout());
        assertEquals(ExecutionConfig.getDefaultTimeout(), builder.getExecutionConfig().getParsedTimeout());
        assertTrue(builder.getExecutionConfig().isStopOnError());
        assertTrue(builder.getExecutionConfig().isCheckTestFile());
    }
}
