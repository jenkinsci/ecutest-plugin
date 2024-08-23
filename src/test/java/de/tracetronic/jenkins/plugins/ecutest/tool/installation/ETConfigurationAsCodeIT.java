/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
import io.jenkins.plugins.casc.misc.Util;
import io.jenkins.plugins.casc.model.CNode;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * {@link ConfigurationAsCode} compatibility tests for {@link ETInstallation}.
 */
public class ETConfigurationAsCodeIT {

    @ClassRule
    @ConfiguredWithCode("casc.yml")
    public static JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    @Test
    public void testImportConfiguration() {
        final ETInstallation.DescriptorImpl descriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        assertThat(descriptor, notNullValue());

        final ETInstallation[] installations = descriptor.getInstallations();
        assertThat(installations.length, is(2));

        ETInstallation installation = installations[0];
        assertThat(installation.getName(), is("ecu.test"));
        assertThat(installation.getHome(), is("C:\\ecu.test"));
        assertThat(installation.getProgId(), is(ETComProperty.DEFAULT_PROG_ID));
        assertThat(installation.getTimeout(), is(ETComProperty.DEFAULT_TIMEOUT));
        assertThat(installation.getProperties(), empty());

        installation = installations[1];
        assertThat(installation.getName(), is("ecu.test 2024.1"));
        assertThat(installation.getHome(), is("C:\\Program Files\\ecu.test 2024.1"));
        assertThat(installation.getProgId(), is("ecu.test.Application.2024.1"));
        assertThat(installation.getTimeout(), is(60));
        assertThat(installation.getProperties().size(), is(1));
        assertThat(installation.getProperties().get(0), is(instanceOf(ETToolProperty.class)));
        assertThat(((ETToolProperty) installation.getProperties().get(0)).getProgId(), is("ecu.test.Application.2024.1"));
        assertThat(((ETToolProperty) installation.getProperties().get(0)).getTimeout(), is(60));
        assertThat(((ETToolProperty) installation.getProperties().get(0)).isRegisterComServer(), is(true));
    }

    @Test
    public void testExportConfiguration() throws Exception {
        final ETInstallation.DescriptorImpl descriptor = jenkins.jenkins
            .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        assertThat(descriptor, notNullValue());

        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        final ConfigurationContext context = new ConfigurationContext(registry);
        final Configurator<ETInstallation.DescriptorImpl> configurator =
            context.lookupOrFail(ETInstallation.DescriptorImpl.class);
        final CNode node = configurator.describe(descriptor, context);

        final String exported = Util.toYamlString(node);
        final String expected = Util.toStringFromYamlFile(this, "casc-export.yml");

        assertThat(exported, is(expected));
    }
}
