/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * {@link ConfigurationAsCode} compatibility tests for {@link ATXInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXConfigurationAsCodeIT {

    @Rule
    public JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("configuration-as-code.yml")
    public void testImportConfiguration() throws Exception {
        final ATXInstallation.DescriptorImpl descriptor = jenkins.jenkins.getDescriptorByType(
            ATXInstallation.DescriptorImpl.class);
        assertThat(descriptor, notNullValue());

        final ATXInstallation[] installations = descriptor.getInstallations();
        assertThat(installations.length, is(1));

        ATXInstallation installation = installations[0];
        assertThat(installation.getName(), is("TEST-GUIDE"));
        assertThat(installation.getToolName(), is("ECU-TEST"));
        assertThat(installation.getConfig(), notNullValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    @ConfiguredWithCode("configuration-as-code.yml")
    public void testExportConfiguration() throws Exception {
        final ATXInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);

        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        final ConfigurationContext context = new ConfigurationContext(registry);
        final Configurator configurator = context.lookupOrFail(ATXInstallation.DescriptorImpl.class);

        final CNode node = configurator.describe(etDescriptor, context);
        assertThat(node, notNullValue());
        assertThat(node, instanceOf(Mapping.class));

        final Sequence installations = node.asMapping().get("installations").asSequence();
        assertThat(installations.size(), is(1));

        Mapping installation = installations.get(0).asMapping();
        assertThat(installation.getScalarValue("name"), equalTo("TEST-GUIDE"));
        assertThat(installation.getScalarValue("toolName"), equalTo("ECU-TEST"));

        Sequence settings = installation.get("config").asMapping().get("settings").asSequence();
        assertThat(settings, not(empty()));
    }
}
