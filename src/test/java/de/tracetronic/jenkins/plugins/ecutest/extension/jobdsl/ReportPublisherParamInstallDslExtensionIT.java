/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.tms.TMSPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

/**
 * Integration tests for {@link ReportPublisherDslExtension} with parameterized tool selection.
 */
public class ReportPublisherParamInstallDslExtensionIT extends AbstractDslExtensionIT {

    public static final String JOB_NAME = "reportPublisherParamInstall";
    public static final String SCRIPT_NAME = "reportPublisherParamInstall.groovy";

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
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));

        final ATXInstallation.DescriptorImpl atxImpl = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        final ATXInstallation inst = new ATXInstallation("TEST-GUIDE", "${ECUTEST}", new ATXConfig());
        atxImpl.setInstallations(inst);
    }

    @Test
    public void testPublishersWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final List<Publisher> publishers = project.getPublishersList();
        assertThat("Report related publisher steps should exist", publishers, hasSize(4));
    }

    @Test
    public void testPublishATXWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();
        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("TESTGUIDE", "TEST-GUIDE");
                }
            }));

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final ATXPublisher publisher = publishers.get(ATXPublisher.class);
        assertNotNull("ATX report publisher should exist", publisher);
        assertThat(publisher.getAtxName(), is("${TESTGUIDE}"));
        assertThat(publisher.getInstallation(envVars).getName(), is("TEST-GUIDE"));
        assertThat(publisher.getInstallation(envVars).getToolName(), is("${ECUTEST}"));
    }

    @Test
    public void testUNITPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final JUnitPublisher publisher = publishers.get(JUnitPublisher.class);
        assertNotNull("UNIT report publisher should exist", publisher);
        assertThat(publisher.getToolName(), is("${ECUTEST}"));
    }

    @Test
    public void testGeneratorPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final ReportGeneratorPublisher publisher = publishers.get(ReportGeneratorPublisher.class);
        assertNotNull("Report generator publisher should exist", publisher);
        assertThat(publisher.getToolName(), is("${ECUTEST}"));
    }

    @Test
    public void testTMSPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final TMSPublisher publisher = publishers.get(TMSPublisher.class);
        assertNotNull("TMS publisher should exist", publisher);
        assertThat(publisher.getToolName(), is("${ECUTEST}"));
    }
}
