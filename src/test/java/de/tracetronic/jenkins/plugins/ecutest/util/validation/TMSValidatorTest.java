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
 * Unit tests for {@link TMSValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TMSValidatorTest {

    TMSValidator tmsValidator;

    @Before
    public void setUp() throws Exception {
        tmsValidator = new TMSValidator();
    }

    // Validation of TMS test path
    @Test
    public void testEmptyTestPath() {
        final FormValidation validation = tmsValidator.validateTestPath("");
        assertEquals("Error if empty test path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullTestPath() {
        final FormValidation validation = tmsValidator.validateTestPath(null);
        assertEquals("Error if test path not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedTestPath() {
        final FormValidation validation = tmsValidator.validateTestPath("${PROJECT_PATH}");
        assertEquals("Warning if parameterized test path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidTestPath() {
        final FormValidation validation = tmsValidator.validateTestPath("project");
        assertEquals("Valid test path", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of archive path
    @Test
    public void testEmptyArchivePath() {
        final FormValidation validation = tmsValidator.validateArchivePath("");
        assertEquals("Error if empty archive path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullArchivePath() {
        final FormValidation validation = tmsValidator.validateArchivePath(null);
        assertEquals("Error if archive path not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedArchivePath() {
        final FormValidation validation = tmsValidator.validateArchivePath("${ARCHIVE_PATH}");
        assertEquals("Warning if parameterized archive path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidArchivePath() {
        final FormValidation validation = tmsValidator.validateArchivePath("test.prz");
        assertEquals("Valid archive path has .prz extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidArchivePath() {
        final FormValidation validation = tmsValidator.validateArchivePath("test");
        assertEquals("Error if invalid archive path", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of import path
    @Test
    public void testEmptyImportPath() {
        final FormValidation validation = tmsValidator.validateImportPath("");
        assertEquals("Valid if empty import path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullImportPath() {
        final FormValidation validation = tmsValidator.validateImportPath(null);
        assertEquals("Valid if null import path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedImportPath() {
        final FormValidation validation = tmsValidator.validateImportPath("${IMPORT_PATH}");
        assertEquals("Warning if parameterized import path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidImportPath() {
        final FormValidation validation = tmsValidator.validateImportPath("import");
        assertEquals("Valid import path", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of import configuration path
    @Test
    public void testEmptyImportConfigPath() {
        final FormValidation validation = tmsValidator.validateImportConfigPath("");
        assertEquals("Valid if empty import config path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullImportConfigPath() {
        final FormValidation validation = tmsValidator.validateImportConfigPath(null);
        assertEquals("Valid if null import config path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedImportConfigPath() {
        final FormValidation validation = tmsValidator.validateImportConfigPath("${IMPORT_CONFIG_PATH}");
        assertEquals("Warning if parameterized import config path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidImportConfigPath() {
        final FormValidation validation = tmsValidator.validateImportConfigPath("import");
        assertEquals("Valid import config path", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of import path
    @Test
    public void testEmptyExportPath() {
        final FormValidation validation = tmsValidator.validateExportPath("");
        assertEquals("Error if empty export path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullExportPath() {
        final FormValidation validation = tmsValidator.validateExportPath(null);
        assertEquals("Error if null export path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedExportPath() {
        final FormValidation validation = tmsValidator.validateExportPath("${EXPORT_PATH}");
        assertEquals("Warning if parameterized export path", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidExportPath() {
        final FormValidation validation = tmsValidator.validateExportPath("export");
        assertEquals("Valid export path", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of package file
    @Test
    public void testEmptyPackageFile() {
        final FormValidation validation = tmsValidator.validatePackageFile("");
        assertEquals("Error if empty package file", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullPackageFile() {
        final FormValidation validation = tmsValidator.validatePackageFile(null);
        assertEquals("Error if package file not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedPackageFile() {
        final FormValidation validation = tmsValidator.validatePackageFile("${TEST}");
        assertEquals("Warning if parameterized package file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidPackageFile() {
        final FormValidation validation = tmsValidator.validatePackageFile("test.pkg");
        assertEquals("Valid package file has .pkg extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidPackageFile() {
        final FormValidation validation = tmsValidator.validatePackageFile("test");
        assertEquals("Error if invalid package file", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of project file
    @Test
    public void testEmptyProjectFile() {
        final FormValidation validation = tmsValidator.validateProjectFile("");
        assertEquals("Error if empty project file", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullProjectFile() {
        final FormValidation validation = tmsValidator.validateProjectFile(null);
        assertEquals("Error if project file not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedProjectFile() {
        final FormValidation validation = tmsValidator.validateProjectFile("${TEST}");
        assertEquals("Warning if parameterized project file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidProjectFile() {
        final FormValidation validation = tmsValidator.validateProjectFile("test.prj");
        assertEquals("Valid project file has .prj extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidProjectFile() {
        final FormValidation validation = tmsValidator.validateProjectFile("test");
        assertEquals("Error if invalid project file", FormValidation.Kind.ERROR, validation.kind);
    }
}
