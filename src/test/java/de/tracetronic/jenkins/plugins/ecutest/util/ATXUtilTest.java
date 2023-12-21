/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSecretSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting.SettingsGroup;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;
import hudson.EnvVars;
import hudson.util.Secret;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link ATXUtil}.
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

    @Test
    public void testBaseUrlBySpecificConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXTextSetting serverUrl = new ATXTextSetting("serverURL", SettingsGroup.CONNECTION, "", "", "localhost");
        final ATXTextSetting serverPort = new ATXTextSetting("serverPort", SettingsGroup.CONNECTION, "", "", "8086");
        final ATXTextSetting serverContextPath = new ATXTextSetting("serverContextPath", SettingsGroup.CONNECTION, "", "",
            "context");
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", SettingsGroup.CONNECTION, "",
            "", true);
        uploadSettings.add(serverUrl);
        uploadSettings.add(serverPort);
        uploadSettings.add(serverContextPath);
        uploadSettings.add(useHttpsConnection);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        assertThat(ATXUtil.getBaseUrl(atxConfig, new EnvVars()), is("https://localhost:8086/context"));
    }

    @Test
    public void testBaseUrlByExpandedConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXTextSetting serverUrl = new ATXTextSetting("serverURL", SettingsGroup.CONNECTION, "", "", "${SERVER_URL}");
        final ATXTextSetting serverPort = new ATXTextSetting("serverPort", SettingsGroup.CONNECTION, "", "", "${SERVER_PORT}");
        final ATXTextSetting serverContextPath = new ATXTextSetting("serverContextPath", SettingsGroup.CONNECTION, "", "",
            "${SERVER_CONTEXT}");
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", SettingsGroup.CONNECTION, "",
            "", true);
        uploadSettings.add(serverUrl);
        uploadSettings.add(serverPort);
        uploadSettings.add(serverContextPath);
        uploadSettings.add(useHttpsConnection);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

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

    @Test
    public void testInvalidProxyUrl() {
        assertNull(ATXUtil.getProxyUrl(null, null));
    }

    @Test
    public void testProxyUrlByDefaultConfig() {
        final ATXConfig atxConfig = new ATXConfig();
        assertThat(ATXUtil.getProxyUrl(atxConfig, new EnvVars()), blankOrNullString());
    }

    @Test
    public void testHttpProxyUrlBySpecificConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXSecretSetting httpProxy = new ATXSecretSetting("httpProxy", SettingsGroup.CONNECTION, "", "",
            Secret.fromString("http://user:pass@127.0.0.1:8080"));
        final ATXSecretSetting httpsProxy = new ATXSecretSetting("httpsProxy", SettingsGroup.CONNECTION, "", "",
            Secret.fromString("http://user:pass@127.0.0.1:8081"));
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection",
            SettingsGroup.CONNECTION, "", "", false);
        uploadSettings.add(httpProxy);
        uploadSettings.add(httpsProxy);
        uploadSettings.add(useHttpsConnection);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        assertThat(ATXUtil.getProxyUrl(atxConfig, new EnvVars()), is("http://user:pass@127.0.0.1:8080"));
    }

    @Test
    public void testHttpsProxyUrlBySpecificConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXSecretSetting httpProxy = new ATXSecretSetting("httpProxy", SettingsGroup.CONNECTION, "", "",
            Secret.fromString("http://user:pass@127.0.0.1:8080"));
        final ATXSecretSetting httpsProxy = new ATXSecretSetting("httpsProxy", SettingsGroup.CONNECTION, "", "",
            Secret.fromString("http://user:pass@127.0.0.1:8081"));
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", SettingsGroup.CONNECTION, "",
            "", true);
        uploadSettings.add(httpProxy);
        uploadSettings.add(httpsProxy);
        uploadSettings.add(useHttpsConnection);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        assertThat(ATXUtil.getProxyUrl(atxConfig, new EnvVars()), is("http://user:pass@127.0.0.1:8081"));
    }

    @Test
    public void testHttpProxyUrlByExpandedConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXSecretSetting httpProxy = new ATXSecretSetting("httpProxy", SettingsGroup.CONNECTION, "", "",
            Secret.fromString("${PROXY_URL}"));
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", SettingsGroup.CONNECTION, "",
            "", false);
        uploadSettings.add(httpProxy);
        uploadSettings.add(useHttpsConnection);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("PROXY_URL", "http://user:pass@127.0.0.1:8080");
                }
            }));

        assertThat(ATXUtil.getProxyUrl(atxConfig, envVars), is("http://user:pass@127.0.0.1:8080"));
    }

    @Test
    public void testHttpsProxyUrlByExpandedConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXSecretSetting httpsProxy = new ATXSecretSetting("httpsProxy", SettingsGroup.CONNECTION, "", "",
            Secret.fromString("${PROXY_URL}"));
        final ATXBooleanSetting useHttpsConnection = new ATXBooleanSetting("useHttpsConnection", SettingsGroup.CONNECTION, "",
            "", true);
        uploadSettings.add(httpsProxy);
        uploadSettings.add(useHttpsConnection);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("PROXY_URL", "http://user:pass@127.0.0.1:8081");
                }
            }));

        assertThat(ATXUtil.getProxyUrl(atxConfig, envVars), is("http://user:pass@127.0.0.1:8081"));
    }

    @Test
    public void testProjectId() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXTextSetting projectId = new ATXTextSetting("projectId", SettingsGroup.CONNECTION, "", "", "2");
        uploadSettings.add(projectId);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        assertThat(ATXUtil.getProjectId(atxConfig, new EnvVars()), is("2"));
    }

    @Test
    public void testProjectIdByExpandedConfig() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();
        final ATXTextSetting projectId = new ATXTextSetting("projectId", SettingsGroup.CONNECTION, "", "", "${PROJECT_ID}");
        uploadSettings.add(projectId);

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        final EnvVars envVars = new EnvVars(
            Collections.unmodifiableMap(new HashMap<String, String>() {

                private static final long serialVersionUID = 1L;

                {
                    put("PROJECT_ID", "2");
                }
            }));

        assertThat(ATXUtil.getProjectId(atxConfig, envVars), is("2"));
    }

    @Test
    public void testUnavailableProjectId() {
        final List<ATXSetting<?>> uploadSettings = new ArrayList<>();

        final ATXConfig atxConfig = new ATXConfig(uploadSettings, null);

        assertThat(ATXUtil.getProjectId(atxConfig, new EnvVars()), nullValue());
    }

    @Test
    public void testSingleTestplanMap() {
        final List<ATXSetting<?>> specialSettings = new ArrayList<>();
        final ATXBooleanSetting singleTestplanMap = new ATXBooleanSetting(
            "mapSeparateProjectExecutionAsSingleTestplan", SettingsGroup.SPECIAL, "", "", false);
        specialSettings.add(singleTestplanMap);

        final ATXConfig atxConfig = new ATXConfig(specialSettings, null);

        assertThat(ATXUtil.isSingleTestplanMap(atxConfig), is(false));
    }
}
