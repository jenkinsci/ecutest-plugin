/**
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
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link ToolValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolValidatorTest extends TestCase {

    ToolValidator toolValidator;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        toolValidator = new ToolValidator();
        folder.create();
    }

    // Validation of workspace dir
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
