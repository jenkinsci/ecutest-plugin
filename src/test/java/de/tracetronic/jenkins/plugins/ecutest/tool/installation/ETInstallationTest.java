/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import hudson.tools.ToolProperty;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link ETInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETInstallationTest {

    @Test
    public void testInstallation() {
        final ETInstallation inst = new ETInstallation("ECU-TEST", "C:\\ECU-TEST",
            Collections.emptyList());
        assertNotNull(inst);
        assertEquals("ECU-TEST", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals("ECU-TEST.Application", inst.getProgId());
    }

    @Test
    public void testInstallationWithCustomProgId() {
        final ETInstallation inst = new ETInstallation("ECU-TEST", "C:\\ECU-TEST",
            Collections.singletonList(new ETToolProperty("ECU-TEST6.Application", 120)));
        assertNotNull(inst);
        assertEquals("ECU-TEST", inst.getName());
        assertEquals("C:\\ECU-TEST", inst.getHome());
        assertEquals(1, inst.getProperties().size());
        assertEquals("ECU-TEST6.Application", inst.getProgId());
    }
}
