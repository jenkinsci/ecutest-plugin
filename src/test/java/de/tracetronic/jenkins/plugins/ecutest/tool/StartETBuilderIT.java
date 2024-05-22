/*
 * Copyright (c) 2015-2024 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link StartETBuilder}.
 */
public class StartETBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final StartETBuilder before = new StartETBuilder("ecu.test");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(StartETBuilder.class));

        final StartETBuilder after = (StartETBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final StartETBuilder before = new StartETBuilder("ecu.test");
        before.setWorkspaceDir("workspace");
        before.setSettingsDir("settings");
        before.setTimeout("120");
        before.setDebugMode(false);
        before.setKeepInstance(false);
        before.setUpdateUserLibs(false);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(StartETBuilder.class));

        final StartETBuilder after = (StartETBuilder) delegate;
        jenkins.assertEqualBeans(before, after, "workspaceDir,settingsDir,timeout,debugMode,keepInstance,updateUserLibs");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StartETBuilder builder = new StartETBuilder("ecu.test");
        builder.setWorkspaceDir("workspace");
        builder.setSettingsDir("settings");
        builder.setTimeout("120");
        builder.setDebugMode(true);
        builder.setKeepInstance(true);
        builder.setUpdateUserLibs(true);
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.StartETBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='toolName']");
        jenkins.assertXPath(page, "//option[@value='ecu.test']");
        WebAssert.assertInputPresent(page, "_.workspaceDir");
        WebAssert.assertInputContainsValue(page, "_.workspaceDir", "workspace");
        WebAssert.assertInputPresent(page, "_.settingsDir");
        WebAssert.assertInputContainsValue(page, "_.settingsDir", "settings");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "120");
        jenkins.assertXPath(page, "//input[@name='_.debugMode' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.keepInstance' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.updateUserLibs' and @checked='true']");
    }

    @Test
    public void testToolId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StartETBuilder builder = new StartETBuilder("ecu.test");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Tool id should be 0", 0, builder.getToolId(build));
    }

    @Test
    public void testParameterizedToolName() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StartETBuilder builder = new StartETBuilder("${ECUTEST}");
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
        final StartETBuilder builder = new StartETBuilder("${ECUTEST}");
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
    public void testPipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy");
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy");
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName the script name
     * @throws Exception the exception
     */
    private void assertPipelineStep(final String scriptName) throws Exception {
        assumeWindowsSlave();

        final String script = loadTestResource(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        jenkins.assertLogContains("ecu.test executable could not be found!", run);
    }
}
