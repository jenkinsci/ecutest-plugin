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
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * System tests for {@link ExportPackageBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportPackageBuilderST extends SystemTestBase {

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
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ExportPackageBuilder', "
                + "        exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg',"
                + "        exportPath: 'export', createNewPath: false,"
                + "        credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultExportPackagePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ExportPackageBuilder', "
                + "        exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedExportPackagePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  exportPackages "
                + "     exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg',"
                + "        exportPath: 'export', createNewPath: false, "
                + "        credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultExportPackagePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  exportPackages "
                + "     exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testExportPackageAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ExportPackageBuilder', "
                + "        exportConfigs: [[$class: 'ExportPackageAttributeConfig', filePath: 'test.pkg',"
                + "        credentialsId: 'credentialsId', timeout: '600']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultExportPackageAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'ExportPackageBuilder', "
                + "        exportConfigs: [[$class: 'ExportPackageAttributeConfig', filePath: 'test.pkg']]])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedExportPackageAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  exportPackages "
                + "     exportConfigs: [[$class: 'ExportPackageAttributeConfig', filePath: 'test.pkg',"
                + "        credentialsId: 'credentialsId', timeout: '600']]\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultExportPackageAttributePipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  exportPackages "
                + "     exportConfigs: [[$class: 'ExportPackageAttributeConfig', filePath: 'test.pkg']]\n"
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
     * Performs a default configuration round-trip testing for a {@link ExportPackageBuilder}.
     *
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
