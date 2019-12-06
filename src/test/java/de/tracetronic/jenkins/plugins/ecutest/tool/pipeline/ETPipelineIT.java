/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETToolProperty;
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Integration tests for {@link ETPipeline}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETPipelineIT extends IntegrationTestBase {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", Collections
            .singletonList(new ETToolProperty("ECU-TEST.Application.8.0", 120, false))));

        final ScriptApproval scriptApproval = ScriptApproval.get();
        List<String> approvedSignatures = Arrays.asList(
            "method de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation getProgId",
            "method de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation getTimeout",
            "method de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation isRegisterComServer");
        for (String signature : approvedSignatures) {
            scriptApproval.approveSignature(signature);
        }
    }

    @Test
    public void testExistingInstallation() throws Exception {
        assertPipeline("etInstallation.groovy");
    }

    @Test
    public void testNewInstallation() throws Exception {
        assertPipeline("etNewInstallation.groovy");
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName the script name
     * @throws Exception the exception
     */
    private void assertPipeline(final String scriptName) throws Exception {
        final String script = loadPipelineScript(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }
}
