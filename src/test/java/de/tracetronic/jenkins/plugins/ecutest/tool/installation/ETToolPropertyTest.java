/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ETToolProperty}.
 */
public class ETToolPropertyTest {

    @Test
    public void testBlankConstructor() {
        final ETToolProperty property = new ETToolProperty("", 0, true);
        assertEquals(ETToolProperty.DescriptorImpl.getDefaultProgId(), property.getProgId());
        assertEquals(ETToolProperty.DescriptorImpl.getDefaultTimeout(), property.getTimeout());
        assertTrue(property.isRegisterComServer());
    }

    @Test
    public void testNullConstructor() {
        final ETToolProperty property = new ETToolProperty(null, 0, true);
        assertEquals(ETToolProperty.DescriptorImpl.getDefaultProgId(), property.getProgId());
        assertEquals(0, property.getTimeout());
        assertTrue(property.isRegisterComServer());
    }

    @Test
    public void testConstructor() {
        final ETToolProperty property = new ETToolProperty("ECU-TEST6.Application", 120, true);
        assertEquals("ECU-TEST6.Application", property.getProgId());
        assertEquals(120, property.getTimeout());
        assertTrue(property.isRegisterComServer());
    }
}
