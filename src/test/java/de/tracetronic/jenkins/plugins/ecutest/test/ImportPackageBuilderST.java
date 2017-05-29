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
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * System tests for {@link ImportPackageBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportPackageBuilderST extends SystemTestBase {

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
    public void testPackagePipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportPackageBuilder', "
                + "        importConfigs: [[$class: 'ImportPackageConfig', tmsPath: 'package',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultPackagePipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportPackageBuilder', "
                + "        importConfigs: [[$class: 'ImportPackageConfig', tmsPath: 'package']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedPackagePipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importPackages "
                + "     importConfigs: [[$class: 'ImportPackageConfig', tmsPath: 'package',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultPackagePipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importPackages "
                + "     importConfigs: [[$class: 'ImportPackageConfig', tmsPath: 'package']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testPackageDirPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportPackageBuilder', "
                + "        importConfigs: [[$class: 'ImportPackageDirConfig', tmsPath: 'packageDir',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultPackageDirPipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportPackageBuilder', "
                + "        importConfigs: [[$class: 'ImportPackageDirConfig', tmsPath: 'packageDir']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedPackageDirPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importPackages "
                + "     importConfigs: [[$class: 'ImportPackageDirConfig', tmsPath: tmsPath: 'packageDir',"
                + "        importPath: 'import', credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultPackageDirPipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importPackages "
                + "     importConfigs: [[$class: 'ImportPackageDirConfig', tmsPath: 'packageDir']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testPackageAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportPackageBuilder', "
                + "        importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg',"
                + "        credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultPackageAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('slaves') {\n"
                + "  step([$class: 'ImportPackageBuilder', "
                + "        importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedPackageAttributePipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importPackages "
                + "     importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg',"
                + "        credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultPackageAttributePipelineStep() throws Exception {
        assumeSymbolDependencies();

        final String script = ""
                + "node('slaves') {\n"
                + "  importPackages "
                + "     importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg']]\n"
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
     * Performs a default configuration round-trip testing for a {@link ImportPackageBuilder}.
     *
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
