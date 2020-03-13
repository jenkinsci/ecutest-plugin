/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ToolValidator}.
 */
public class ToolValidatorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    ToolValidator toolValidator;

    @Before
    public void setUp() throws Exception {
        toolValidator = new ToolValidator();
        folder.create();
    }

    // Validation of workspace directory
    @Test
    public void testEmptyWorkspaceDir() {
        final FormValidation validation = toolValidator.validateWorkspaceDir("");
        assertEquals("Valid if empty workspace dir", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullWorkspaceDir() {
        final FormValidation validation = toolValidator.validateWorkspaceDir(null);
        assertEquals("Valid if null workspace dir", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedWorkspaceDir() {
        final FormValidation validation = toolValidator.validateWorkspaceDir("${WS}");
        assertEquals("Warning if parameterized workspace dir", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidWorkspaceDir() {
        final FormValidation validation = toolValidator.validateWorkspaceDir("workspace");
        assertEquals("Valid workspace dir", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of settings directory
    @Test
    public void testEmptySettingsDir() {
        final FormValidation validation = toolValidator.validateSettingsDir("");
        assertEquals("Valid if empty settings dir", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullSettingsDir() {
        final FormValidation validation = toolValidator.validateSettingsDir(null);
        assertEquals("Valid if null settings dir", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedSettingsDir() {
        final FormValidation validation = toolValidator.validateSettingsDir("${WS}");
        assertEquals("Warning if parameterized settings dir", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidSettingsDir() {
        final FormValidation validation = toolValidator.validateSettingsDir("workspace");
        assertEquals("Valid settings dir", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of ToolLibs.ini
    @Test
    public void testEmptyToolLibsIni() {
        final FormValidation validation = toolValidator.validateToolLibsIni("");
        assertEquals("Valid if empty ToolLibs.ini", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullToolLibsIni() {
        final FormValidation validation = toolValidator.validateToolLibsIni(null);
        assertEquals("Error if no ToolLibs.ini", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedToolLibsIni() {
        final FormValidation validation = toolValidator.validateToolLibsIni("${TOOL_LIBS_INI}");
        assertEquals("Warning if parameterized ToolLibs.ini", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidToolLibsIni() throws IOException {
        final String toolLibsIni = folder.newFile("ToolLibs.ini").getAbsolutePath();
        final FormValidation validation = toolValidator.validateToolLibsIni(toolLibsIni);
        assertEquals("Valid ToolLibs.ini", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidDirAsToolLibsIni() throws IOException {
        final String toolLibsIni = folder.newFolder().getAbsolutePath();
        final FormValidation validation = toolValidator.validateToolLibsIni(toolLibsIni);
        assertEquals("Invalid directory as ToolLibs.ini ", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of TCP port
    @Test
    public void testEmptyTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort("");
        assertEquals("Warning if empty tcp port", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNullTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort(null);
        assertEquals("Warning if no tcp port", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort("${TCP_PORT}");
        assertEquals("Warning if parameterized tcp port", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort("5017");
        assertEquals("Valid tcp port", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNegativeTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort("-1");
        assertEquals("Invalid tcp port if negative", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testZeroTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort("0");
        assertEquals("Invalid tcp port if equals zero", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testAlphaNumericTcpPort() {
        final FormValidation validation = toolValidator.validateTcpPort("abc123");
        assertEquals("Invalid tcp port if alphanumeric", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of timeout
    @Test
    public void testEmptyTimeout() {
        final FormValidation validation = toolValidator.validateTimeout("", 120);
        assertEquals("Warning if empty timeout", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNullTimeout() {
        final FormValidation validation = toolValidator.validateTimeout(null, 120);
        assertEquals("Warning if no timeout", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedtimeout() {
        final FormValidation validation = toolValidator.validateTimeout("${TIMEOUT}", 120);
        assertEquals("Warning if parameterized timeout", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidTimeout() {
        final FormValidation validation = toolValidator.validateTimeout("120", 120);
        assertEquals("Valid timeout", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNegativeTimeout() {
        final FormValidation validation = toolValidator.validateTimeout("-1", 120);
        assertEquals("Invalid timeout if negative", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testZeroTimeout() {
        final FormValidation validation = toolValidator.validateTimeout("0", 120);
        assertEquals("Warning timeout if equals zero (i.e. disabled)", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testAlphaNumericTimeout() {
        final FormValidation validation = toolValidator.validateTimeout("abc123", 120);
        assertEquals("Invalid timeout if alphanumeric", FormValidation.Kind.ERROR, validation.kind);
    }
}
