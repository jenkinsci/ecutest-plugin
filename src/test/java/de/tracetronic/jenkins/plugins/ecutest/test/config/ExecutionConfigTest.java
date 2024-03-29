/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExecutionConfig}.
 */
public class ExecutionConfigTest {

    @Test
    public void testIntConstructor() {
        final ExecutionConfig config = new ExecutionConfig(60, true, true, false);
        assertThat(config.getParsedTimeout(), is(60));
        assertTrue(config.isStopOnError());
        assertTrue(config.isCheckTestFile());
        assertFalse(config.isRecordWarnings());
    }

    @Test
    public void testStringConstructor() {
        final ExecutionConfig config = new ExecutionConfig("60", true, true, false);
        assertThat(config.getParsedTimeout(), is(60));
    }

    @Test
    public void testInvalidConstructor() {
        final ExecutionConfig config = new ExecutionConfig("abc", true, true, false);
        assertThat(config.getParsedTimeout(), is(ExecutionConfig.getDefaultTimeout()));
    }

    @Test
    public void testExpand() {
        final ExecutionConfig config = new ExecutionConfig("${TIMEOUT}", true, true, false);
        final EnvVars envVars = new EnvVars();
        envVars.put("TIMEOUT", "60");
        assertThat(config.expand(envVars).getParsedTimeout(), is(60));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ExecutionConfig.class).verify();
    }
}
