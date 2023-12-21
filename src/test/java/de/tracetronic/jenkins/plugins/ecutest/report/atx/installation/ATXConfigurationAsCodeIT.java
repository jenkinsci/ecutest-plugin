/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
import io.jenkins.plugins.casc.misc.Util;
import io.jenkins.plugins.casc.model.CNode;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * {@link ConfigurationAsCode} compatibility tests for {@link ATXInstallation}.
 */
public class ATXConfigurationAsCodeIT {

    @ClassRule
    @ConfiguredWithCode("casc.yml")
    public static JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    @Test
    public void testImportConfiguration() {
        final ATXInstallation.DescriptorImpl descriptor = jenkins.jenkins.getDescriptorByType(
            ATXInstallation.DescriptorImpl.class);
        assertThat(descriptor, notNullValue());

        final ATXInstallation[] installations = descriptor.getInstallations();
        assertThat(installations.length, is(1));

        ATXInstallation installation = installations[0];
        assertThat(installation.getName(), is("test.guide"));
        assertThat(installation.getToolName(), is("ecu.test"));

        ATXConfig config = installation.getConfig();
        assertThat(config, notNullValue());
        assertThat(config.getSettings(), not(empty()));
        assertThat(config.getCustomSettings(), not(empty()));

        assertThat(config.getSettingValueByGroup("uploadToServer", ATXSetting.SettingsGroup.UPLOAD), is(true));
        assertThat(config.getSettingValueByGroup("serverURL", ATXSetting.SettingsGroup.CONNECTION), is("127.0.0.1"));

        assertThat(config.getCustomSettings().get(0), is(instanceOf(ATXCustomBooleanSetting.class)));
        assertThat(config.getCustomSettings().get(0).getName(), is("customOption"));
        assertThat(((ATXCustomBooleanSetting) config.getCustomSettings().get(0)).isChecked(), is(true));
        assertThat(config.getCustomSettings().get(1), is(instanceOf(ATXCustomTextSetting.class)));
        assertThat(config.getCustomSettings().get(1).getName(), is("customLabel"));
        assertThat(((ATXCustomTextSetting) config.getCustomSettings().get(1)).getValue(), is("test"));
    }

    @Test
    public void testExportConfiguration() throws Exception {
        final ATXInstallation.DescriptorImpl descriptor = jenkins.jenkins
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);

        final ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        final ConfigurationContext context = new ConfigurationContext(registry);
        final Configurator<Object> configurator = context.lookupOrFail(ATXInstallation.DescriptorImpl.class);
        final CNode node = configurator.describe(descriptor, context);

        final String exported = Util.toYamlString(node);
        final String expected = Util.toStringFromYamlFile(this, "casc-export.yml");

        assertThat(exported, is(expected));
    }
}
