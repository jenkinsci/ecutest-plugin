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
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
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
 * Integration tests for {@link ImportPackageBuilder}.
 */
public class ImportPackageBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL, "credentialsId", "test", "user", "password"));
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testDefaultEmptyConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = new ImportPackageBuilder(null);
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultPackageConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = createImportPackageBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultPackageDirConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = createImportPackageDirBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultPackageAttributeConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = createImportPackageAttributeBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testEmptyConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = new ImportPackageBuilder(null);
        testConfigRoundTripStep(before);
    }

    @Test
    public void testPackageConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = createImportPackageBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testPackageDirConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = createImportPackageDirBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testPackageAttributeConfigRoundTripStep() throws Exception {
        final ImportPackageBuilder before = createImportPackageAttributeBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testPackageConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportPackageBuilder builder = createImportPackageBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportPackageBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.tmsPath' and @value='package']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testPackageDirConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportPackageBuilder builder = createImportPackageDirBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportPackageBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.tmsPath' and @value='packageDir']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testPackageAttributeConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportPackageBuilder builder = createImportPackageAttributeBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportPackageAttributeConfig_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.filePath' and @value='test.pkg']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testImportPackagePipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy");
    }

    @Test
    public void testImportDefaultPackagePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportPackagePipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportPackagePipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy");
    }

    @Test
    public void testImportPackageDirPipelineStep() throws Exception {
        assertPipelineStep("classicDirStep.groovy");
    }

    @Test
    public void testImportDefaultPackageDiripelineStep() throws Exception {
        assertPipelineStep("classicDefaultDirStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportPackageDirPipelineStep() throws Exception {
        assertPipelineStep("symbolDirStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportPackageDirPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultDirStep.groovy");
    }

    @Test
    public void testImportPackageAttributePipelineStep() throws Exception {
        assertPipelineStep("classicAttributeStep.groovy");
    }

    @Test
    public void testImportDefaultPackageAttributePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportPackageAttributePipelineStep() throws Exception {
        assertPipelineStep("symbolAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportPackageAttribbutePipelineStep() throws Exception {
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
     * Performs a default configuration round-trip testing for a {@link ImportPackageBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testDefaultConfigRoundTripStep(final ImportPackageBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ImportPackageBuilder.class));

        final ImportPackageBuilder after = (ImportPackageBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    /**
     * Performs a configuration round-trip testing for a {@link ImportPackageBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testConfigRoundTripStep(final ImportPackageBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ImportPackageBuilder.class));

        final ImportPackageBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "importConfigs");
    }

    /**
     * Creates an {@link ImportPackageBuilder} containing a {@link ImportPackageConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportPackageBuilder createImportPackageBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportPackageConfig tmsConfig = new ImportPackageConfig("package", "import", "credentialsId", "600");
        importConfigs.add(tmsConfig);
        return new ImportPackageBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportPackageBuilder} containing a {@link ImportPackageDirConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportPackageBuilder createImportPackageDirBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportPackageDirConfig tmsDirConfig = new ImportPackageDirConfig(
            "packageDir", "import", "credentialsId", "600");
        importConfigs.add(tmsDirConfig);
        return new ImportPackageBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportPackageBuilder} containing a {@link ImportPackageAttributeConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportPackageBuilder createImportPackageAttributeBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportPackageAttributeConfig attributeConfig = new ImportPackageAttributeConfig("test.pkg",
            "credentialsId", "600");
        importConfigs.add(attributeConfig);
        return new ImportPackageBuilder(importConfigs);
    }
}
