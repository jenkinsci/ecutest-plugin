/*
 * Copyright (c) 2015-2024 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Integration tests for {@link ExportPackageBuilder}.
 */
public class ExportPackageBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL, "credentialsId", "test", "user", "password"));
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testDefaultEmptyConfigRoundTripStep() throws Exception {
        final ExportPackageBuilder before = new ExportPackageBuilder(null);
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultExportPackageConfigRoundTripStep() throws Exception {
        final ExportPackageBuilder before = createExportPackageBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultExportPackageAttributeConfigRoundTripStep() throws Exception {
        final ExportPackageBuilder before = createExportPackageAttributeBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testEmptyConfigRoundTripStep() throws Exception {
        final ExportPackageBuilder before = new ExportPackageBuilder(null);
        testConfigRoundTripStep(before);
    }

    @Test
    public void testExportPackageConfigRoundTripStep() throws Exception {
        final ExportPackageBuilder before = createExportPackageBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testExportPackageAttributeConfigRoundTripStep() throws Exception {
        final ExportPackageBuilder before = createExportPackageAttributeBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testExportPackageConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ExportPackageBuilder builder = createExportPackageBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ExportPackageBuilder_DisplayName());
        WebAssert.assertTextPresent(page, ETPlugin.DEPRECATION_WARNING);
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.filePath' and @value='test.pkg']");
        jenkins.assertXPath(page, "//input[@name='_.exportPath' and @value='export']");
        jenkins.assertXPath(page, "//input[@name='_.createNewPath' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testExportPackageAttributeConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ExportPackageBuilder builder = createExportPackageAttributeBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ExportPackageAttributeConfig_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.filePath' and @value='test.pkg']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testExportPackagePipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy");
    }

    @Test
    public void testExportDefaultPackagePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedExportPackagePipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultExportPackagePipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy");
    }

    @Test
    public void testExportPackageAttributePipelineStep() throws Exception {
        assertPipelineStep("classicAttributeStep.groovy");
    }

    @Test
    public void testExportDefaultPackageAttributePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedExportPackageAttributePipelineStep() throws Exception {
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

        final String script = loadTestResource(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        jenkins.assertLogContains(ETPlugin.DEPRECATION_WARNING, run);
        jenkins.assertLogContains("No running ecu.test instance found, please configure one at first!", run);
    }

    /**
     * Performs a default configuration round-trip testing for a {@link ExportPackageBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testDefaultConfigRoundTripStep(final ExportPackageBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ExportPackageBuilder.class));

        final ExportPackageBuilder after = (ExportPackageBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    /**
     * Performs a configuration round-trip testing for a {@link ExportPackageBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testConfigRoundTripStep(final ExportPackageBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ExportPackageBuilder.class));

        final ExportPackageBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "exportConfigs");
    }

    /**
     * Creates an {@link ExportPackageBuilder} containing a {@link ExportPackageConfig} configuration.
     *
     * @return the configured builder
     */
    private ExportPackageBuilder createExportPackageBuilder() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageConfig tmsConfig = new ExportPackageConfig("test.pkg", "export", true,
            "credentialsId", "600");
        exportConfigs.add(tmsConfig);
        return new ExportPackageBuilder(exportConfigs);
    }

    /**
     * Creates an {@link ExportPackageBuilder} containing a {@link ExportPackageAttributeConfig} configuration.
     *
     * @return the configured builder
     */
    private ExportPackageBuilder createExportPackageAttributeBuilder() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageAttributeConfig attributeConfig = new ExportPackageAttributeConfig("test.pkg",
            "credentialsId", "600");
        exportConfigs.add(attributeConfig);
        return new ExportPackageBuilder(exportConfigs);
    }
}
