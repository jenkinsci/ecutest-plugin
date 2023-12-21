/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link StopETBuilder}.
 */
public class StopETBuilderTest {

    @Test
    public void testDefaultStep() {
        final StopETBuilder builder = new StopETBuilder("");
        assertBuilder(builder);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final StopETBuilder builder = new StopETBuilder(null);
        builder.setTimeout(null);
        assertBuilder(builder);
    }

    /**
     * Asserts the builder properties.
     *
     * @param builder the builder
     */
    private void assertBuilder(final StopETBuilder builder) {
        assertNotNull(builder);
        assertNotNull(builder.getToolName());
        assertTrue(builder.getToolName().isEmpty());
        assertNotNull(builder.getTimeout());
        assertEquals(String.valueOf(builder.getDefaultTimeout()), builder.getTimeout());
    }
}
