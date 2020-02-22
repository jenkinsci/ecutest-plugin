/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link StartETBuilder}.
 */
public class StartETBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final StartETBuilder builder = new StartETBuilder("");
        assertBuilder(builder);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final StartETBuilder builder = new StartETBuilder(null);
        builder.setWorkspaceDir(null);
        builder.setSettingsDir(null);
        builder.setTimeout(null);
        builder.setDebugMode(false);
        builder.setKeepInstance(false);
        builder.setUpdateUserLibs(false);
        assertBuilder(builder);
    }

    /**
     * Asserts the builder properties.
     *
     * @param builder the builder
     */
    private void assertBuilder(final StartETBuilder builder) {
        assertNotNull(builder);
        assertNotNull(builder.getToolName());
        assertTrue(builder.getToolName().isEmpty());
        assertNotNull(builder.getWorkspaceDir());
        assertTrue(builder.getWorkspaceDir().isEmpty());
        assertNotNull(builder.getSettingsDir());
        assertTrue(builder.getSettingsDir().isEmpty());
        assertNotNull(builder.getTimeout());
        assertEquals(String.valueOf(builder.getDefaultTimeout()), builder.getTimeout());
        assertFalse(builder.isDebugMode());
        assertFalse(builder.isKeepInstance());
        assertFalse(builder.isUpdateUserLibs());
    }
}
