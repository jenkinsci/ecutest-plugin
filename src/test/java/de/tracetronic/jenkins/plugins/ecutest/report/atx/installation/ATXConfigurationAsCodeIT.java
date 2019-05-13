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
    public void testImportConfiguration() {
        final ATXInstallation.DescriptorImpl descriptor = jenkins.jenkins.getDescriptorByType(
            ATXInstallation.DescriptorImpl.class);
        assertThat(descriptor, notNullValue());

        final ATXInstallation[] installations = descriptor.getInstallations();
        assertThat(installations.length, is(1));

        ATXInstallation installation = installations[0];
        assertThat(installation.getName(), is("TEST-GUIDE"));
        assertThat(installation.getToolName(), is("ECU-TEST"));

        ATXConfig config = installation.getConfig();
        assertThat(config, notNullValue());
        assertThat(config.getSettings(), not(empty()));
        assertThat(config.getCustomSettings(), not(empty()));

        assertThat(config.getSettingValueByGroup("uploadToServer", ATXSetting.SettingsGroup.UPLOAD), is(true));
        assertThat(config.getSettingValueByGroup("serverURL", ATXSetting.SettingsGroup.UPLOAD), is("127.0.0.1"));

        assertThat(config.getCustomSettings().get(0), is(instanceOf(ATXCustomBooleanSetting.class)));
        assertThat(config.getCustomSettings().get(0).getName(), is("customOption"));
        assertThat(((ATXCustomBooleanSetting) config.getCustomSettings().get(0)).isChecked(), is(true));
        assertThat(config.getCustomSettings().get(1), is(instanceOf(ATXCustomTextSetting.class)));
        assertThat(config.getCustomSettings().get(1).getName(), is("customLabel"));
        assertThat(((ATXCustomTextSetting) config.getCustomSettings().get(1)).getValue(), is("test"));
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
        Sequence customSettings = installation.get("config").asMapping().get("customSettings").asSequence();
        assertThat(customSettings, not(empty()));

        Mapping uploadSetting = settings.get(0).asMapping().get("atx-boolean-setting").asMapping();
        assertThat(uploadSetting.getScalarValue("name"), is("uploadToServer"));
        assertThat(uploadSetting.getScalarValue("value"), is("true"));
        Mapping serverSetting = settings.get(1).asMapping().get("atx-text-setting").asMapping();
        assertThat(serverSetting.getScalarValue("name"), is("serverURL"));
        assertThat(serverSetting.getScalarValue("value"), is("127.0.0.1"));

        Mapping booleanSetting = customSettings.get(0).asMapping().get("atx-custom-boolean-setting").asMapping();
        assertThat(booleanSetting.getScalarValue("name"), is("customOption"));
        assertThat(booleanSetting.getScalarValue("checked"), is("true"));
        Mapping textSetting = customSettings.get(1).asMapping().get("atx-custom-text-setting").asMapping();
        assertThat(textSetting.getScalarValue("name"), is("customLabel"));
        assertThat(textSetting.getScalarValue("value"), is("test"));
    }
}
