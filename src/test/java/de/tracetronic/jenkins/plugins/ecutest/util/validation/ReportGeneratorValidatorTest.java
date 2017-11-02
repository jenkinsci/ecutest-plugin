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
