/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ETInstallation}.
 */
public class ETInstallationTest {

    @Test
    public void testInstallation() {
        final ETInstallation inst = new ETInstallation("ecu.test", "C:\\ECU-TEST",
            Collections.emptyList());
        assertNotNull(inst);
        assertEquals("ecu.test", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST.Application", inst.getProgId());
        assertEquals(0, inst.getTimeout());
        assertFalse(inst.isRegisterComServer());
    }

    @Test
    public void testInstallationWithCustomSettings() {
        final ETInstallation inst = new ETInstallation("ecu.test", "C:\\ECU-TEST",
            Collections.singletonList(new ETToolProperty("ECU-TEST6.Application", 120, true)));
        assertNotNull(inst);
        assertEquals("ecu.test", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals(1, inst.getProperties().size());
        assertEquals("ECU-TEST6.Application", inst.getProgId());
        assertEquals(120, inst.getTimeout());
        assertTrue(inst.isRegisterComServer());
    }
}
