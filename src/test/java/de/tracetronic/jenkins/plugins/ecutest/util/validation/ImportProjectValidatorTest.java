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

import hudson.util.FormValidation;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ImportProjectValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectValidatorTest extends TestCase {

    ImportProjectValidator importValidator;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        importValidator = new ImportProjectValidator();
    }

    // Validation of project path
    @Test
    public void testEmptyProjectPath() {
        final FormValidation validation = importValidator.validateProjectPath("");
        assertEquals("Error if empty project path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullProjectPath() {
        final FormValidation validation = importValidator.validateProjectPath(null);
        assertEquals("Error if project path not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedProjectPath() {
        final FormValidation validation = importValidator.validateProjectPath("${PROJECT_PATH}");
        assertEquals("Warning if parameterized project path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidProjectPath() {
        final FormValidation validation = importValidator.validateProjectPath("project");
        assertEquals("Valid project path", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of archive path
    @Test
    public void testEmptyArchivePath() {
        final FormValidation validation = importValidator.validateArchivePath("");
        assertEquals("Error if empty archive path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullArchivePath() {
        final FormValidation validation = importValidator.validateArchivePath(null);
        assertEquals("Error if archive path not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedArchivePath() {
        final FormValidation validation = importValidator.validateArchivePath("${ARCHIVE_PATH}");
        assertEquals("Warning if parameterized archive path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidArchivePath() {
        final FormValidation validation = importValidator.validateArchivePath("test.prz");
        assertEquals("Valid archive path has .prz extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidArchivePath() {
        final FormValidation validation = importValidator.validateArchivePath("test");
        assertEquals("Error if invalid archive path", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of import path
    @Test
    public void testEmptyImportPath() {
        final FormValidation validation = importValidator.validateImportPath("");
        assertEquals("Valid if empty import path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullImportPath() {
        final FormValidation validation = importValidator.validateImportPath(null);
        assertEquals("Valid if null import path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedImportPath() {
        final FormValidation validation = importValidator.validateImportPath("${IMPORT_PATH}");
        assertEquals("Warning if parameterized import path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidImportPath() {
        final FormValidation validation = importValidator.validateImportPath("import");
        assertEquals("Valid import path", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of import configuration path
    @Test
    public void testEmptyImportConfigPath() {
        final FormValidation validation = importValidator.validateImportPath("");
        assertEquals("Valid if empty import config path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullImportConfigPath() {
        final FormValidation validation = importValidator.validateImportPath(null);
        assertEquals("Valid if null import config path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedImportConfigPath() {
        final FormValidation validation = importValidator.validateImportPath("${IMPORT_CONFIG_PATH}");
        assertEquals("Warning if parameterized import config path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidImportConfigPath() {
        final FormValidation validation = importValidator.validateImportPath("import");
        assertEquals("Valid import config path", FormValidation.Kind.OK, validation.kind);
    }
}
