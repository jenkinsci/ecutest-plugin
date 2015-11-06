/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleProject;
import javaposse.jobdsl.plugin.LookupStrategy;
import javaposse.jobdsl.plugin.RemovedJobAction;
import javaposse.jobdsl.plugin.RemovedViewAction;
import javaposse.jobdsl.plugin.ExecuteDslScripts;

import org.apache.commons.io.IOUtils;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;

/**
 * Common base class for DslExtension related System tests.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractDslExtensionST extends SystemTestBase {

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
     * @throws Exception
     *             the exception
     */
    protected FreeStyleProject createTestJob() throws Exception {
        buildSeedJob(getDslScript());

        assertThat(jenkins.getInstance().getJobNames(), hasItem(is(getJobName())));
        final FreeStyleProject project = jenkins.getInstance().getItemByFullName(getJobName(), FreeStyleProject.class);
        return project;
    }

    /**
     * Builds the seed job which generates the test job.
     *
     * @param dslScript
     *            the DSL script
     * @return the test job
     * @throws Exception
     *             the exception
     */
    private FreeStyleProject buildSeedJob(final String dslScript) throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(
                new ExecuteDslScripts(
                        new ExecuteDslScripts.ScriptLocation(null, null,
                                IOUtils.toString(this.getClass().getResourceAsStream(dslScript))),
                        false,
                        RemovedJobAction.DELETE,
                        RemovedViewAction.DELETE,
                        LookupStrategy.JENKINS_ROOT
                )
                );

        jenkins.buildAndAssertSuccess(project);
        return project;
    }
}
