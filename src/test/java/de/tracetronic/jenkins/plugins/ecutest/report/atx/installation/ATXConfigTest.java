/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
