/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link TestPackageBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestPackageBuilderST extends SystemTestBase {

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final TestPackageBuilder before = new TestPackageBuilder("test.pkg");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(TestPackageBuilder.class));

        final TestPackageBuilder after = (TestPackageBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf", true, true);
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final TestPackageBuilder before = new TestPackageBuilder("test.pkg");
        before.setTestConfig(testConfig);
        before.setPackageConfig(packageConfig);
        before.setExecutionConfig(executionConfig);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(TestPackageBuilder.class));

        final TestPackageBuilder after = (TestPackageBuilder) delegate;
        jenkins.assertEqualBeans(before, after, "testFile,testConfig,packageConfig,executionConfig");
    }

    @Deprecated
    @Test
    public void testConfigRoundTrip() throws Exception {
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final TestPackageBuilder before = new TestPackageBuilder("test.pkg", testConfig, packageConfig, executionConfig);
        final TestPackageBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "testFile,testConfig,packageConfig,executionConfig");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf", true, true);
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final TestPackageBuilder builder = new TestPackageBuilder("test.pkg");
        builder.setTestConfig(testConfig);
        builder.setPackageConfig(packageConfig);
        builder.setExecutionConfig(executionConfig);
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.TestPackageBuilder_DisplayName());
        WebAssert.assertInputPresent(page, "_.testFile");
        WebAssert.assertInputContainsValue(page, "_.testFile", "test.pkg");
        WebAssert.assertInputPresent(page, "_.tbcFile");
        WebAssert.assertInputContainsValue(page, "_.tbcFile", "test.tbc");
        WebAssert.assertInputPresent(page, "_.tcfFile");
        WebAssert.assertInputContainsValue(page, "_.tcfFile", "test.tcf");
        jenkins.assertXPath(page, "//input[@name='_.forceReload' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.loadOnly' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.runTest' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.runTraceAnalysis' and @checked='true']");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "600");
        jenkins.assertXPath(page, "//input[@name='_.stopOnError' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.checkTestFile' and @checked='true']");
    }

    @Test
    public void testTestId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final TestPackageBuilder builder = new TestPackageBuilder("test.pkg");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Test id should be 0", 0, builder.getTestId(build));
    }

    @Test
    public void testPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'TestPackageBuilder',"
                + "        testFile: 'test.pkg',"
                + "        testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],"
                + "        packageConfig: [parameters: [], runTest: false, runTraceAnalysis: false],"
                + "        executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'TestPackageBuilder', testFile: 'test.pkg'])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  testPackage "
                + "        testFile: 'test.pkg',"
                + "        testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],"
                + "        packageConfig: [parameters: [], runTest: false, runTraceAnalysis: false],"
                + "        executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  testPackage testFile: 'test.pkg'\n"
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
