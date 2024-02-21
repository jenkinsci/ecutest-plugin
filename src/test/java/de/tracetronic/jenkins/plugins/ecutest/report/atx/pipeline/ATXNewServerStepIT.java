/*
 * Copyright (c) 2015-2024 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Test;

import java.util.Objects;

public class ATXNewServerStepIT extends IntegrationTestBase {

    @Test
    public void TestNewATXServerWithDynamicConfig() throws Exception {
        assertPipeline("atxNewDynamicServer.groovy", true);
    }

    /**
     * Asserts the pipeline step execution.
     *
     * @param scriptName the script name
     * @throws Exception the exception
     */
    private void assertPipeline(final String scriptName, boolean expectSuccess) throws Exception {
        final String script = loadTestResource(scriptName);
        final WorkflowJob job = jenkins.createProject(WorkflowJob.class, "pipeline");
        job.setDefinition(new CpsFlowDefinition(script, true));

        jenkins.assertBuildStatus(expectSuccess ? Result.SUCCESS : Result.FAILURE,
            Objects.requireNonNull(job.scheduleBuild2(0)).get());
    }
}
