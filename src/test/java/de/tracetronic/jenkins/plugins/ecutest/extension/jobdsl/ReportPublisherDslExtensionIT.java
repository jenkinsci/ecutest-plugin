/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.tms.TMSPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link ReportPublisherDslExtension}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportPublisherDslExtensionIT extends AbstractDslExtensionIT {

    public static final String JOB_NAME = "reportPublisher";
    public static final String SCRIPT_NAME = "reportPublisher.groovy";

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
        final ATXInstallation inst = new ATXInstallation("TEST-GUIDE", "ECU-TEST", new ATXConfig());
        atxImpl.setInstallations(inst);
    }

    @Test
    public void testPublishersWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final List<Publisher> publishers = project.getPublishersList();
        assertThat("Report related publisher steps should exist", publishers, hasSize(6));
    }

    @Test
    public void testPublishATXWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final ATXPublisher publisher = publishers.get(ATXPublisher.class);
        assertNotNull("ATX report publisher should exist", publisher);
        assertThat(publisher.getAtxName(), is("TEST-GUIDE"));
        assertThat(publisher.getInstallation().getToolName(), is("ECU-TEST"));
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
    }

    @Test
    public void testTRFPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final TRFPublisher publisher = publishers.get(TRFPublisher.class);
        assertNotNull("TRF report publisher should exist", publisher);
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
    }

    @Test
    public void testUNITPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final JUnitPublisher publisher = publishers.get(JUnitPublisher.class);
        assertNotNull("UNIT report publisher should exist", publisher);
        assertThat(publisher.getToolName(), is("ECU-TEST"));
        assertEquals(0, Double.compare(15, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(30, publisher.getFailedThreshold()));
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
    }

    @Test
    public void testETLogPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final ETLogPublisher publisher = publishers.get(ETLogPublisher.class);
        assertNotNull("ECU-TEST log publisher should exist", publisher);
        assertTrue(publisher.isUnstableOnWarning());
        assertTrue(publisher.isFailedOnError());
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
    }

    @Test
    public void testGeneratorPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final ReportGeneratorPublisher publisher = publishers.get(ReportGeneratorPublisher.class);
        assertNotNull("Report generator publisher should exist", publisher);
        assertThat(publisher.getToolName(), is("ECU-TEST"));
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
        testGeneratorConfigWithDsl(publisher.getGenerators());
        testCustomGeneratorConfigWithDsl(publisher.getCustomGenerators());
    }

    private void testGeneratorConfigWithDsl(final List<ReportGeneratorConfig> list) throws Exception {
        assertThat("Generator should exist", list, hasSize(1));
        assertThat(list.get(0).getName(), is("HTML"));
        assertThat(list.get(0).isUsePersistedSettings(), is(true));
        testGeneratorSettingsWithDsl(list.get(0).getSettings());
    }

    private void testCustomGeneratorConfigWithDsl(final List<ReportGeneratorConfig> list) throws Exception {
        assertThat("Custom generator should exist", list, hasSize(1));
        assertThat(list.get(0).getName(), is("Custom"));
        assertThat(list.get(0).isUsePersistedSettings(), is(false));
        testGeneratorSettingsWithDsl(list.get(0).getSettings());
    }

    private void testGeneratorSettingsWithDsl(final List<ReportGeneratorSetting> list) throws Exception {
        assertThat("Generator settings should exist", list, hasSize(2));
        assertThat(list.get(0).getName(), is("param"));
        assertThat(list.get(0).getValue(), is("123"));
        assertThat(list.get(1).getName(), is("param2"));
        assertThat(list.get(1).getValue(), is("456"));
    }

    @Test
    public void testTMSPublisherWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();
        final TMSPublisher publisher = publishers.get(TMSPublisher.class);
        assertNotNull("TMS publisher should exist", publisher);
        assertThat(publisher.getToolName(), is("ECU-TEST"));
        assertThat(publisher.getCredentialsId(), is("credentialsId"));
        assertThat(publisher.getTimeout(), is("600"));
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
    }
}
