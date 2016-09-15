/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import static org.junit.Assert.assertEquals;
import hudson.EnvVars;
import hudson.slaves.DumbSlave;
import hudson.tools.ToolLocationNodeProperty;

import java.util.Collections;

import org.junit.Test;
import org.jvnet.hudson.test.recipes.LocalData;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;

/**
 * System tests for {@link ETInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETInstallationST extends SystemTestBase {

    @Test
    @LocalData
    public void testInstallation() {
        final ETInstallation[] installations = jenkins.jenkins.getDescriptorByType(ETInstallation.DescriptorImpl.class)
                .getInstallations();
        assertEquals(1, installations.length);

        final ETInstallation inst = installations[0];
        assertEquals("ECU-TEST", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST.Application", inst.getProgramId());
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
        assertEquals("ECU-TEST6.Application", inst.getProgramId());
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
        assertEquals("ECU-TEST.Application", inst.getProgramId());
    }

    @Test
    public void testGlobalConfigPresence() throws Exception {
        final HtmlPage page = getWebClient().goTo("configure");
        jenkins.assertXPath(page,
                "//tr[@name='de-tracetronic-jenkins-plugins-ecutest-tool-installation-ETInstallation']");
    }

    @Test
    public void testFormRoundTrip() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
                .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", Collections
                .singletonList(new ETToolProperty("ECU-TEST6.Application"))));

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
    }
}
