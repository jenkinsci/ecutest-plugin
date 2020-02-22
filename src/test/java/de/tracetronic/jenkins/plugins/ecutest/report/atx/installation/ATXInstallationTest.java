/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXInstallation}.
 */
public class ATXInstallationTest {

    @Test
    public void testNullConfig() {
        final ATXInstallation inst = new ATXInstallation("TG", "ET", null);
        assertThat(inst.getName(), is("TG"));
        assertThat(inst.getToolName(), is("ET"));
        assertNotNull(inst.getConfig());
    }

    @Test
    public void testEmptySettings() {
        final ATXConfig config = new ATXConfig(Collections.emptyList(), Collections.emptyList());
        final ATXInstallation inst = new ATXInstallation("TG", "ET", config);
        assertNotNull(inst.getConfig());
        assertTrue(inst.getConfig().getSettings().isEmpty());
    }

    @Test
    public void testEmptyCustomSettings() {
        final ATXConfig config = new ATXConfig(Collections.emptyList(), Collections.emptyList());
        final ATXInstallation inst = new ATXInstallation("TG", "ET", config);
        assertNotNull(inst.getConfig());
        assertTrue(inst.getConfig().getCustomSettings().isEmpty());
    }

    @Test
    public void testGetAllInstallations() {
        assertThat(ATXInstallation.all(), emptyArray());
    }

    @Test
    public void testGetUndefinedInstallation() {
        assertNull(ATXInstallation.get("undefined"));
    }
}
