/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link ETLogPublisher}.
 */
public class ETLogPublisherIT extends IntegrationTestBase {

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final ETLogPublisher before = new ETLogPublisher();

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ETLogPublisher.class));

        final ETLogPublisher after = (ETLogPublisher) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final ETLogPublisher before = new ETLogPublisher();
        before.setUnstableOnWarning(false);
        before.setRunOnFailed(false);
        before.setAllowMissing(false);
        before.setRunOnFailed(false);
        before.setArchiving(true);
        before.setKeepAll(true);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ETLogPublisher.class));

        final ETLogPublisher after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after,
            "unstableOnWarning,failedOnError,testSpecific,allowMissing,runOnFailed,archiving,keepAll");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ETLogPublisher publisher = new ETLogPublisher();
        publisher.setUnstableOnWarning(true);
        publisher.setFailedOnError(true);
        publisher.setTestSpecific(true);
        publisher.setAllowMissing(true);
        publisher.setRunOnFailed(true);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        project.getPublishersList().add(publisher);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ETLogPublisher_DisplayName());
        jenkins.assertXPath(page, "//input[@name='_.unstableOnWarning' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.failedOnError' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.testSpecific' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.allowMissing' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.runOnFailed' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.archiving']");
        jenkins.assertXPath(page, "//input[@name='_.keepAll']");
    }

    @Test
    public void testAllowMissing() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ETLogPublisher publisher = new ETLogPublisher();
        publisher.setAllowMissing(false);
        project.getPublishersList().add(publisher);

        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testRunOnFailed() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) throws InterruptedException, IOException {
                return false;
            }
        });

        final ETLogPublisher publisher = new ETLogPublisher();
        publisher.setRunOnFailed(false);
        project.getPublishersList().add(publisher);

        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
        assertThat("Skip message should be present in console log", build.getLog(100).toString(),
            containsString("Skipping publisher"));
    }

    @Test
    public void testUnstableOnWarning() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final URL url = this.getClass().getResource("ECU_TEST_OUT.log");
        final FilePath logFile = new FilePath(new File(url.getFile()));
        project.setCustomWorkspace(logFile.getParent().getRemote());

        final ETLogPublisher publisher = new ETLogPublisher();
        publisher.setUnstableOnWarning(true);
        project.getPublishersList().add(publisher);

        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.UNSTABLE, build);
    }

    @Test
    public void testFailedOnError() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final URL url = this.getClass().getResource("ECU_TEST_ERR.log");
        final FilePath logFile = new FilePath(new File(url.getFile()));
        project.setCustomWorkspace(logFile.getParent().getRemote());

        final ETLogPublisher publisher = new ETLogPublisher();
        publisher.setFailedOnError(true);
        project.getPublishersList().add(publisher);

        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testPipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy", true);
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy", false);
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy", true);
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy", false);
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName the script name
     * @param status     the expected build status
     * @throws Exception the exception
     */
    private void assertPipelineStep(final String scriptName, final boolean status) throws Exception {
        assumeWindowsSlave();

        final String script = loadPipelineScript(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        if (status == true) {
            final WorkflowRun run = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
            jenkins.assertLogContains("Publishing ECU-TEST logs...", run);
            jenkins.assertLogContains("Archiving ECU-TEST logs is disabled.", run);
        } else {
            final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
            jenkins.assertLogContains("Publishing ECU-TEST logs...", run);
            jenkins.assertLogContains("Empty log results are not allowed, setting build status to FAILURE!", run);
        }
    }
}
