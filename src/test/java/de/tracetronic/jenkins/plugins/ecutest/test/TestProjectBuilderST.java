/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;
import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link TestProjectBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestProjectBuilderST extends SystemTestBase {

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final TestProjectBuilder before = new TestProjectBuilder("test.prj");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(TestProjectBuilder.class));

        final TestProjectBuilder after = (TestProjectBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf", true, true);
        final ProjectConfig projectConfig = new ProjectConfig(true, "filter", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final TestProjectBuilder before = new TestProjectBuilder("test.pkg");
        before.setTestConfig(testConfig);
        before.setProjectConfig(projectConfig);
        before.setExecutionConfig(executionConfig);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(TestProjectBuilder.class));

        // Need to flip keepConfig property due to inverted UI behavior
        final TestConfig testConfig2 = new TestConfig("test.tbc", "test.tcf", true, true, true, null);
        before.setTestConfig(testConfig2);

        final TestProjectBuilder after = (TestProjectBuilder) delegate;
        jenkins.assertEqualBeans(before, after, "testFile,testConfig,projectConfig,executionConfig");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf", true, true);
        final ProjectConfig projectConfig = new ProjectConfig(true, "filter", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final TestProjectBuilder builder = new TestProjectBuilder("test.prj");
        builder.setTestConfig(testConfig);
        builder.setProjectConfig(projectConfig);
        builder.setExecutionConfig(executionConfig);
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.TestProjectBuilder_DisplayName());
        WebAssert.assertInputPresent(page, "_.testFile");
        WebAssert.assertInputContainsValue(page, "_.testFile", "test.prj");
        WebAssert.assertInputPresent(page, "_.tbcFile");
        WebAssert.assertInputContainsValue(page, "_.tbcFile", "test.tbc");
        WebAssert.assertInputPresent(page, "_.tcfFile");
        WebAssert.assertInputContainsValue(page, "_.tcfFile", "test.tcf");
        WebAssert.assertInputPresent(page, "_.tcfFile");
        WebAssert.assertInputContainsValue(page, "_.tcfFile", "test.tcf");
        jenkins.assertXPath(page, "//input[@name='_.forceReload' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.loadOnly' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.execInCurrentPkgDir' and @checked='true']");
        WebAssert.assertInputPresent(page, "_.filterExpression");
        WebAssert.assertInputContainsValue(page, "_.filterExpression", "filter");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "600");
        jenkins.assertXPath(page, "//input[@name='_.stopOnError' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.checkTestFile' and @checked='true']");
    }

    @Test
    public void testTestId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final TestProjectBuilder builder = new TestProjectBuilder("test.prj");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Test id should be 0", 0, builder.getTestId(build));
    }

    @Test
    public void testPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'TestProjectBuilder',"
                + "        testFile: 'test.prj',"
                + "        testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],"
                + "        projectConfig: [execInCurrentPkgDir: true, filterExpression: 'Name=\"test\"', jobExecMode: 'SEPARATE_SEQUENTIAL_EXECUTION'],"
                + "        executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'TestProjectBuilder', testFile: 'test.prj'])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  testProject "
                + "        testFile: 'test.prj',"
                + "        testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],"
                + "        projectConfig: [execInCurrentPkgDir: true, filterExpression: 'Name=\"test\"', jobExecMode: 'SEPARATE_SEQUENTIAL_EXECUTION'],"
                + "        executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  testProject testFile: 'test.prj'\n"
                + "}";
        assertPipelineStep(script);
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param script
     *            the script
     * @throws Exception
     *             the exception
     */
    private void assertPipelineStep(final String script) throws Exception {
        assumeWindowsSlave();

        final WorkflowJob job = jenkins.jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        jenkins.assertLogContains("No running ECU-TEST instance found, please configure one at first!", run);
    }
}
