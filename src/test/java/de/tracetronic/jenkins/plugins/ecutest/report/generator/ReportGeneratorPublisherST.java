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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.slaves.DumbSlave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * System tests for {@link ReportGeneratorPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorPublisherST extends SystemTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
                .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final ReportGeneratorPublisher before = new ReportGeneratorPublisher("ECU-TEST");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ReportGeneratorPublisher.class));

        final ReportGeneratorPublisher after = (ReportGeneratorPublisher) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final ReportGeneratorPublisher before = new ReportGeneratorPublisher("ECU-TEST");
        before.setAllowMissing(true);
        before.setRunOnFailed(true);
        before.setArchiving(true);
        before.setKeepAll(true);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(ReportGeneratorPublisher.class));

        final ReportGeneratorPublisher after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "allowMissing,runOnFailed,archiving,keepAll");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final List<ReportGeneratorSetting> settings = new ArrayList<ReportGeneratorSetting>();
        settings.add(new ReportGeneratorSetting("name", "value"));
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig("HTML", settings));
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig("Custom", settings));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setGenerators(generators);
        publisher.setCustomGenerators(customGenerators);
        publisher.setAllowMissing(true);
        publisher.setRunOnFailed(true);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        project.getPublishersList().add(publisher);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.ReportGeneratorPublisher_DisplayName());
        jenkins.assertXPath(page, "//select[@name='toolName']");
        jenkins.assertXPath(page, "//option[@value='ECU-TEST']");
        jenkins.assertXPath(page, "//select[@name='_.name' and @value='HTML']");
        jenkins.assertXPath(page, "//input[@name='_.name' and @value='Custom']");
        jenkins.assertXPath(page, "//input[@name='_.name' and @value='name']");
        jenkins.assertXPath(page, "//input[@name='_.value' and @value='value']");
        jenkins.assertXPath(page, "//input[@name='_.allowMissing' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.runOnFailed' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.archiving']");
        jenkins.assertXPath(page, "//input[@name='_.keepAll']");
    }

    @Test
    public void testAllowMissing() throws Exception {
        final DumbSlave slave = assumeWindowsSlave();

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setAssignedNode(slave);
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setAllowMissing(false);
        project.getPublishersList().add(publisher);
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
    }

    @Test
    public void testRunOnFailed() throws Exception {
        final DumbSlave slave = assumeWindowsSlave();

        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.setAssignedNode(slave);
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                    final BuildListener listener) throws InterruptedException, IOException {
                return false;
            }
        });
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setRunOnFailed(false);
        project.getPublishersList().add(publisher);
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, build);
        assertThat("Skip message should be present in console log", build.getLog(100).toString(),
                containsString("Skipping publisher"));
    }

    @Test
    public void testParameterizedToolName() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("${ECUTEST}");
        project.getPublishersList().add(publisher);

        final EnvVars envVars = new EnvVars(
                Collections.unmodifiableMap(new HashMap<String, String>() {

                    private static final long serialVersionUID = 1L;
                    {
                        put("ECUTEST", "ECU-TEST");
                    }
                }));

        assertEquals("Tool name should be resolved", "ECU-TEST", publisher.getToolInstallation(envVars).getName());
    }

    @Test
    public void testPipelineStep() throws Exception {
        assertPipelineStep("classicStep.groovy", false);
    }

    @Test
    public void testDefaultPipelineStep() throws Exception {
        assertPipelineStep("classicDefaultStep.groovy", true);
    }

    @Test
    public void testSymbolAnnotatedPipelineStep() throws Exception {
        assertPipelineStep("symbolStep.groovy", false);
    }

    @Test
    public void testSymbolAnnotatedDefaultPipelineStep() throws Exception {
        assertPipelineStep("symbolDefaultStep.groovy", true);
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName
     *            the script name
     * @param emptyResults
     *            if results are expected
     * @throws Exception
     *             the exception
     */
    private void assertPipelineStep(final String scriptName, final boolean emptyResults) throws Exception {
        assumeWindowsSlave();

        final String script = loadPipelineScript(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script));

        if (emptyResults == false) {
            final WorkflowRun run = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
            jenkins.assertLogContains("Publishing generator reports...", run);
            jenkins.assertLogContains("Starting ECU-TEST failed.", run);
        } else {
            final WorkflowRun run = jenkins.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
            jenkins.assertLogContains("Publishing generator reports...", run);
            jenkins.assertLogContains("Empty test results are not allowed, setting build status to FAILURE!", run);
        }
    }

    /**
     * Loads given pipeline script from test resources.
     *
     * @param name
     *            the file name
     * @return the pipeline content
     */
    private String loadPipelineScript(final String name) {
        return loadPipelineScript(name, this.getClass());
    }
}
