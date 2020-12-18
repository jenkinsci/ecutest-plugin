/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import org.junit.Test;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ATXInstallationIT extends IntegrationTestBase {

    @Test
    @LocalData
    public void testServerMigration() {
        final ATXPublisher publisher = new ATXPublisher("TEST-GUIDE");
        final ATXInstallation installation = publisher.getInstallation();
        assertNotNull(installation);

        assertMigratedSetting(installation, "httpProxy", "http://user:pass@10.10.10.2:8080");
        assertMigratedSetting(installation, "httpsProxy", "http://user:pass@10.10.10.2:8080");
        assertMigratedSetting(installation, "uploadAuthenticationKey", "API-TOKEN");
    }

    private void assertMigratedSetting(final ATXInstallation installation, final String settingName,
                                       final String expectedValue) {
        Optional<ATXSetting<?>> setting = installation.getConfig().getSettingByName(settingName);
        assertThat(setting.isPresent(), is(true));
        assertThat(setting.get(), instanceOf(ATXSecretSetting.class));
        assertThat(((ATXSecretSetting) setting.get()).getSecretValue(), is(expectedValue));
    }
}
