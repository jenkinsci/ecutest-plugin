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
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirTMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectTMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * System tests for {@link ImportProjectBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectBuilderST extends SystemTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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
        final ImportProjectBuilder before = createImportTMSBuilder();
        testDefaultConfigRoundTripStep(before);
    }

    @Test
    public void testDefaultTMSDirConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportTMSDirBuilder();
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
    public void testTMSConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportTMSBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testTMSDirConfigRoundTripStep() throws Exception {
        final ImportProjectBuilder before = createImportTMSDirBuilder();
        testConfigRoundTripStep(before);
    }

    @Test
    public void testArchiveConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportArchiveBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//input[@name='_.projectPath' and @value='test.prz']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.importConfigPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.replaceFiles' and @checked='true']");
    }

    @Test
    public void testTMSConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportTMSBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.projectPath' and @value='project']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testTMSDirConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ImportProjectBuilder builder = createImportTMSDirBuilder();
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ImportProjectBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='_.credentialsId']");
        jenkins.assertXPath(page, "//option[@value='credentialsId']");
        jenkins.assertXPath(page, "//input[@name='_.projectPath' and @value='projectDir']");
        jenkins.assertXPath(page, "//input[@name='_.importPath' and @value='import']");
        jenkins.assertXPath(page, "//input[@name='_.timeout' and @value='600']");
    }

    @Test
    public void testArchivePipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectArchiveConfig', projectPath: 'test.prz',"
                + "        importPath: 'import', importConfigPath: 'import', replaceFiles: true]]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultArchivePipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectArchiveConfig', projectPath: 'test.prz']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedArchivePipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectArchiveConfig', projectPath: 'test.prz',"
                + "        importPath: 'import', importConfigPath: 'import', replaceFiles: true]]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultArchivePipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectArchiveConfig', projectPath: 'test.prz']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testTMSPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectTMSConfig', projectPath: 'project',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultTMSPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectTMSConfig', projectPath: 'project']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedTMSPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectTMSConfig', projectPath: projectPath: 'project',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultTMSPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectTMSConfig', projectPath: 'project']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testTMSDirPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectDirTMSConfig', projectPath: 'projectDir',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultTMSDirPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportProjectBuilder', "
                + "        importConfigs: [[$class: 'ImportProjectDirTMSConfig', projectPath: 'projectDir']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedTMSDirPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectDirTMSConfig', projectPath: projectPath: 'projectDir',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultTMSDirPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importProjects "
                + "     importConfigs: [[$class: 'ImportProjectDirTMSConfig', projectPath: 'projectDir']]\n"
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
        final List<ImportProjectConfig> importConfigs = new ArrayList<ImportProjectConfig>();
        final ImportProjectArchiveConfig archiveConfig = new ImportProjectArchiveConfig("test.prz", "import", "import",
                true);
        importConfigs.add(archiveConfig);
        return new ImportProjectBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportProjectBuilder} containing a {@link ImportProjectTMSConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportProjectBuilder createImportTMSBuilder() {
        final List<ImportProjectConfig> importConfigs = new ArrayList<ImportProjectConfig>();
        final ImportProjectTMSConfig tmsConfig = new ImportProjectTMSConfig("project", "import", "credentialsId", "600");
        importConfigs.add(tmsConfig);
        return new ImportProjectBuilder(importConfigs);
    }

    /**
     * Creates an {@link ImportProjectBuilder} containing a {@link ImportProjectDirTMSConfig} configuration.
     *
     * @return the configured builder
     */
    private ImportProjectBuilder createImportTMSDirBuilder() {
        final List<ImportProjectConfig> importConfigs = new ArrayList<ImportProjectConfig>();
        final ImportProjectDirTMSConfig tmsDirConfig = new ImportProjectDirTMSConfig(
                "projectDir", "import", "credentialsId", "600");
        importConfigs.add(tmsDirConfig);
        return new ImportProjectBuilder(importConfigs);
    }
}
