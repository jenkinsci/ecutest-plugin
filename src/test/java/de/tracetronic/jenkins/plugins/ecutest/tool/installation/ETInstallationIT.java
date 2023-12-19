/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.slaves.DumbSlave;
import hudson.tools.ToolLocationNodeProperty;
import org.junit.Test;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;
import java.util.Collections;
import java.util.Objects;

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
        assertEquals("ecu.test", inst.getName());
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
        assertEquals("ecu.test", inst.getName());
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
        assertEquals("ecu.test", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST.Application", inst.getProgId());
    }

    @Test
    public void testGlobalConfigPresence() throws Exception {
        final HtmlPage page = getWebClient().goTo("configureTools");
        jenkins.assertXPath(page,
            "//div[@name='de-tracetronic-jenkins-plugins-ecutest-tool-installation-ETInstallation']");
    }

    @Test
    public void testFormRoundTrip() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", Collections
            .singletonList(new ETToolProperty("ECU-TEST6.Application", 120, true))));

        final ToolLocationNodeProperty property = new ToolLocationNodeProperty(
            new ToolLocationNodeProperty.ToolLocation(etDescriptor, "ecu.test", "C:\\ECU-TEST"));
        final DumbSlave agent = jenkins.createSlave("agent", new EnvVars());
        agent.getNodeProperties().add(property);

        final HtmlPage page = getWebClient().getPage(agent, "configure");
        final HtmlForm form = page.getFormByName("config");
        jenkins.submit(form);

        assertEquals(1, agent.getNodeProperties().toList().size());

        final ToolLocationNodeProperty prop = agent.getNodeProperties().get(ToolLocationNodeProperty.class);
        assertEquals(1, prop.getLocations().size());

        final ToolLocationNodeProperty.ToolLocation location = prop.getLocations().get(0);
        assertEquals(etDescriptor, location.getType());
        assertEquals("ecu.test", location.getName());
        assertEquals("C:\\ECU-TEST", location.getHome());
    }

    @Test
    public void testExecutable() throws Exception {
        DumbSlave agent = assumeWindowsSlave();
        String exeFilePath = new File("C:\\ECU-TEST", "ECU-TEST.exe").getAbsolutePath();
        Objects.requireNonNull(agent.createPath(exeFilePath)).write();

        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", null));
        final ETInstallation[] installations = etDescriptor.getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        final Launcher launcher = agent.createLauncher(jenkins.createTaskListener());

        String executable = inst.getExecutable(launcher);
        assertEquals(exeFilePath, executable);
    }

    @Test
    public void testComExecutable() throws Exception {
        DumbSlave agent = assumeWindowsSlave();
        String exeFilePath = new File("C:\\ECU-TEST", "ECU-TEST_COM.exe").getAbsolutePath();
        Objects.requireNonNull(agent.createPath(exeFilePath)).write();

        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", null));
        final ETInstallation[] installations = etDescriptor.getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        final Launcher launcher = agent.createLauncher(jenkins.createTaskListener());

        String executable = inst.getComExecutable(launcher);
        assertEquals(exeFilePath, executable);
    }

    @Test
    public void testTSExecutable() throws Exception {
        DumbSlave agent = assumeWindowsSlave();
        String exeFilePath = new File("C:\\ECU-TEST", "Tool-Server.exe").getAbsolutePath();
        Objects.requireNonNull(agent.createPath(exeFilePath)).write();

        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ecu.test", "C:\\ECU-TEST", null));
        final ETInstallation[] installations = etDescriptor.getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        final Launcher launcher = agent.createLauncher(jenkins.createTaskListener());

        String executable = inst.getTSExecutable(launcher);
        assertEquals(exeFilePath, executable);
    }
}
