/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration;
import javaposse.jobdsl.plugin.LookupStrategy;
import javaposse.jobdsl.plugin.RemovedJobAction;
import javaposse.jobdsl.plugin.RemovedViewAction;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests.
 */
public abstract class AbstractDslExtensionIT extends IntegrationTestBase {

    /**
     * Gets the test job name.
     *
     * @return the test job name
     */
    protected abstract String getJobName();

    /**
     * Gets the DSL script.
     *
     * @return the DSL script
     */
    protected abstract String getDslScript();

    /**
     * Creates the test job.
     *
     * @return the test job
     * @throws Exception the exception
     */
    protected FreeStyleProject createTestJob() throws Exception {
        buildSeedJob(getDslScript());

        assertThat(jenkins.getInstance().getJobNames(), hasItem(is(getJobName())));
        return jenkins.getInstance().getItemByFullName(getJobName(), FreeStyleProject.class);
    }

    /**
     * Builds the seed job which generates the test job.
     *
     * @param dslScript the DSL script
     * @throws Exception the exception
     */
    private void buildSeedJob(final String dslScript) throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final ExecuteDslScripts builder = new ExecuteDslScripts();
        builder.setScriptText(IOUtils.toString(this.getClass().getResourceAsStream(dslScript), StandardCharsets.UTF_8));
        builder.setRemovedJobAction(RemovedJobAction.DELETE);
        builder.setRemovedViewAction(RemovedViewAction.DELETE);
        builder.setLookupStrategy(LookupStrategy.JENKINS_ROOT);
        project.getBuildersList().add(builder);
        jenkins.getInstance().getDescriptorByType(GlobalJobDslSecurityConfiguration.class).setUseScriptSecurity(false);

        jenkins.buildAndAssertSuccess(project);
    }
}
