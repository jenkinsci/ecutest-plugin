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
 * Unit tests for {@link ReportGeneratorValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorValidatorTest {

    ReportGeneratorValidator reportValidator;

    @Before
    public void setUp() throws Exception {
        reportValidator = new ReportGeneratorValidator();
    }

    // Validation of generator name
    @Test
    public void testEmptyGeneratorName() {
        final FormValidation validation = reportValidator.validateGeneratorName("");
        assertEquals("Error if empty generator name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullGeneratorName() {
        final FormValidation validation = reportValidator.validateGeneratorName(null);
        assertEquals("Error if generator name not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedGeneratorName() {
        final FormValidation validation = reportValidator.validateGeneratorName("${GENERATOR}");
        assertEquals("Warning if parameterized generator name", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testGeneratorName() {
        final FormValidation validation = reportValidator.validateGeneratorName("HTML");
        assertEquals("Valid generator name", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of generator setting name
    @Test
    public void testEmptyGeneratorSettingName() {
        final FormValidation validation = reportValidator.validateSettingName("");
        assertEquals("Error if empty generator setting name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullGeneratorSettingName() {
        final FormValidation validation = reportValidator.validateSettingName(null);
        assertEquals("Error if generator setting name not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedGeneratorSettingName() {
        final FormValidation validation = reportValidator.validateSettingName("${NAME}");
        assertEquals("Warning if parameterized generator setting name", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testGeneratorSettingName() {
        final FormValidation validation = reportValidator.validateSettingName("name");
        assertEquals("Valid generator setting name", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of generator setting value
    @Test
    public void testEmptyGeneratorSettingValue() {
        final FormValidation validation = reportValidator.validateSettingValue("");
        assertEquals("Valid if empty generator setting value", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullGeneratorSettingValue() {
        final FormValidation validation = reportValidator.validateSettingValue(null);
        assertEquals("Valid if generator setting value not defined", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedGeneratorSettingValue() {
        final FormValidation validation = reportValidator.validateSettingValue("${NAME}");
        assertEquals("Warning if parameterized generator setting value", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testGeneratorSettingValue() {
        final FormValidation validation = reportValidator.validateSettingValue("value");
        assertEquals("Valid generator setting value", FormValidation.Kind.OK, validation.kind);
    }
}
