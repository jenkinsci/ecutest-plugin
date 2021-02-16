/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.EnvVars;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link ReportGeneratorSetting}.
 */
public class ReportGeneratorSettingTest {

    @Test
    public void testNullConstructor() {
        final ReportGeneratorSetting setting = new ReportGeneratorSetting(null, null);
        assertThat(setting.getName(), is(""));
        assertThat(setting.getValue(), is(""));
    }

    @Test
    public void testConstructor() {
        final ReportGeneratorSetting setting = new ReportGeneratorSetting("name", "value");
        assertThat(setting.getName(), is("name"));
        assertThat(setting.getValue(), is("value"));
    }

    @Test
    public void testExpand() {
        final ReportGeneratorSetting setting = new ReportGeneratorSetting("${NAME}", "${VALUE}");
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final ReportGeneratorSetting expSetting = setting.expand(envVars);
        assertThat(expSetting.getName(), is("name"));
        assertThat(expSetting.getValue(), is("value"));
    }
}
