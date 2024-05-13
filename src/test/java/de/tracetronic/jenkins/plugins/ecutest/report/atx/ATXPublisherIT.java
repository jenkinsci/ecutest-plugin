/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.DumbSlave;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration tests for {@link ATXPublisher}.
 */
public class ATXPublisherIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
        final ATXInstallation.DescriptorImpl atxImpl = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        final ATXInstallation inst = new ATXInstallation("test.guide", "ecu.test", new ATXConfig());
        atxImpl.setInstallations(inst);
    }

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final ATXPublisher before = new ATXPublisher("test.guide");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ATXPublisher.class));

        final ATXPublisher after = (ATXPublisher) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final ATXPublisher before = new ATXPublisher("test.guide");
        before.setFailOnOffline(false);
        before.setUsePersistedSettings(false);
        before.setInjectBuildVars(false);
        before.setRunOnFailed(false);
        before.setAllowMissing(false);
        before.setRunOnFailed(false);
        before.setArchiving(true);
        before.setKeepAll(true);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ATXPublisher.class));

        final ATXPublisher after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "failOnOffline,usePersistedSettings," +
            "allowMissing,runOnFailed,archiving,keepAll");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        publisher.setFailOnOffline(true);
        publisher.setUsePersistedSettings(true);
        publisher.setInjectBuildVars(true);
        publisher.setAllowMissing(true);
        publisher.setRunOnFailed(true);
        publisher.setArchiving(false);
        publisher.setKeepAll(false);
        project.getPublishersList().add(publisher);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ATXPublisher_DisplayName());
        jenkins.assertXPath(page, "//select[@name='atxName']");
        jenkins.assertXPath(page, "//option[@value='test.guide']");
        jenkins.assertXPath(page, "//input[@name='_.failOnOffline' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.usePersistedSettings' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.injectBuildVars' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.allowMissing' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.runOnFailed' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.archiving']");
        jenkins.assertXPath(page, "//input[@name='_.keepAll']");
    }

    @Test
    public void testGlobalConfigPresence() throws Exception {
        final HtmlPage page = getWebClient().goTo("configure");
        jenkins.assertXPath(page, "//div[@name='de-tracetronic-jenkins-plugins-ecutest-report-atx-installation" +
            "-ATXInstallation']");
    }

    @Test
    @LocalData
    public void testDefaultConfig() {
        final ATXInstallation.DescriptorImpl atxImpl = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        assertNotNull(atxImpl.getDefaultConfig());
    }

    @Test
    @LocalData
    public void testCurrentInstallation() {
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        assertNotNull(publisher.getInstallation());
    }

    @Test
    public void testFormRoundTrip() {
        final ATXInstallation.DescriptorImpl atxImpl = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        assertEquals(1, atxImpl.getInstallations().length);

        final ATXPublisher publisher = new ATXPublisher("test.guide");
        final ATXInstallation installation = publisher.getInstallation();
        assertNotNull(installation);
        assertEquals(installation.getName(), "test.guide");
        assertEquals(installation.getToolName(), "ecu.test");
    }

    @Test
    public void testFailOnOffline() throws Exception {
        final DumbSlave agent = assumeWindowsSlave();

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setAssignedNode(agent);
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        publisher.setFailOnOffline(true);
        project.getPublishersList().add(publisher);
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testAllowMissing() throws Exception {
        final DumbSlave slave = assumeWindowsSlave();

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setAssignedNode(slave);
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        publisher.setAllowMissing(false);
        project.getPublishersList().add(publisher);
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testRunOnFailed() throws Exception {
        final DumbSlave agent = assumeWindowsSlave();

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setAssignedNode(agent);
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) throws InterruptedException, IOException {
                return false;
            }
        });
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        publisher.setRunOnFailed(false);
        project.getPublishersList().add(publisher);
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
        assertThat("Skip message should be present in console log", build.getLog(100).toString(),
            containsString("Skipping publisher"));
    }

    @Test
    public void testParameterizedATXName() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ATXPublisher publisher = new ATXPublisher("${TESTGUIDE}");
        project.getPublishersList().add(publisher);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("TESTGUIDE", "test.guide");
                }
            }));

        assertEquals("ATX name should be resolved", "test.guide", publisher.getInstallation(envVars).getName());
    }

    @Test
    public void testParameterizedToolName() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        project.getPublishersList().add(publisher);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("ECUTEST", "ecu.test");
                }
            }));

        final ATXInstallation installation = publisher.getInstallation();
        assertNotNull(installation);
        assertEquals("Tool name should be resolved", "ecu.test",
            publisher.getToolInstallation(installation.getToolName(), envVars).getName());
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
        jenkins.assertLogContains("Publishing ATX reports to test.guide...", run);
        jenkins.assertLogContains("Starting ecu.test failed.", run);
    }
}
