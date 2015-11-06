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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * System tests for {@link ToolBuilderDslExtension}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolBuilderDslExtensionST extends AbstractDslExtensionST {

    public static final String JOB_NAME = "toolBuilder";
    public static final String SCRIPT_NAME = "toolBuilder.groovy";

    @Override
    protected String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected String getDslScript() {
        return SCRIPT_NAME;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
                .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testBuildersWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final List<Builder> builders = project.getBuilders();
        assertThat("Tool related build steps should exist", builders, hasSize(4));
    }

    @Test
    public void testStartETWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StartETBuilder builder = builders.get(StartETBuilder.class);
        assertNotNull("Start ECU-TEST builder should exist", builder);
        assertThat(builder.getToolName(), is("ECU-TEST"));
        assertThat(builder.getWorkspaceDir(), is("test"));
        assertThat(builder.getTimeout(), is("60"));
        assertThat(builder.isDebugMode(), is(true));
    }

    @Test
    public void testStopETWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StopETBuilder builder = builders.get(StopETBuilder.class);
        assertNotNull("Stop ECU-TEST builder should exist", builder);
        assertThat(builder.getToolName(), is("ECU-TEST"));
        assertThat(builder.getTimeout(), is("60"));
    }

    @Test
    public void testStartTSWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StartTSBuilder builder = builders.get(StartTSBuilder.class);
        assertNotNull("Start Tool-Server builder should exist", builder);
        assertThat(builder.getToolName(), is("ECU-TEST"));
        assertThat(builder.getTimeout(), is("60"));
        assertThat(builder.getTcpPort(), is("5000"));
        assertThat(builder.getToolLibsIni(), is("C:\\ToolLibs.ini"));
    }

    @Test
    public void testStopTSWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StopTSBuilder builder = builders.get(StopTSBuilder.class);
        assertNotNull("Stop Tool-Server builder should exist", builder);
        assertThat(builder.getToolName(), is("ECU-TEST"));
        assertThat(builder.getTimeout(), is("60"));
    }
}
