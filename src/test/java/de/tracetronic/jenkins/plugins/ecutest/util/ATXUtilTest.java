/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;
import hudson.EnvVars;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

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

    @SuppressWarnings("rawtypes")
    @Test
    public void testProjectId() {
        final List<ATXSetting> uploadSettings = new ArrayList<ATXSetting>();
        final ATXTextSetting projectId = new ATXTextSetting("projectId", "", "", "2");
        uploadSettings.add(projectId);

        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("uploadConfig", uploadSettings);
        final ATXConfig atxConfig = new ATXConfig(configMap, null);

        assertThat(ATXUtil.getProjectId(atxConfig, new EnvVars()), is("2"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testProjectIdByExpandedConfig() {
        final List<ATXSetting> uploadSettings = new ArrayList<ATXSetting>();
        final ATXTextSetting projectId = new ATXTextSetting("projectId", "", "", "${PROJECT_ID}");
        uploadSettings.add(projectId);

        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("uploadConfig", uploadSettings);
        final ATXConfig atxConfig = new ATXConfig(configMap, null);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("PROJECT_ID", "2");
                }
            }));

        assertThat(ATXUtil.getProjectId(atxConfig, envVars), is("2"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testUnavailableProjectId() {
        final List<ATXSetting> uploadSettings = new ArrayList<ATXSetting>();

        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("uploadConfig", uploadSettings);
        final ATXConfig atxConfig = new ATXConfig(configMap, null);

        assertThat(ATXUtil.getProjectId(atxConfig, new EnvVars()), nullValue());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testSingleTestplanMap() {
        final List<ATXSetting> specialSettings = new ArrayList<ATXSetting>();
        final ATXBooleanSetting singleTestplanMap = new ATXBooleanSetting(
            "mapSeparateProjectExecutionAsSingleTestplan", "", "", false);
        specialSettings.add(singleTestplanMap);

        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();
        configMap.put("specialConfig", specialSettings);
        final ATXConfig atxConfig = new ATXConfig(configMap, null);

        assertThat(ATXUtil.isSingleTestplanMap(atxConfig), is(false));
    }
}
