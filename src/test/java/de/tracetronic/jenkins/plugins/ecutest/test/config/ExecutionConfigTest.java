/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExecutionConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExecutionConfigTest {

    @Test
    public void testIntConstructor() {
        final ExecutionConfig config = new ExecutionConfig(60, true, true);
        assertThat(config.getParsedTimeout(), is(60));
        assertTrue(config.isStopOnError());
        assertTrue(config.isCheckTestFile());
    }

    @Test
    public void testStringConstructor() {
        final ExecutionConfig config = new ExecutionConfig("60", true, true);
        assertThat(config.getParsedTimeout(), is(60));
    }

    @Test
    public void testInvalidConstructor() {
        final ExecutionConfig config = new ExecutionConfig("abc", true, true);
        assertThat(config.getParsedTimeout(), is(ExecutionConfig.getDefaultTimeout()));
    }

    @Test
    public void testExpand() {
        final ExecutionConfig config = new ExecutionConfig("${TIMEOUT}", true, true);
        final EnvVars envVars = new EnvVars();
        envVars.put("TIMEOUT", "60");
        assertThat(config.expand(envVars).getParsedTimeout(), is(60));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ExecutionConfig.class).verify();
    }
}
