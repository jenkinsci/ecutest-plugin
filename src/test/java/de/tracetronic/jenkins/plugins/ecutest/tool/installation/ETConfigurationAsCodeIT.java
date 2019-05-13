/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.Configurator;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.casc.model.Sequence;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * {@link ConfigurationAsCode} compatibility tests for {@link ETInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETConfigurationAsCodeIT {

    @Rule
    public JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("configuration-as-code.yml")
    public void testImportConfiguration() {
        final ETInstallation.DescriptorImpl descriptor = jenkins.jenkins.getDescriptorByType(
            ETInstallation.DescriptorImpl.class);
        assertThat(descriptor, notNullValue());

        final ETInstallation[] installations = descriptor.getInstallations();
        assertThat(installations.length, is(2));

        ETInstallation installation = installations[0];
        assertThat(installation.getName(), is("ECU-TEST"));
        assertThat(installation.getHome(), is("C:\\ECU-TEST"));
        assertThat(installation.getProgId(), is(ETComProperty.DEFAULT_PROG_ID));
        assertThat(installation.getTimeout(), is(ETComProperty.DEFAULT_TIMEOUT));
        assertThat(installation.getProperties(), empty());

        installation = installations[1];
        assertThat(installation.getName(), is("ECU-TEST 7.2"));
        assertThat(installation.getHome(), is("C:\\Program Files\\ECU-TEST 7.2"));
        assertThat(installation.getProgId(), is("ECU-TEST.Application.7.2"));
        assertThat(installation.getTimeout(), is(60));
        assertThat(installation.getProperties().size(), is(1));
        assertEquals(installation.getProperties().get(0).getClass(), ETToolProperty.class);
        assertThat(((ETToolProperty) installation.getProperties().get(0)).getProgId(), is("ECU-TEST.Application.7.2"));
        assertThat(((ETToolProperty) installation.getProperties().get(0)).getTimeout(), is(60));
    }

    @Test
    @SuppressWarnings("unchecked")
    @ConfiguredWithCode("configuration-as-code.yml")
    public void testExportConfiguration() throws Exception {
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);

        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        final ConfigurationContext context = new ConfigurationContext(registry);
        final Configurator configurator = context.lookupOrFail(ETInstallation.DescriptorImpl.class);

        final CNode node = configurator.describe(etDescriptor, context);
        assertThat(node, notNullValue());
        assertThat(node, instanceOf(Mapping.class));

        final Sequence installations = node.asMapping().get("installations").asSequence();
        assertThat(installations.size(), is(2));

        Mapping installation = installations.get(0).asMapping();
        assertThat(installation.getScalarValue("name"), equalTo("ECU-TEST"));
        assertThat(installation.getScalarValue("home"), equalTo("C:\\ECU-TEST"));

        Sequence properties = installation.get("properties").asSequence();
        assertThat(properties, empty());

        installation = installations.get(1).asMapping();
        assertThat(installation.getScalarValue("name"), equalTo("ECU-TEST 7.2"));
        assertThat(installation.getScalarValue("home"), equalTo("C:\\Program Files\\ECU-TEST 7.2"));

        properties = installation.get("properties").asSequence();
        final Mapping property = properties.get(0).asMapping().get("ecu-test-property").asMapping();
        assertThat(property.getScalarValue("progId"), equalTo("ECU-TEST.Application.7.2"));
        assertThat(property.getScalarValue("timeout"), equalTo("60"));
    }
}
