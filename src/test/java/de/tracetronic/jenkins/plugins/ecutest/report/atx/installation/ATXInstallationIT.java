/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import org.junit.Test;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;

public class ATXInstallationIT extends IntegrationTestBase {

    @Test
    @LocalData
    public void testServerMigration() {
        final ATXPublisher publisher = new ATXPublisher("TEST-GUIDE");
        final ATXInstallation installation = publisher.getInstallation();
        assertNotNull(installation);

        assertMigratedSecretSetting(installation, "httpProxy", "http://user:pass@10.10.10.2:8080");
        assertMigratedSecretSetting(installation, "httpsProxy", "http://user:pass@10.10.10.2:8080");
        assertMigratedSecretSetting(installation, "uploadAuthenticationKey", "API-TOKEN");

        assertMigratedBoolean2StringSetting(installation, "archiveRecordings", "False");
        assertMigratedBoolean2StringSetting(installation, "useSettingsFromServer", "Never");
    }

    private void assertMigratedSecretSetting(final ATXInstallation installation, final String settingName,
                                             final String expectedValue) {
        Optional<ATXSetting<?>> setting = installation.getConfig().getSettingByName(settingName);
        assertThat(setting.isPresent(), is(true));
        assertThat(setting.get(), instanceOf(ATXSecretSetting.class));
        assertThat(((ATXSecretSetting) setting.get()).getSecretValue(), is(expectedValue));
    }

    private void assertMigratedBoolean2StringSetting(final ATXInstallation installation, final String settingName,
                                             final String expectedValue) {
        Optional<ATXSetting<?>> setting = installation.getConfig().getSettingByName(settingName);
        assertThat(setting.isPresent(), is(true));
        assertThat(setting.get(), instanceOf(ATXTextSetting.class));
        assertThat(((ATXTextSetting) setting.get()).getValue(), is(expectedValue));
    }
}
