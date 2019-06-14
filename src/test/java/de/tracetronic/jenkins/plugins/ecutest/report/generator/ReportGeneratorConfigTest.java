/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.EnvVars;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ReportGeneratorConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorConfigTest {

    @Test
    public void testNullConstructor() {
        final ReportGeneratorConfig config = new ReportGeneratorConfig(null, null, false);
        assertNotNull(config.getName());
        assertNotNull(config.getSettings());
    }

    @Test
    public void testEmptyConstructor() {
        final ReportGeneratorConfig config = new ReportGeneratorConfig("", null, false);
        assertTrue(config.getName().isEmpty());
        assertNotNull(config.getSettings());
    }

    @Test
    public void testEmptySettings() {
        final List<ReportGeneratorSetting> settings = new ArrayList<ReportGeneratorSetting>();
        settings.add(new ReportGeneratorSetting(" ", " "));
        final ReportGeneratorConfig config = new ReportGeneratorConfig("", settings, false);
        assertTrue(config.getSettings().isEmpty());
    }

    @Test
    public void testExpand() {
        final List<ReportGeneratorSetting> settings = new ArrayList<ReportGeneratorSetting>();
        settings.add(new ReportGeneratorSetting("${NAME}", "${VALUE}"));
        final ReportGeneratorConfig config = new ReportGeneratorConfig("", settings, false);
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final ReportGeneratorConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getSettings().get(0).getName(), is("name"));
        assertThat(expConfig.getSettings().get(0).getValue(), is("value"));
    }
}
