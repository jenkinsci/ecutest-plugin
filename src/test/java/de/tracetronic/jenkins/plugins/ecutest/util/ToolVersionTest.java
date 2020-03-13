/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ToolVersion}.
 */
public class ToolVersionTest {

    @Test
    public void testZeroConstructor() {
        final ToolVersion toolVersion = new ToolVersion(0, 0, 0, "0");
        assertEquals("Check zero-ed version", "0.0.0.0", toolVersion.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeConstructor() {
        new ToolVersion(-1, -1, -1);
    }

    @Test
    public void testParseValidToolVersion() {
        final ToolVersion expectedToolVersion = new ToolVersion(1, 2, 3, "4");
        final ToolVersion parsedToolVersion = ToolVersion.parse("1.2.3.4");
        assertEquals("Check parsed version", expectedToolVersion, parsedToolVersion);
    }

    @Test
    public void testParseHighToolVersion() {
        final ToolVersion expectedToolVersion = new ToolVersion(1000, 2000, 3000, "4000");
        final ToolVersion parsedToolVersion = ToolVersion.parse("1000.2000.3000.4000");
        assertEquals("Check parsed high version", expectedToolVersion, parsedToolVersion);
    }

    @Test
    public void testParseValidHashtagToolVersion() {
        final ToolVersion expectedToolVersion = new ToolVersion(1, 2, 3, "4");
        final ToolVersion parsedToolVersion = ToolVersion.parse("1.2.3#4");
        assertEquals("Check parsed hashtag version", expectedToolVersion, parsedToolVersion);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidToolVersion() throws IllegalArgumentException {
        ToolVersion.parse("a.b.c.d");
    }

    @Test
    public void testCompareSameToolVersions() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        final ToolVersion sameToolVersion = new ToolVersion(1, 2, 3, "4");
        assertEquals("Tool versions compare to equals", 0, toolVersion.compareTo(sameToolVersion));
    }

    @Test
    public void testCompareLessThanToolVersions() {
        final ToolVersion toolVersion = new ToolVersion(4, 3, 2, "1");
        final ToolVersion lesserToolVersion = new ToolVersion(1, 2, 3, "4");
        assertEquals("Tool versions compare to less than", -1, lesserToolVersion.compareTo(toolVersion));
    }

    @Test
    public void testCompareGreaterThanToolVersions() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        final ToolVersion greaterToolVersion = new ToolVersion(4, 3, 2, "1");
        assertEquals("Tool versions compare to greater than", 1, greaterToolVersion.compareTo(toolVersion));
    }

    @Test
    public void testCompareWithoutMicro() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        final ToolVersion sameToolVersion = new ToolVersion(1, 2, 0, "0");
        assertEquals("Tool versions compare without micro to equals", 0,
            sameToolVersion.compareWithoutMicroTo(toolVersion));
    }

    @Test
    public void testCompareWithoutQualifier() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        final ToolVersion sameToolVersion = new ToolVersion(1, 2, 3, "0");
        assertEquals("Tool versions compare without qualifier to equals", 0,
            sameToolVersion.compareWithoutQualifierTo(toolVersion));
    }

    @Test
    public void testToString() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        assertThat(toolVersion.toString(), is("1.2.3.4"));
    }

    @Test
    public void testToMicroString() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        assertThat(toolVersion.toMicroString(), is("1.2.3"));
    }

    @Test
    public void testToMinorString() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, "4");
        assertThat(toolVersion.toMinorString(), is("1.2"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ToolVersion.class).verify();
    }
}
