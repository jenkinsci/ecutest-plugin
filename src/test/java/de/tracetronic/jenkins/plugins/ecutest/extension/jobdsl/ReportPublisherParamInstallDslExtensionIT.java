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
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hudson.EnvVars;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.tms.TMSPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * Integration tests for {@link ReportPublisherDslExtension} with parameterized tool selection.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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

        final ATXPublisher.DescriptorImpl atxImpl = jenkins.jenkins
                .getDescriptorByType(ATXPublisher.DescriptorImpl.class);
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
