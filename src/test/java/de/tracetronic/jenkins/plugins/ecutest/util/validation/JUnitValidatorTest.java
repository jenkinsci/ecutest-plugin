/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import static org.junit.Assert.assertEquals;
import hudson.util.FormValidation;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JUnitValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitValidatorTest {

    JUnitValidator unitValidator;

    @Before
    public void setUp() throws Exception {
        unitValidator = new JUnitValidator();
    }

    // Validation of unstable threshold
    @Test
    public void testEmptyUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("");
        assertEquals("Warning if empty unstable threshold", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNullUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("");
        assertEquals("Warning if null unstable threshold", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("${UNSTABLE}");
        assertEquals("Warning if parameterized unstable threshold", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("50");
        assertEquals("Valid unstable threshold", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNegativeUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("-1");
        assertEquals("Error if negative unstable threshold", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testOutOfRangeUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("101");
        assertEquals("Error if unstable threshold exceeds 100", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testInvalidUnstableThreshold() {
        final FormValidation validation = unitValidator.validateUnstableThreshold("invalid");
        assertEquals("Error if invald unstable threshold", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of failed threshold
    @Test
    public void testEmptyFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("");
        assertEquals("Warning if empty failed threshold", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNullFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("");
        assertEquals("Warning if null failed threshold", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("${UNSTABLE}");
        assertEquals("Warning if parameterized failed threshold", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("50");
        assertEquals("Valid failed threshold", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNegativeFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("-1");
        assertEquals("Error if negative failed threshold", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testOutOfRangeFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("101");
        assertEquals("Error if failed threshold exceeds 100", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testInvalidFailedThreshold() {
        final FormValidation validation = unitValidator.validateFailedThreshold("invalid");
        assertEquals("Error if invald failed threshold", FormValidation.Kind.ERROR, validation.kind);
    }
}
