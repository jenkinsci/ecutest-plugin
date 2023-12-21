/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

/**
 * Integration tests for {@link ToolBuilderDslExtension} with parameterized tool selection.
 */
public class ToolBuilderParamInstallDslExtensionIT extends AbstractDslExtensionIT {

    public static final String JOB_NAME = "toolBuilderParamInstall";
    public static final String SCRIPT_NAME = "toolBuilderParamInstall.groovy";

    @Override
    protected String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected String getDslScript() {
        return SCRIPT_NAME;
    }

    @Before
    public void setUp() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
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
        assertNotNull("Start ecu.test builder should exist", builder);
        assertThat(builder.getToolName(), is("${ECUTEST}"));
    }

    @Test
    public void testStopETWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StopETBuilder builder = builders.get(StopETBuilder.class);
        assertNotNull("Stop ecu.test builder should exist", builder);
        assertThat(builder.getToolName(), is("${ECUTEST}"));
    }

    @Test
    public void testStartTSWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StartTSBuilder builder = builders.get(StartTSBuilder.class);
        assertNotNull("Start Tool-Server builder should exist", builder);
        assertThat(builder.getToolName(), is("${ECUTEST}"));
    }

    @Test
    public void testStopTSWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final StopTSBuilder builder = builders.get(StopTSBuilder.class);
        assertNotNull("Stop Tool-Server builder should exist", builder);
        assertThat(builder.getToolName(), is("${ECUTEST}"));
    }
}
