/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.snakeyaml.Yaml;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link ConfigurationAsCode} compatibility.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ConfigurationAsCodeIT extends IntegrationTestBase {

    @Test
    public void testImportConfiguration() throws Exception {
        final URL yamlConfig = this.getClass().getResource("configuration-as-code.yml");
        System.out.println(yamlConfig);
        ConfigurationAsCode.get().configure(yamlConfig.toString());

        ETInstallation[] installations = jenkins.jenkins.getDescriptorByType(ETInstallation.DescriptorImpl.class).getInstallations();

        assertThat(installations[0].getName(), is("ECU-TEST"));
        assertThat(installations[0].getHome(), is("C:\\ECU-TEST"));
        assertThat(installations[0].getProgId(), is(ETComProperty.DEFAULT_PROG_ID));
        assertThat(installations[0].getTimeout(), is(ETComProperty.DEFAULT_TIMEOUT));
        assertThat(installations[0].getProperties(), empty());

        assertThat(installations[1].getName(), is("ECU-TEST 7.2"));
        assertThat(installations[1].getHome(), is("C:\\Program Files\\ECU-TEST 7.2"));
        assertThat(installations[1].getProgId(), is("ECU-TEST.Application.7.2"));
        assertThat(installations[1].getTimeout(), is(60));
        assertThat(installations[1].getProperties().size(), is(1));
        assertEquals(installations[1].getProperties().get(0).getClass(), ETToolProperty.class);
        assertThat(((ETToolProperty) installations[1].getProperties().get(0)).getProgId(), is("ECU-TEST.Application.7.2"));
        assertThat(((ETToolProperty) installations[1].getProperties().get(0)).getTimeout(), is(60));

        assertThat(installations.length, is(2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExportConfiguration() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST",
            Collections.singletonList(new ETToolProperty("ECU-TEST.Application", 120))));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ConfigurationAsCode.get().export(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Map<String, Object> yamlConfig = new Yaml().load(inputStream);

        Map<String, Object> tools = (Map<String, Object>) yamlConfig.get("tool");
        assertNotNull(tools);

        Map<String, Object> tool = (Map<String, Object>) tools.get("ecu-test");
        assertNotNull(tool);

        List<Map<String, Object>> installations = (List<Map<String, Object>>) (tool.get("installations"));
        assertThat(installations.size(), is(1));

        Map<String, Object> installation = installations.get(0);
        assertThat(installation.get("name"), is("ECU-TEST"));
        assertThat(installation.get("home"), is("C:\\ECU-TEST"));

        List<Map<String, Object>> properties = (List<Map<String, Object>>) (installation.get("properties"));
        assertThat(properties.size(), is(1));

        Map<String, Object> property = properties.get(0);
        Map<String, Object> toolProperty = (Map<String, Object>) property.get("ecu-test-property");
        assertNotNull(toolProperty);

        assertThat(toolProperty.get("progId"), is("ECU-TEST.Application"));
        assertThat(toolProperty.get("timeout"), is(120));
    }
}
