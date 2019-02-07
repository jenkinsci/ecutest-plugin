/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@SuppressWarnings("rawtypes")
public class ATXConfigTest {

    @Test
    public void testNullConfigMap() {
        final ATXConfig config = new ATXConfig(null, null);
        assertTrue(config.getConfigMap().isEmpty());
        assertTrue(config.getCustomSettings().isEmpty());
    }

    @Test
    public void testEmptyConfigMap() {
        final ATXConfig config = new ATXConfig(Collections.emptyMap(),
            Collections.emptyList());
        assertTrue(config.getConfigMap().isEmpty());
        assertTrue(config.getCustomSettings().isEmpty());
    }

    @Test
    public void testOwnConfigMap() {
        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("testConfig", new ArrayList<ATXSetting>());
        final ATXConfig config = new ATXConfig(configMap, null);
        assertNotNull(config.getConfigByName("testConfig"));
    }

    @Test
    public void testDefaultConfigMap() {
        final ATXConfig config = new ATXConfig();
        assertTrue(!config.getConfigMap().isEmpty());
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
        clone.getConfigMap().clear();
        assertThat(clone.getConfigMap(), is(not(config.getConfigMap())));
    }

    @Test
    public void testGetConfigByName() {
        final ATXConfig config = new ATXConfig();
        assertNotNull(config.getConfigByName("uploadConfig"));
    }

    @Test
    public void testGetInvalidConfigByName() {
        final ATXConfig config = new ATXConfig(null, null);
        assertNull(config.getConfigByName("invalid"));
    }

    @Test
    public void testGetSettingByName() {
        final ATXConfig config = new ATXConfig();
        assertNotNull(config.getSettingByName("serverPort"));
    }

    @Test
    public void testGetInvalidSettingByName() {
        final ATXConfig config = new ATXConfig(null, null);
        assertNull(config.getSettingByName("invalid"));
    }

    @Test
    public void testGetNotExistingSettingByName() {
        final ATXConfig config = new ATXConfig();
        assertNull(config.getSettingByName("notexisting"));
    }

    @Test
    public void testGetSettingValueByName() {
        final ATXConfig config = new ATXConfig();
        assertThat(config.getSettingValueByName("serverPort", config.getConfigByName("uploadConfig")),
            is("8085"));
    }

    @Test
    public void testGetInvalidSettingValueByName() {
        final ATXConfig config = new ATXConfig(null, null);
        assertThat(config.getSettingValueByName("invalid", config.getConfigByName("invalid")), nullValue());
    }

    @Test
    public void testGetEmptySettingValueByName() {
        final ATXConfig config = new ATXConfig(null, null);
        assertThat(config.getSettingValueByName("empty", new ArrayList<ATXSetting>()), nullValue());
    }
}
