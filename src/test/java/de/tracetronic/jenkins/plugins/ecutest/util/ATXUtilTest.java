/*
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import hudson.EnvVars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;

/**
 * Unit tests for {@link ATXUtil}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXUtilTest {

    private static final String ACTUAL = "DefaultTestName";

    @Test
    public void testValidATXNameForNull() {
        assertThat(ATXUtil.getValidATXName(null), is(ACTUAL));
    }

    @Test
    public void testValidATXNameForEmpty() {
        assertThat(ATXUtil.getValidATXName(""), is(ACTUAL));
    }

    @Test
    public void testValidATXNameForUnderScore() {
        assertThat(ATXUtil.getValidATXName("_"), is(ACTUAL));
    }

    @Test
    public void testValidATXNameForDoubleUnderScore() {
        assertThat(ATXUtil.getValidATXName("__"), is(ACTUAL));
    }

    @Test
    public void testValidATXNameForTripleUnderScore() {
        assertThat(ATXUtil.getValidATXName("___"), is(ACTUAL));
    }

    @Test
    public void testValidATXNameForSpecialChars() {
        assertThat(ATXUtil.getValidATXName("äÄöÖüÜß"), is("aeAeoeOeueUess"));
    }

    @Test
    public void testValidATXNameForMinus() {
        assertThat(ATXUtil.getValidATXName("Test-Name"), is("Test_Name"));
    }

    @Test
    public void testValidATXNameForDoubleMinus() {
        assertThat(ATXUtil.getValidATXName("Test--Name"), is("Test_Name"));
    }

    @Test
    public void testValidATXNameForNumber() {
        assertThat(ATXUtil.getValidATXName("123Test-Name"), is("i123Test_Name"));
    }

    @Test
    public void testValidATXNameForDot() {
        assertThat(ATXUtil.getValidATXName("Test.Name"), is("Test_Name"));
    }

    @Test
    public void testValidATXNameForWhiteSpace() {
        assertThat(ATXUtil.getValidATXName("Test Name"), is("TestName"));
    }

    @Test
    public void testInvalidBaseUrl() {
        assertNull(ATXUtil.getBaseUrl(null, null, null, false));
    }

    @Test
    public void testUnsecuredBaseUrl() {
        assertThat(ATXUtil.getBaseUrl("localhost", "8085", "context", false), is("http://localhost:8085/context"));
    }

    @Test
    public void testSecuredBaseUrl() {
        assertThat(ATXUtil.getBaseUrl("localhost", "8085", "context", true), is("https://localhost:8085/context"));
    }

    @Test
    public void testInvalidBaseUrlByConfig() {
        assertNull(ATXUtil.getBaseUrl(null, null));
    }

    @Test
    public void testBaseUrlByDefaultConfig() {
        final ATXConfig atxConfig = new ATXConfig();
        assertThat(ATXUtil.getBaseUrl(atxConfig, new EnvVars()), is("http://127.0.0.1:8085"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBaseUrlBySpecificConfig() {
        final List<ATXSetting> uploadSettings = new ArrayList<ATXSetting>();
        final ATXTextSetting serverUrl = new ATXTextSetting("serverURL", "", "", "localhost");
        final ATXTextSetting serverPort = new ATXTextSetting("serverPort", "", "", "8086");
        final ATXTextSetting serverContextPath = new ATXTextSetting("serverContextPath", "", "", "context");
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", "", "", true);
        uploadSettings.add(serverUrl);
        uploadSettings.add(serverPort);
        uploadSettings.add(serverContextPath);
        uploadSettings.add(useHttpsConnection);

        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("uploadConfig", uploadSettings);
        final ATXConfig atxConfig = new ATXConfig(configMap, null);

        assertThat(ATXUtil.getBaseUrl(atxConfig, new EnvVars()), is("https://localhost:8086/context"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testBaseUrlByExpandedConfig() {
        final List<ATXSetting> uploadSettings = new ArrayList<ATXSetting>();
        final ATXTextSetting serverUrl = new ATXTextSetting("serverURL", "", "", "${SERVER_URL}");
        final ATXTextSetting serverPort = new ATXTextSetting("serverPort", "", "", "${SERVER_PORT}");
        final ATXTextSetting serverContextPath = new ATXTextSetting("serverContextPath", "", "", "${SERVER_CONTEXT}");
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", "", "", true);
        uploadSettings.add(serverUrl);
        uploadSettings.add(serverPort);
        uploadSettings.add(serverContextPath);
        uploadSettings.add(useHttpsConnection);

        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("uploadConfig", uploadSettings);
        final ATXConfig atxConfig = new ATXConfig(configMap, null);

        final EnvVars envVars = new EnvVars(
                Collections.unmodifiableMap(new HashMap<String, String>() {

                    private static final long serialVersionUID = 1L;
                    {
                        put("SERVER_URL", "localhost");
                        put("SERVER_PORT", "8086");
                        put("SERVER_CONTEXT", "context");
                    }
                }));

        assertThat(ATXUtil.getBaseUrl(atxConfig, envVars), is("https://localhost:8086/context"));
    }
}
