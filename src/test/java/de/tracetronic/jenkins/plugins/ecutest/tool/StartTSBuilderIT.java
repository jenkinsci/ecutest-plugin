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
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
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

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * Integration tests for {@link StartTSBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StartTSBuilderIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
                .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final StartTSBuilder before = new StartTSBuilder("ECU-TEST");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(StartTSBuilder.class));

        final StartTSBuilder after = (StartTSBuilder) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final StartTSBuilder before = new StartTSBuilder("ECU-TEST");
        before.setToolLibsIni("C:\\ToolLibs.ini");
        before.setTcpPort("5017");
        before.setTimeout("120");
        before.setKeepInstance(false);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(StartTSBuilder.class));

        final StartTSBuilder after = (StartTSBuilder) delegate;
        jenkins.assertEqualBeans(before, after, "toolLibsIni,tcpPort,timeout,keepInstance");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StartTSBuilder builder = new StartTSBuilder("ECU-TEST");
        builder.setToolLibsIni("C:\\ToolLibs.ini");
        builder.setTcpPort("5017");
        builder.setTimeout("120");
        builder.setKeepInstance(true);
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.StartTSBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='toolName']");
        jenkins.assertXPath(page, "//option[@value='ECU-TEST']");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "120");
        WebAssert.assertInputPresent(page, "_.toolLibsIni");
        WebAssert.assertInputContainsValue(page, "_.toolLibsIni", "C:\\ToolLibs.ini");
        WebAssert.assertInputPresent(page, "_.tcpPort");
        WebAssert.assertInputContainsValue(page, "_.tcpPort", "5017");
        jenkins.assertXPath(page, "//input[@name='_.keepInstance' and @checked='true']");
    }

    @Test
    public void testToolId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StartTSBuilder builder = new StartTSBuilder("ECU-TEST");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Tool id should be 0", 0, builder.getToolId(build));
    }

    @Test
    public void testParameterizedToolName() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StartTSBuilder builder = new StartTSBuilder("${ECUTEST}");
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
        assertPipelineStep("classicStep.groovy");
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy");
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy");
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
        jenkins.assertLogContains("Starting Tool-Server...", run);
        jenkins.assertLogContains("Starting Tool-Server failed!", run);
    }
}
