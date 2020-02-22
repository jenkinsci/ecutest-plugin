/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env.view;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import hudson.model.FreeStyleBuild;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration tests for {@link TestEnvActionView}.
 */
public class TestEnvActionViewIT extends IntegrationTestBase {

    @Test
    public void testWithoutTestEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final TestEnvActionView testEnvActionView = new TestEnvActionView(build, jenkins.createTaskListener());
        build.addAction(testEnvActionView);

        assertEquals("No test env variables should exist", 0, testEnvActionView.getEnvVariables().size());
    }

    @Test
    public void testWithTestEnvInvisibleAction() throws Exception {
        final int testId = 0;
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final PackageClient packageClient = new PackageClient("test.pkg", testConfig, packageConfig, executionConfig);
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(testId, packageClient);
        final FreeStyleBuild build = jenkins.createFreeStyleProject()
            .scheduleBuild2(0, null, Collections.singletonList(testEnvAction)).get();

        assertNotNull("One TestEnvInvisibleAction should exist", build.getAction(TestEnvInvisibleAction.class));
        assertNotNull("One TestEnvActionView should exist", build.getAction(TestEnvActionView.class));
    }
}
