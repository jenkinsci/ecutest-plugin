/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
