/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Integration tests for {@link TestEnvContributor}.
 */
public class TestEnvContributorIT extends IntegrationTestBase {

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
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true, false);
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
        assertEquals("TT_TEST_REPORT_0 should match env action", testEnvAction.getTestReportDir(),
            envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_REPORT + testId));
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
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true, false);
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
        assertEquals("TT_TEST_REPORT_0 should match env action", testEnvAction.getTestReportDir(),
            envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_REPORT + testId));
        assertEquals("TT_TEST_RESULT_0 should match env action", testEnvAction.getTestResult(),
            envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_RESULT + testId));
        assertEquals("TT_TEST_TIMEOUT_0 should match env action", String.valueOf(testEnvAction.getTimeout()),
            envVars.get(TestEnvContributor.PREFIX + TestEnvContributor.TEST_TIMEOUT + testId));
    }
}
