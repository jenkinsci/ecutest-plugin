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
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * System tests for {@link ExportProjectBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportProjectBuilderST extends SystemTestBase {

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
     * @param scriptName
     *            the script name
     * @throws Exception
     *             the exception
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
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
     * @param before
     *            the instance before
     * @throws Exception
     *             the exception
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
