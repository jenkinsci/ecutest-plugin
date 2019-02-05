/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TestConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestConfigTest {

    @Test
    public void testNullConstructor() {
        final TestConfig config = new TestConfig(null, null, false, false, false, null);
        assertThat(config.getTbcFile(), is(""));
        assertThat(config.getTcfFile(), is(""));
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
        assertFalse(config.isKeepConfig());
        assertNotNull(config.getConstants());
    }

    @Test
    public void testEmptyConstructor() {
        final TestConfig config = new TestConfig(null, null, false, false);
        assertThat(config.getTbcFile(), is(""));
        assertThat(config.getTcfFile(), is(""));
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
        assertFalse(config.isKeepConfig());
        assertTrue(config.getConstants().isEmpty());
    }

    @Test
    public void testEmptyConstants() {
        final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();
        constants.add(new GlobalConstant(" ", " "));
        final TestConfig config = new TestConfig(null, null, false, false, false, constants);
        assertTrue(config.getConstants().isEmpty());
    }

    @Test
    public void testExpand() {
        final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();
        constants.add(new GlobalConstant("${NAME}", "${VALUE}"));
        final TestConfig config = new TestConfig("${TBC}", "${TCF}", false, false, false, constants);
        final EnvVars envVars = new EnvVars();
        envVars.put("TBC", "test.tbc");
        envVars.put("TCF", "test.tcf");
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final TestConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTbcFile(), is("test.tbc"));
        assertThat(expConfig.getTcfFile(), is("test.tcf"));
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
        assertFalse(config.isKeepConfig());
        assertThat(expConfig.getConstants().get(0).getName(), is("name"));
        assertThat(expConfig.getConstants().get(0).getValue(), is("value"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(TestConfig.class).verify();
    }
}
