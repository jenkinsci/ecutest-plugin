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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
