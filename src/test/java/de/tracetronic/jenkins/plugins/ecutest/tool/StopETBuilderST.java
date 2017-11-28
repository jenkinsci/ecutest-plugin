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
package de.tracetronic.jenkins.plugins.ecutest.tool;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import java.util.Collections;
import java.util.HashMap;

import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;

/**
 * System tests for {@link StopETBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StopETBuilderST extends SystemTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
                .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final StopETBuilder before = new StopETBuilder("ECU-TEST");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(StopETBuilder.class));

        final StopETBuilder after = (StopETBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final StopETBuilder before = new StopETBuilder("ECU-TEST");
        before.setTimeout("120");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(StopETBuilder.class));

        final StopETBuilder after = (StopETBuilder) delegate;
        jenkins.assertEqualBeans(before, after, "timeout");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StopETBuilder builder = new StopETBuilder("ECU-TEST");
        builder.setTimeout("30");
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.StopETBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='toolName']");
        jenkins.assertXPath(page, "//option[@value='ECU-TEST']");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "30");
    }

    @Test
    public void testToolId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StopETBuilder builder = new StopETBuilder("ECU-TEST");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Tool id should be 0", 0, builder.getToolId(build));
    }

    @Test
    public void testParameterizedToolName() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StopETBuilder builder = new StopETBuilder("${ECUTEST}");
        project.getBuildersList().add(builder);

        final EnvVars envVars = new EnvVars(
                Collections.unmodifiableMap(new HashMap<String, String>() {

                    private static final long serialVersionUID = 1L;
                    {
                        put("ECUTEST", "ECU-TEST");
                    }
                }));

        assertEquals("Tool name should be resolved", "ECU-TEST", builder.getToolInstallation(envVars).getName());
    }

    @Test
    public void testPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'StopETBuilder', toolName: 'ECU-TEST', timeout: '120'])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  step([$class: 'StopETBuilder', toolName: 'ECU-TEST'])\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  stopET toolName: 'ECU-TEST', timeout: '120'\n"
                + "}";
        assertPipelineStep(script);
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        final String script = ""
                + "node('windows') {\n"
                + "  stopET toolName: 'ECU-TEST'\n"
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

        // Create dummy JACOB library
        final FilePath rootPath = jenkins.jenkins.getRootPath();
        if (rootPath != null) {
            rootPath.child("\\plugins\\ecutest\\WEB-INF\\lib\\" + DllUtil.getLibraryFile(jenkins.jenkins.toComputer()))
            .write();
        }

        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        final WorkflowRun run = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains("Stopping ECU-TEST...", run);
        jenkins.assertLogContains("No running ECU-TEST instance found!", run);
    }
}
