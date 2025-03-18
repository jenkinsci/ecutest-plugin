/*
 * Copyright (c) 2015-2024 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Integration tests for {@link StopETBuilder}.
 */
public class LicenseETBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final LicenseETBuilder before = new LicenseETBuilder("ecu.test");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(LicenseETBuilder.class));

        final LicenseETBuilder after = (LicenseETBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final LicenseETBuilder builder = new LicenseETBuilder("ecu.test");
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.LicenseETBuilder_DisplayName());
        WebAssert.assertTextPresent(page, ETPlugin.DEPRECATION_WARNING);
        jenkins.assertXPath(page, "//select[@name='toolName']");
        jenkins.assertXPath(page, "//option[@value='ecu.test']");
    }

    @Test
    public void testParameterizedToolName() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final LicenseETBuilder builder = new LicenseETBuilder("${ECUTEST}");
        project.getBuildersList().add(builder);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("ECUTEST", "ecu.test");
                }
            }));

        assertEquals("Tool name should be resolved", "ecu.test", builder.getToolInstallation(envVars).getName());
    }

    @Test
    public void testParameterizedToolInstallation() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);

        final ETInstallation installation = new ETInstallation("ecu.test2", "C:\\ECU-TEST2",
            JenkinsRule.NO_PROPERTIES);
        final LicenseETBuilder builder = new LicenseETBuilder("${ECUTEST}");
        builder.setInstallation(installation);
        project.getBuildersList().add(builder);


        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;

                {
                    put("ECUTEST", "ecu.test");
                }
            }));

        assertFalse("Tool installation verification should be false", builder.isInstallationVerified(envVars));
    }

    @Test
    public void verifyInvalidToolInstallation() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final LicenseETBuilder builder = new LicenseETBuilder("ecu.test");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
        jenkins.assertLogContains(ETPlugin.DEPRECATION_WARNING, build);
        jenkins.assertLogContains("ecu.test executable for 'ecu.test' could not be found", build);
    }
}
