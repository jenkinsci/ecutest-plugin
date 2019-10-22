/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Integration tests for {@link ATXPipeline}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXPipelineIT extends IntegrationTestBase {

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
        final ATXInstallation.DescriptorImpl atxImpl = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        final ATXInstallation inst = new ATXInstallation("TEST-GUIDE", "ECU-TEST", new ATXConfig());
        atxImpl.setInstallations(inst);
    }

    @Test
    public void testExistingServer() throws Exception {
        assertPipeline("atxServer.groovy", true);
    }

    @Test
    public void testNewServer() throws Exception {
        assertPipeline("atxNewServer.groovy", true);
    }

    @Test
    public void testOverrideSetting() throws Exception {
        assertPipeline("atxOverrideSetting.groovy", true);
    }

    @Test
    public void testPublish() throws Exception {
        assertPipeline("atxPublish.groovy", false);
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName the script name
     * @throws Exception the exception
     */
    private void assertPipeline(final String scriptName, boolean expectSuccess) throws Exception {
        final String script = loadPipelineScript(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        jenkins.assertBuildStatus(expectSuccess ? Result.SUCCESS : Result.FAILURE, job.scheduleBuild2(0).get());
    }
}
