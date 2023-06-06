/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;
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
 * Integration tests for {@link ImportProjectBuilder}.
 */
public class ImportProjectBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        SystemCredentialsProvider.getInstance().getCredentials().add(new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL, "credentialsId", "test", "user", "password"));
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testDefaultEmptyConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = new ImportProjectBuilder(null);
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultArchiveConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportArchiveBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultTMSConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportProjectBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultTMSDirConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportProjectDirBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultProjectAttributeConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportProjectAttributeBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testEmptyConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = new ImportProjectBuilder(null);
        testConfigRoundTripStep(before);
    }

    @Test
    public void testArchiveConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportArchiveBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testProjectConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportProjectBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testProjectDirConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportProjectDirBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testProjectAttributeConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportProjectAttributeBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testArchiveConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportArchiveBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//input[@name='_.tmsPath' and @value='test.prz']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.importConfigPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.replaceFiles' and @checked='true']");
    }

    @Test
    public void testProjectConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportProjectBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.tmsPath' and @value='project']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.importMissingPackages' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
        jenkins.assertXPath(page, "//input[@name='_.tmProjectId' and @value='2']");
    }

    @Test
    public void testProjectDirConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportProjectDirBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.tmsPath' and @value='projectDir']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testProjectAttributeConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportProjectAttributeBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectAttributeConfig_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.filePath' and @value='test.prj']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testImportProjectPipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy");
    }

    @Test
    public void testImportDefaultProjectPipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportProjectPipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportProjectPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy");
    }

    @Test
    public void testImportProjectArchivePipelineStep() throws Exception {
        assertPipelineStep("classicArchiveStep.groovy");
    }

    @Test
    public void testImportDefaultProjectArchivePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultArchiveStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportProjectArchivePipelineStep() throws Exception {
        assertPipelineStep("symbolArchiveStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportProjectArchivePipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultArchiveStep.groovy");
    }

    @Test
    public void testImportProjectDirPipelineStep() throws Exception {
        assertPipelineStep("classicDirStep.groovy");
    }

    @Test
    public void testImportDefaultProjectDiripelineStep() throws Exception {
        assertPipelineStep("classicDefaultDirStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportProjectDirPipelineStep() throws Exception {
        assertPipelineStep("symbolDirStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportProjectDirPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultDirStep.groovy");
    }

    @Test
    public void testImportProjectAttributePipelineStep() throws Exception {
        assertPipelineStep("classicAttributeStep.groovy");
    }

    @Test
    public void testImportDefaultProjectAttributePipelineStep() throws Exception {
        assertPipelineStep("classicDefaultAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedImportProjectAttributePipelineStep() throws Exception {
        assertPipelineStep("symbolAttributeStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultImportProjectAttribbutePipelineStep() throws Exception {
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
        jenkins.assertLogContains("No running ECU-TEST instance found, please configure one at first!", run);
    }

    /**
     * Performs a default configuration round-trip testing for a {@link ImportProjectBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testDefaultConfigRoundTripStep(final ImportProjectBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ImportProjectBuilder.class));

        final ImportProjectBuilder after = (ImportProjectBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    /**
     * Performs a configuration round-trip testing for a {@link ImportProjectBuilder}.
     *
     * @param before the instance before
     * @throws Exception the exception
     */
    private void testConfigRoundTripStep(final ImportProjectBuilder before) throws Exception {
        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ImportProjectBuilder.class));

        final ImportProjectBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "importConfigs");
    }

    /**
     * Creates an {@link ImportProjectBuilder} containing a {@link ImportProjectArchiveConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportProjectBuilder createImportArchiveBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectArchiveConfig archiveConfig = new ImportProjectArchiveConfig("test.prz", "import", "import",
            true);
        importConfigs.add(archiveConfig);
        return new ImportProjectBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportProjectBuilder} containing a {@link ImportProjectConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportProjectBuilder createImportProjectBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectConfig tmsConfig = new ImportProjectConfig("project", "import", true,
            "credentialsId", "600", "2");
        importConfigs.add(tmsConfig);
        return new ImportProjectBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportProjectBuilder} containing a {@link ImportProjectDirConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportProjectBuilder createImportProjectDirBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectDirConfig tmsDirConfig = new ImportProjectDirConfig(
            "projectDir", "import", "credentialsId", "600");
        importConfigs.add(tmsDirConfig);
        return new ImportProjectBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportProjectBuilder} containing a {@link ImportProjectAttributeConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportProjectBuilder createImportProjectAttributeBuilder() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectAttributeConfig attributeConfig = new ImportProjectAttributeConfig("test.prj",
            "credentialsId", "600");
        importConfigs.add(attributeConfig);
        return new ImportProjectBuilder(importConfigs);
    }
}
