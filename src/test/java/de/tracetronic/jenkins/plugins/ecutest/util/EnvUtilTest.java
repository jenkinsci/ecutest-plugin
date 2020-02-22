/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import hudson.EnvVars;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link EnvUtil}.
 */
public class EnvUtilTest {

    @Test
    public void testDefaultIfEmptyEnvVar() {
        final String expanded = EnvUtil.expandEnvVar("", new EnvVars(), "default");
        assertEquals("Should expand to default value if empty", "default", expanded);
    }

    @Test
    public void testDefaultIfNullEnvVar() {
        final String expanded = EnvUtil.expandEnvVar(null, new EnvVars(), "default");
        assertEquals("Should expand to default value if null", "default", expanded);
    }

    @Test
    public void testExpandedEnvVar() {
        final String expanded = EnvUtil.expandEnvVar("${test}", new EnvVars("test", "expandedTest"), "default");
        assertEquals("Should expand using env vars", "expandedTest", expanded);
    }

    @Test
    public void testMultipleExpandedEnvVar() {
        final String expanded = EnvUtil.expandEnvVar("${test}${var}",
            new EnvVars("test", "expandedTest", "var", "123"), "default");
        assertEquals("Should expand using multiple env vars", "expandedTest123", expanded);
    }
}
