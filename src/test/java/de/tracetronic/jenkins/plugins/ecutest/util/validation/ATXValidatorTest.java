/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ATXValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXValidatorTest {

    ATXValidator atxValidator;

    @Before
    public void setUp() throws Exception {
        atxValidator = new ATXValidator();
    }

    // Validation of TEST-GUIDE name
    @Test
    public void testEmptyName() {
        final FormValidation validation = atxValidator.validateName("");
        assertEquals("Error if empty TEST-GUIDE name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullName() {
        final FormValidation validation = atxValidator.validateName(null);
        assertEquals("Error if no TEST-GUIDE name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testValidName() {
        final FormValidation validation = atxValidator.validateName("TEST-GUIDE");
        assertEquals("Valid TEST-GUIDE name", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of server URL
    @Test
    public void testEmptyServerUrl() {
        final FormValidation validation = atxValidator.validateServerUrl("");
        assertEquals("Error if empty server URL", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullServerUrl() {
        final FormValidation validation = atxValidator.validateServerUrl(null);
        assertEquals("Error if no server URL", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testIPServerUrl() throws IOException {
        final FormValidation validation = atxValidator.validateServerUrl("127.0.0.1");
        assertEquals("Valid IP formatted server URL", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testLocalhostServerUrl() throws IOException {
        final FormValidation validation = atxValidator.validateServerUrl("localhost");
        assertEquals("Valid localhost server URL", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidProtocolServerUrl() throws IOException {
        final FormValidation validation = atxValidator.validateServerUrl("http://127.0.0.1");
        assertEquals("Error if protocol added to server URL", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedServerUrl() {
        final FormValidation validation = atxValidator.validateServerUrl("${SERVER_URL}");
        assertEquals("Warning if parameterized server URL", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of server port
    @Test
    public void testEmptyServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("");
        assertEquals("Error if empty server port", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullServerPort() {
        final FormValidation validation = atxValidator.validateServerPort(null);
        assertEquals("Error if no server port", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testValidServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("8085");
        assertEquals("Valid server port", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNegativeServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("-1");
        assertEquals("Invalid server port if negative", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testZeroServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("0");
        assertEquals("Invalid server port if equals zero", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testAlphaNumericServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("abc123");
        assertEquals("Invalid server port if alphanumeric", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testOutOfRangeServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("70000");
        assertEquals("Invalid server port if higher than 65535", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testPrivilegedServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("500");
        assertEquals("Server port need root privileges on Unix", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedServerPort() {
        final FormValidation validation = atxValidator.validateServerPort("${SERVER_PORT}");
        assertEquals("Warning if parameterized server port", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of server context path
    @Test
    public void testEmptyServerContextPath() {
        final FormValidation validation = atxValidator.validateServerContextPath("");
        assertEquals("Valid if empty server context path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullServerContextPath() {
        final FormValidation validation = atxValidator.validateServerContextPath(null);
        assertEquals("Valid if null server context path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testValidServerContextPath() throws IOException {
        final FormValidation validation = atxValidator.validateServerContextPath("context-abc_123");
        assertEquals("Valid formatted server context path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidServerContextPath() throws IOException {
        final FormValidation validation = atxValidator.validateServerContextPath("*%context/#+");
        assertEquals("Error if invalid server context path", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedServerContextPath() {
        final FormValidation validation = atxValidator.validateServerContextPath("${CONTEXT_PATH}");
        assertEquals("Warning if parameterized server context path", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of archive miscellaneous files
    @Test
    public void testEmptyArchiveExpression() {
        final FormValidation validation = atxValidator.validateArchiveMiscFiles("");
        assertEquals("Valid if empty expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullArchiveExpression() {
        final FormValidation validation = atxValidator.validateArchiveMiscFiles(null);
        assertEquals("Valid if null expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testValidSingleArchiveExpression() {
        final FormValidation validation = atxValidator.validateArchiveMiscFiles("myFile*.asc");
        assertEquals("Valid single expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testValidMultiArchiveExpression() {
        final FormValidation validation = atxValidator.validateArchiveMiscFiles("myFile*.asc;asc/**/myDirFile.*;*;**");
        assertEquals("Valid multiple expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidArchiveExpression() {
        final FormValidation validation = atxValidator.validateArchiveMiscFiles("-");
        assertEquals("Invalid archive expression", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedArchiveExpression() {
        final FormValidation validation = atxValidator.validateArchiveMiscFiles("${ARCHIVES}");
        assertEquals("Warning if parameterized archive expression", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of covered attributes
    @Test
    public void testEmptyAttributesExpression() {
        final FormValidation validation = atxValidator.validateCoveredAttributes("");
        assertEquals("Valid if empty expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullAttributesExpression() {
        final FormValidation validation = atxValidator.validateCoveredAttributes(null);
        assertEquals("Valid if null expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testValidSingleAttributesExpression() {
        final FormValidation validation = atxValidator.validateCoveredAttributes("Testlevel");
        assertEquals("Valid single expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testValidMultiAttributesExpression() {
        final FormValidation validation = atxValidator
            .validateCoveredAttributes("Testlevel;Designer;Execution Priority;Estimated Duration [min];");
        assertEquals("Valid multiple expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testCustomAttributesExpression() {
        final FormValidation validation = atxValidator.validateCoveredAttributes("Requirements IDs; Custom Attribute");
        assertEquals("Custom archive expression", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testInvalidAttributesExpression() {
        final FormValidation validation = atxValidator.validateCoveredAttributes("-");
        assertEquals("Invalid archive expression", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedAttributesExpression() {
        final FormValidation validation = atxValidator.validateCoveredAttributes("${ATTRIBUTES}");
        assertEquals("Warning if parameterized archive expression", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of settings switch
    @Test
    public void testEmptySetting() {
        final FormValidation validation = atxValidator.validateSetting("", "");
        assertEquals("Empty setting name will be ignored", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullSetting() {
        final FormValidation validation = atxValidator.validateSetting(null, null);
        assertEquals("Null setting name will be ignored", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testServerURLSetting() {
        final FormValidation validation = atxValidator.validateSetting("serverURL", "localhost");
        assertEquals("Valid server URL setting", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testServerPortSetting() {
        final FormValidation validation = atxValidator.validateSetting("serverPort", "8085");
        assertEquals("Valid server port setting", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testServerContextPath() {
        final FormValidation validation = atxValidator.validateSetting("serverContextPath", "context");
        assertEquals("Valid server port context path", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testArchiveExpressionSetting() {
        final FormValidation validation = atxValidator.validateSetting("archiveMiscFiles", "myFile*.asc;");
        assertEquals("Valid archive misc files setting", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testAtrributeExpressionSetting() {
        final FormValidation validation = atxValidator.validateSetting("coveredAttributes", "Testlevel;");
        assertEquals("Valid covered attributes setting", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidSetting() {
        final FormValidation validation = atxValidator.validateSetting("invalid", "");
        assertEquals("Invalid setting name will be ignored", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testDefaultParameterizedSetting() {
        final FormValidation validation = atxValidator.validateSetting("default", "${VALUE}");
        assertEquals("Warning if parameterized custom setting value", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of custom settings
    @Test
    public void testEmptyCustomSettingName() {
        final FormValidation validation = atxValidator.validateCustomSettingName("");
        assertEquals("Error if custom setting name is empty", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullCustomSettingName() {
        final FormValidation validation = atxValidator.validateCustomSettingName(null);
        assertEquals("Error if custom setting name is null", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testValidCustomSettingName() {
        final FormValidation validation = atxValidator.validateCustomSettingName("missingSetting");
        assertEquals("Valid name for custom setting", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidCustomSettingName() {
        final FormValidation validation = atxValidator.validateCustomSettingName("!invalid123");
        assertEquals("Error if name of custom setting is not alpha", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedCustomSettingName() {
        final FormValidation validation = atxValidator.validateCustomSettingName("${CUSTOM_NAME}");
        assertEquals("Error if parameterized custom setting name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testEmptyCustomSettingValue() {
        final FormValidation validation = atxValidator.validateCustomSettingValue("");
        assertEquals("Valid if custom setting name is empty", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullCustomSettingValue() {
        final FormValidation validation = atxValidator.validateCustomSettingValue(null);
        assertEquals("Valid if custom setting name is null", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testValidCustomSettingValue() {
        final FormValidation validation = atxValidator.validateCustomSettingValue("valid");
        assertEquals("Valid value for custom setting", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedCustomSettingValue() {
        final FormValidation validation = atxValidator.validateCustomSettingValue("${CUSTOM_VALUE}");
        assertEquals("Warning if parameterized custom setting value", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of test connection
    @Test
    public void testEmptyConnection() {
        final FormValidation validation = atxValidator.testConnection("", false);
        assertEquals("Error if connection URL is empty", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullConnection() {
        final FormValidation validation = atxValidator.testConnection(null, false);
        assertEquals("Error if connection URL is null", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testMalformedConnection() {
        final FormValidation validation = atxValidator.testConnection("null", false);
        assertEquals("Error if connection URL is invalid", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNotReachableUnsecuredConnection() {
        final FormValidation validation = atxValidator.testConnection("http://localhost:0", false);
        assertEquals("Warning if connection URL is not reachable", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNotReachableSecuredConnection() {
        final FormValidation validation = atxValidator.testConnection("https://localhost:0", false);
        assertEquals("Warning if secured connection URL is not reachable", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedConnection() {
        final FormValidation validation = atxValidator.testConnection("${URL}", false);
        assertEquals("Warning if parameterized connection URL", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidConnectionBySettings() {
        final FormValidation validation = atxValidator.testConnection("localhost", "0", "", false, false);
        assertEquals("Warning if connection URL is not reachable", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testInvalidConnectionBySettings() {
        final FormValidation validation = atxValidator.testConnection(null, null, null, false, false);
        assertEquals("Error if connection URL is invalid", FormValidation.Kind.ERROR, validation.kind);
    }
}
