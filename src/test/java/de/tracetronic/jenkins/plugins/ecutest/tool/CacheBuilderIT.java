/*
 * Copyright (c) 2015-2024 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for {@link CacheBuilder}.
 */
public class CacheBuilderIT extends IntegrationTestBase {

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        CacheConfig config = new CacheConfig(Caches.CacheType.A2L, "C:\\test.a2l", "", false);
        final CacheBuilder before = new CacheBuilder(Collections.singletonList(config));

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(CacheBuilder.class));

        final CacheBuilder after = (CacheBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        CacheConfig config = new CacheConfig(Caches.CacheType.A2L, "C:\\test.a2l", "", false);
        final CacheBuilder before = new CacheBuilder(Collections.singletonList(config));

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(CacheBuilder.class));

        final CacheBuilder after = (CacheBuilder) delegate;
        jenkins.assertEqualBeans(before, after, "caches");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        CacheConfig config = new CacheConfig(Caches.CacheType.A2L, "C:\\test.a2l", "test", true);
        final CacheBuilder builder = new CacheBuilder(Collections.singletonList(config));
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.CacheBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.type']");
        jenkins.assertXPath(page, "//option[@value='A2L']");
        WebAssert.assertInputPresent(page, "_.filePath");
        WebAssert.assertInputContainsValue(page, "_.filePath", "C:\\test.a2l");
        WebAssert.assertInputPresent(page, "_.dbChannel");
        WebAssert.assertInputContainsValue(page, "_.dbChannel", "test");
        jenkins.assertXPath(page, "//input[@name='_.clear' and @checked='true']");
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
        jenkins.assertLogContains("No running ecu.test instance found, please configure one at first!", run);
    }
}
