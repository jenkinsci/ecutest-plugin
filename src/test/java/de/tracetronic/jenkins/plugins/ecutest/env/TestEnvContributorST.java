/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;

import java.util.List;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link TestEnvContributor}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestEnvContributorST extends SystemTestBase {

    private final TestEnvContributor contributor = new TestEnvContributor();

    @Test
    public void testWithoutTestEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final EnvVars envVars = new EnvVars();
        contributor.buildEnvironmentFor(build, envVars, jenkins.createTaskListener());

        final List<TestEnvInvisibleAction> envActions = build.getActions(TestEnvInvisibleAction.class);
        assertEquals("No test env action should exist", 0, envActions.size());
    }

    @Test
    public void testWithTestPackageEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final int testId = 0;
        final EnvVars envVars = new EnvVars();
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final PackageClient packageClient = new PackageClient("test.pkg", testConfig, packageConfig, executionConfig);
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(testId, packageClient);
        build.addAction(testEnvAction);

        contributor.buildEnvironmentFor(build, envVars, jenkins.createTaskListener());

        final List<TestEnvInvisibleAction> envActions = build.getActions(TestEnvInvisibleAction.class);
        assertEquals("Only one test env action should exist", 1, envActions.size());

        assertEquals("TT_TEST_NAME_0 should match env action", testEnvAction.getTestName(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_NAME + testId));
        assertEquals("TT_TEST_DESCRIPTION_0 should match env action", testEnvAction.getTestDescription(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_DESCRIPTION + testId));
        assertEquals("TT_TEST_TYPE_0 should match env action", testEnvAction.getTestType().name(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TYPE + testId));
        assertEquals("TT_TEST_FILE_0 should match env action", testEnvAction.getTestFile(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_FILE + testId));
        assertEquals("TT_TEST_TBC_0 should match env action", testEnvAction.getTestTbc(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TBC + testId));
        assertEquals("TT_TEST_TCF_0 should match env action", String.valueOf(testEnvAction.getTestTcf()),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TCF + testId));
        assertEquals("TT_TEST_REPORT_DIR_0 should match env action", testEnvAction.getTestReportDir(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_REPORT_DIR + testId));
        assertEquals("TT_TEST_RESULT_0 should match env action", testEnvAction.getTestResult(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_RESULT + testId));
        assertEquals("TT_TEST_TIMEOUT_0 should match env action", String.valueOf(testEnvAction.getTimeout()),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TIMEOUT + testId));
    }

    @Test
    public void testWithTestProjectEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final int testId = 0;
        final EnvVars envVars = new EnvVars();
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final ProjectConfig projectConfig = new ProjectConfig(false, "filter", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final ProjectClient projectClient = new ProjectClient("test.prj", testConfig, projectConfig, executionConfig);
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(testId, projectClient);
        build.addAction(testEnvAction);

        contributor.buildEnvironmentFor(build, envVars, jenkins.createTaskListener());

        final List<TestEnvInvisibleAction> envActions = build.getActions(TestEnvInvisibleAction.class);
        assertEquals("Only one test env action should exist", 1, envActions.size());

        assertEquals("TT_TEST_NAME_0 should match env action", testEnvAction.getTestName(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_NAME + testId));
        assertNull("TT_TEST_DESCRIPTION_0 should not exist for projects",
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_DESCRIPTION + testId));
        assertEquals("TT_TEST_TYPE_0 should match env action", testEnvAction.getTestType().name(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TYPE + testId));
        assertEquals("TT_TEST_FILE_0 should match env action", testEnvAction.getTestFile(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_FILE + testId));
        assertEquals("TT_TEST_TBC_0 should match env action", testEnvAction.getTestTbc(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TBC + testId));
        assertEquals("TT_TEST_TCF_0 should match env action", String.valueOf(testEnvAction.getTestTcf()),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TCF + testId));
        assertEquals("TT_TEST_REPORT_DIR_0 should match env action", testEnvAction.getTestReportDir(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_REPORT_DIR + testId));
        assertEquals("TT_TEST_RESULT_0 should match env action", testEnvAction.getTestResult(),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_RESULT + testId));
        assertEquals("TT_TEST_TIMEOUT_0 should match env action", String.valueOf(testEnvAction.getTimeout()),
                envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TIMEOUT + testId));
    }
}
