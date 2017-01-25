/*
 * Copyright (c) 2015 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tracetronic.jenkins.plugins.ecutest;

import static org.junit.Assert.assertEquals;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;

/**
 * Unit tests for {@link ToolVersion}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolVersionTest {

    @Test
    public void testZeroConstructor() {
        final ToolVersion toolVersion = new ToolVersion(0, 0, 0, 0);
        assertEquals("Check zero-ed version", "0.0.0.0", toolVersion.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeConstructor() {
        new ToolVersion(-1, -1, -1, -1);
    }

    @Test
    public void testParseValidToolVersion() {
        final ToolVersion expectedToolVersion = new ToolVersion(1, 2, 3, 4);
        final ToolVersion parsedToolVersion = ToolVersion.parse("1.2.3.4");
        assertEquals("Check parsed version", expectedToolVersion, parsedToolVersion);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidToolVersion() throws IllegalArgumentException {
        ToolVersion.parse("a.b.c.d");
    }

    @Test
    public void testCompareSameToolVersions() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, 4);
        final ToolVersion sameToolVersion = new ToolVersion(1, 2, 3, 4);
        assertEquals("Tool versions compare to equals", 0, toolVersion.compareTo(sameToolVersion));
    }

    @Test
    public void testCompareLessThanToolVersions() {
        final ToolVersion toolVersion = new ToolVersion(4, 3, 2, 1);
        final ToolVersion lesserToolVersion = new ToolVersion(1, 2, 3, 4);
        assertEquals("Tool versions compare to less than", -1, lesserToolVersion.compareTo(toolVersion));
    }

    @Test
    public void testCompareGreaterThanToolVersions() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, 4);
        final ToolVersion greaterToolVersion = new ToolVersion(4, 3, 2, 1);
        assertEquals("Tool versions compare to greater than", 1, greaterToolVersion.compareTo(toolVersion));
    }

    @Test
    public void testCompareWithouQualifier() {
        final ToolVersion toolVersion = new ToolVersion(1, 2, 3, 4);
        final ToolVersion sameToolVersion = new ToolVersion(1, 2, 3, 0);
        assertEquals("Tool versions compare without qualifier to equals", 0,
                sameToolVersion.compareWithoutQualifierTo(toolVersion));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ToolVersion.class).verify();
    }
}
