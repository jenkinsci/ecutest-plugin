/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ETToolProperty}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETToolPropertyTest {

    @Test
    public void testBlankConstructor() {
        final ETToolProperty property = new ETToolProperty("", 0);
        assertEquals(ETToolProperty.DescriptorImpl.getDefaultProgId(), property.getProgId());
        assertEquals(ETToolProperty.DescriptorImpl.getDefaultTimeout(), property.getTimeout());
    }

    @Test
    public void testNullConstructor() {
        final ETToolProperty property = new ETToolProperty(null, 0);
        assertEquals(ETToolProperty.DescriptorImpl.getDefaultProgId(), property.getProgId());
        assertEquals(0, property.getTimeout());
    }

    @Test
    public void testConstructor() {
        final ETToolProperty property = new ETToolProperty("ECU-TEST6.Application", 120);
        assertEquals("ECU-TEST6.Application", property.getProgId());
        assertEquals(120, property.getTimeout());
    }
}
