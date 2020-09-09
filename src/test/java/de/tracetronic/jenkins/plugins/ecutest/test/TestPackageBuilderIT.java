/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.GlobalConstant;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageOutputParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link TestPackageBuilder}.
 */
public class TestPackageBuilderIT extends IntegrationTestBase {

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
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true, false);
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

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final List<GlobalConstant> globalConstants = new ArrayList<GlobalConstant>();
        globalConstants.add(new GlobalConstant("testGlobalName", "testGlobalValue"));
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf", true, true, false, globalConstants);
        final List<PackageParameter> parameters = new ArrayList<PackageParameter>();
        parameters.add(new PackageParameter("testParamName", "testParamValue"));
        final List<PackageOutputParameter> outputParameters = new ArrayList<PackageOutputParameter>();
        outputParameters.add(new PackageOutputParameter("testOutputParamName"));
        final PackageConfig packageConfig = new PackageConfig(true, true, parameters,  outputParameters);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true, false);
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
        jenkins.assertXPath(page, "//input[@name='_.recordWarnings' and @checked='false']");
        jenkins.assertXPath(page, "//input[@name='_.name' and @value='testGlobalName']");
        jenkins.assertXPath(page, "//input[@name='_.value' and @value='testGlobalValue']");
        jenkins.assertXPath(page, "//input[@name='_.name' and @value='testParamName']");
        jenkins.assertXPath(page, "//input[@name='_.value' and @value='testParamValue']");
        jenkins.assertXPath(page, "//input[@name='_.name' and @value='testOutputParamName']");
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
        jenkins.assertLogContains("No running ECU-TEST instance found, please configure one at first!", run);
    }
}
