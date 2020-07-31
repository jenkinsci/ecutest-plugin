/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXConfig}.
 */
public class ATXConfigTest {

    @Test
    public void testNullConfigMap() {
        final ATXConfig config = new ATXConfig(null, null);
        assertFalse(config.getSettings().isEmpty());
        assertTrue(config.getCustomSettings().isEmpty());
    }

    @Test
    public void testEmptyConfigMap() {
        final ATXConfig config = new ATXConfig(Collections.emptyList(), Collections.emptyList());
        assertTrue(config.getSettings().isEmpty());
        assertTrue(config.getCustomSettings().isEmpty());
    }

    @Test
    public void testDefaultConfigMap() {
        final ATXConfig config = new ATXConfig();
        assertFalse(config.getSettings().isEmpty());
    }

    @Test
    public void testClone() {
        final ATXConfig config = new ATXConfig(null, null);
        final ATXConfig clone = config.clone();
        assertThat(clone, not(sameInstance(config)));
    }

    @Test
    public void testManipulatedClone() {
        final ATXConfig config = new ATXConfig();
        final ATXConfig clone = config.clone();
        clone.getSettings().clear();
        assertThat(clone.getSettings(), is(not(config.getSettings())));
    }

    @Test
    public void testGetConfigByName() {
        final ATXConfig config = new ATXConfig();
        assertNotNull(config.getSettingsByGroup(ATXSetting.SettingsGroup.UPLOAD));
    }

    @Test
    public void testGetInvalidConfigByName() {
        final ATXConfig config = new ATXConfig(null, null);
        assertThat(config.getSettingsByGroup(null), Matchers.empty());
    }

    @Test
    public void testGetSettingByName() {
        final ATXConfig config = new ATXConfig();
        Optional<ATXSetting<?>> setting = config.getSettingByName("serverPort");
        assertThat(setting.isPresent(), is(true));
        assertThat(setting.get().value, is("8085"));
    }

    @Test
    public void testGetInvalidSettingByName() {
        final ATXConfig config = new ATXConfig(null, null);
        assertFalse(config.getSettingByName("invalid").isPresent());
    }

    @Test
    public void testGetNotExistingSettingByName() {
        final ATXConfig config = new ATXConfig();
        assertFalse(config.getSettingByName("notexisting").isPresent());
    }

    @Test
    public void testGetSettingValueByGroup() {
        final ATXConfig config = new ATXConfig();
        assertThat(config.getSettingValueBySettings("serverPort",
            config.getSettingsByGroup(ATXSetting.SettingsGroup.CONNECTION)), is("8085"));
    }

    @Test
    public void testGetInvalidSettingValueByGroup() {
        final ATXConfig config = new ATXConfig(null, null);
        assertThat(config.getSettingValueBySettings("invalid", config.getSettingsByGroup(null)), nullValue());
    }

    @Test
    public void testGetEmptySettingValueByGroup() {
        final ATXConfig config = new ATXConfig(null, null);
        assertThat(config.getSettingValueBySettings("empty", new ArrayList<>()), nullValue());
    }

    @Test
    public void testSetSettingValueByName() {
        final ATXConfig config = new ATXConfig(null, null);
        config.setSettingValueByName("uploadToServer", true);
        assertThat(config.getSettingValueByName("uploadToServer"), is(true));
    }
}
