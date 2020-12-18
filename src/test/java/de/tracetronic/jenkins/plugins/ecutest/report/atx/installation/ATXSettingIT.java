/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting.SettingsGroup;
import hudson.util.Secret;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link ATXSetting}.
 */
public class ATXSettingIT extends IntegrationTestBase {

    @Test
    public void testSecretType() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", Secret.fromString("defaultValue"));
        assertTrue(setting.isSecret());
    }

    @Test
    public void testCurrentValueForSecret() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", Secret.fromString("defaultValue"));
        setting.setValue(Secret.fromString("test"));
        assertThat(setting.getSecretValue(), is("test"));
    }

    @Test
    public void testDefaultValueForSecret() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", Secret.fromString("defaultValue"));
        assertThat(setting.getDefaultValue().getPlainText(), is("defaultValue"));
    }
}
