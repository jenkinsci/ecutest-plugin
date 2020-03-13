/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link StartTSBuilder}.
 */
public class StartTSBuilderTest {

    @Test
    public void testDefaultStep() {
        final StartTSBuilder builder = new StartTSBuilder("");
        assertBuilder(builder);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final StartTSBuilder builder = new StartTSBuilder(null);
        builder.setToolLibsIni(null);
        builder.setTcpPort(null);
        builder.setTimeout(null);
        builder.setKeepInstance(false);
        assertBuilder(builder);
    }

    /**
     * Asserts the builder properties.
     *
     * @param builder the builder
     */
    private void assertBuilder(final StartTSBuilder builder) {
        assertNotNull(builder);
        assertNotNull(builder.getToolName());
        assertTrue(builder.getToolName().isEmpty());
        assertNotNull(builder.getTimeout());
        assertEquals(String.valueOf(builder.getDefaultTimeout()), builder.getTimeout());
        assertNotNull(builder.getToolLibsIni());
        assertTrue(builder.getToolLibsIni().isEmpty());
        assertNotNull(builder.getTcpPort());
        assertEquals(String.valueOf(builder.getDefaultTcpPort()), builder.getTcpPort());
        assertFalse(builder.isKeepInstance());
    }
}
