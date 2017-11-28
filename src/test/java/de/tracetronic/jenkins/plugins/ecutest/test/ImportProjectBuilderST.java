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
import static org.junit.Assert.assertThat;
import hudson.model.Result;
import hudson.model.FreeStyleProject;

import java.util.ArrayList;
import java.util.List;

import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Before;
import org.junit.Test;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * System tests for {@link ImportProjectBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectBuilderST extends SystemTestBase {

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
    public void testArchivePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectArchiveConfig', tmsPath: 'test.prz',"
                + "        importPath: 'import', importConfigPath: 'import', replaceFiles: true]]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultArchivePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectArchiveConfig', tmsPath: 'test.prz']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedArchivePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectArchiveConfig', tmsPath: 'test.prz',"
                + "        importPath: 'import', importConfigPath: 'import', replaceFiles: true]]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultArchivePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectArchiveConfig', tmsPath: 'test.prz']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testProjectPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectConfig', tmsPath: 'project',"
                + "        importPath: 'import', importMissingPackages: false,"
                + "        credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultProjectPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectConfig', tmsPath: 'project']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedProjectPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectConfig', tmsPath: 'project',"
                + "        importPath: 'import', importMissingPackages: false, "
                + "        credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultProjectPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectConfig', tmsPath: 'project']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testProjectDirPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectDirConfig', tmsPath: 'projectDir',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultProjectDirPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectDirConfig', tmsPath: 'projectDir']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedProjectDirPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "    importConfigs: [[$class: 'ImportProjectDirConfig', tmsPath: 'projectDir', importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultProjectDirPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "    importConfigs: [[$class: 'ImportProjectDirConfig', tmsPath: 'projectDir']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testProjectAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj',"
                + "        credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultProjectAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedProjectAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj',"
                + "        credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultProjectAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj']]\n"
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

        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        jenkins.assertLogContains("No running ECU-TEST instance found, please configure one at first!", run);
    }

    /**
     * Performs a default configuration round-trip testing for a {@link ImportProjectBuilder}.
     *
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
                "credentialsId", "600");
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
