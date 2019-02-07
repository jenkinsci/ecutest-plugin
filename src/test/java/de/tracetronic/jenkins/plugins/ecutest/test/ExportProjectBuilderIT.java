/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link ExportProjectBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportProjectBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL, "credentialsId", "test", "user", "password"));
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testDefaultEmptyConfigRoundTripStep() throws Exception {
        final ExportProjectBuilder before = new ExportProjectBuilder(null);
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultProjectConfigRoundTripStep() throws Exception {
        final ExportProjectBuilder before = createExportProjectBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultProjectAttributeConfigRoundTripStep() throws Exception {
        final ExportProjectBuilder before = createExportProjectAttributeBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testEmptyConfigRoundTripStep() throws Exception {
        final ExportProjectBuilder before = new ExportProjectBuilder(null);
        testConfigRoundTripStep(before);
    }

    @Test
    public void testProjectConfigRoundTripStep() throws Exception {
        final ExportProjectBuilder before = createExportProjectBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testProjectAttributeConfigRoundTripStep() throws Exception {
        final ExportProjectBuilder before = createExportProjectAttributeBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testProjectConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ExportProjectBuilder builder = createExportProjectBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ExportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.filePath' and @value='test.prj']");
        jenkins.assertXPath(page, "//input[@name='_.exportPath' and @value='export']");
        jenkins.assertXPath(page, "//input[@name='_.createNewPath' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testProjectAttributeConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ExportProjectBuilder builder = createExportProjectAttributeBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ExportProjectAttributeConfig_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.filePath' and @value='test.prj']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testExportProjectPipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy");
    }

    @Test
    public void testExportDefaultProjectPipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedExporProjectPipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultExportProjectPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy");
    }

    @Test
    public void testExportProjectAttributePipelineStep() throws Exception {
        assertPipelineStep("classicAttributeStep.groovy");
    }

    @Test
    public void testExportDefaultProjectAttributePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedExportProjectAttributePipelineStep() throws Exception {
        assertPipelineStep("symbolAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultExportPackageAttribbutePipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultAttributeStep.groovy");
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName the script name
     * @throws Exception the exception
     */
    private void assertPipelineStep(final String scriptName) throws Exception {
        assumeWindowsSlave();

        final String script = loadPipelineScript(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        jenkins.assertLogContains("No running ECU-TEST instance found, please configure one at first!", run);
    }

    /**
     * Performs a default configuration round-trip testing for a {@link ExportProjectBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testDefaultConfigRoundTripStep(final ExportProjectBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ExportProjectBuilder.class));

        final ExportProjectBuilder after = (ExportProjectBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    /**
     * Performs a configuration round-trip testing for a {@link ExportProjectBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testConfigRoundTripStep(final ExportProjectBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ExportProjectBuilder.class));

        final ExportProjectBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "exportConfigs");
    }

    /**
     * Creates an {@link ExportProjectBuilder} containing a {@link ExportProjectConfig} configuration.
     *
     * @return the configured builder
     */
    private ExportProjectBuilder createExportProjectBuilder() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectConfig tmsConfig = new ExportProjectConfig("test.prj", "export", true,
            "credentialsId", "600");
        exportConfigs.add(tmsConfig);
        return new ExportProjectBuilder(exportConfigs);
    }

    /**
     * Creates an {@link ExportProjectBuilder} containing a {@link ExportProjectAttributeConfig} configuration.
     *
     * @return the configured builder
     */
    private ExportProjectBuilder createExportProjectAttributeBuilder() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectAttributeConfig attributeConfig = new ExportProjectAttributeConfig("test.prj",
            "credentialsId", "600");
        exportConfigs.add(attributeConfig);
        return new ExportProjectBuilder(exportConfigs);
    }
}
