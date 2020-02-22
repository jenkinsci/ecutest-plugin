/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.EnvVars;
import hudson.slaves.DumbSlave;
import hudson.tools.ToolLocationNodeProperty;
import org.junit.Test;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Integration tests for {@link ETInstallation}.
 */
public class ETInstallationIT extends IntegrationTestBase {

    @Test
    @LocalData
    public void testInstallation() {
        final ETInstallation[] installations = jenkins.jenkins.getDescriptorByType(ETInstallation.DescriptorImpl.class)
            .getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        assertEquals("ECU-TEST", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST.Application", inst.getProgId());
        assertFalse(inst.isRegisterComServer());
    }

    @Test
    @LocalData
    public void testInstallationWithCustomProgId() {
        final ETInstallation[] installations = jenkins.jenkins.getDescriptorByType(ETInstallation.DescriptorImpl.class)
            .getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        assertEquals("ECU-TEST", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST6.Application", inst.getProgId());
        assertFalse(inst.isRegisterComServer());
    }

    @Test
    @LocalData
    public void testInstallationWithoutConfiguration() {
        final ETInstallation[] installations = jenkins.jenkins.getDescriptorByType(ETInstallation.DescriptorImpl.class)
            .getInstallations();
        assertEquals(0, installations.length);
    }

    @Test
    @LocalData
    public void testInstallationMigration() {
        final ETInstallation[] installations = jenkins.jenkins.getDescriptorByType(ETInstallation.DescriptorImpl.class)
            .getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        assertEquals("ECU-TEST", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST.Application", inst.getProgId());
    }

    @Test
    public void testGlobalConfigPresence() throws Exception {
        final HtmlPage page = getWebClient().goTo("configureTools");
        jenkins.assertXPath(page,
            "//tr[@name='de-tracetronic-jenkins-plugins-ecutest-tool-installation-ETInstallation']");
    }

    @Test
    public void testFormRoundTrip() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", Collections
            .singletonList(new ETToolProperty("ECU-TEST6.Application", 120, true))));

        final ToolLocationNodeProperty property = new ToolLocationNodeProperty(
            new ToolLocationNodeProperty.ToolLocation(etDescriptor, "ECU-TEST", "C:\\ECU-TEST"));
        final DumbSlave slave = jenkins.createSlave("slave", new EnvVars());
        slave.getNodeProperties().add(property);

        final HtmlPage page = getWebClient().getPage(slave, "configure");
        final HtmlForm form = page.getFormByName("config");
        jenkins.submit(form);

        assertEquals(1, slave.getNodeProperties().toList().size());

        final ToolLocationNodeProperty prop = slave.getNodeProperties().get(ToolLocationNodeProperty.class);
        assertEquals(1, prop.getLocations().size());

        final ToolLocationNodeProperty.ToolLocation location = prop.getLocations().get(0);
        assertEquals(etDescriptor, location.getType());
        assertEquals("ECU-TEST", location.getName());
        assertEquals("C:\\ECU-TEST", location.getHome());
    }
}
