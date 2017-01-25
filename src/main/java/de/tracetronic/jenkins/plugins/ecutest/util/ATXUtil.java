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

import hudson.EnvVars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;

/**
 * Utility class providing ATX related functions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public final class ATXUtil {

    /**
     * Instantiates a new {@link ATXUtil}.
     */
    private ATXUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Removes special characters from test name and replaces with underscore "_".
     *
     * @param testName
     *            the test name
     * @return the ATX compliant test name
     */
    public static String getValidATXName(final String testName) {
        String validATXName = "DefaultTestName";
        if (testName != null && StringUtils.countMatches(testName, "_") != testName.length()) {
            validATXName = testName;

            final Map<String, String> specialCharMap = new HashMap<String, String>();
            specialCharMap.put("ä", "ae");
            specialCharMap.put("Ä", "Ae");
            specialCharMap.put("ö", "oe");
            specialCharMap.put("Ö", "Oe");
            specialCharMap.put("ü", "ue");
            specialCharMap.put("Ü", "Ue");
            specialCharMap.put("ß", "ss");
            specialCharMap.put("-", "_");
            specialCharMap.put("\\.", "_");
            specialCharMap.put(" ", "");

            // Replace special chars
            for (final Entry<String, String> specialChar : specialCharMap.entrySet()) {
                validATXName = validATXName.replaceAll(specialChar.getKey(), specialChar.getValue());
            }

            // Remove coherent underscores
            validATXName = removeCoherentUnderscores(validATXName);

            // Add 'i' char if test name starts with digit
            if (Character.isDigit(validATXName.charAt(0))) {
                validATXName = String.format("i%s", validATXName);
            }
        }
        return validATXName;
    }

    /**
     * Removes the coherent underscores.
     *
     * @param testName
     *            the test name
     * @return the string without coherent underscores
     */
    private static String removeCoherentUnderscores(final String testName) {
        final String validATXName = testName.replace("__", "_");
        if (validATXName.equals(testName)) {
            return validATXName;
        } else {
            return removeCoherentUnderscores(validATXName);
        }
    }

    /**
     * Gets the server base URL of the ATX installation.
     * Parameterized settings are expanded by given environment variables.
     *
     * @param config
     *            the ATX configuration
     * @param envVars
     *            the environment variables
     * @return the ATX base URL or {@code null} if invalid URL
     */
    @CheckForNull
    @SuppressWarnings("rawtypes")
    public static String getBaseUrl(final ATXConfig config, final EnvVars envVars) {
        String fullServerUrl = null;
        if (config != null && envVars != null) {
            final List<ATXSetting> uploadSettings = config.getConfigByName("uploadConfig");
            final Object useHttpsConnection = config.getSettingValueByName("useHttpsConnection", uploadSettings);
            final String serverUrl = envVars.expand((String) config.getSettingValueByName("serverURL", uploadSettings));
            final String serverPort = envVars.expand((String) config
                    .getSettingValueByName("serverPort", uploadSettings));
            final String contextPath = envVars.expand((String) config.getSettingValueByName("serverContextPath",
                    uploadSettings));
            if (serverUrl != null && serverPort != null && contextPath != null && useHttpsConnection != null) {
                fullServerUrl = getBaseUrl(serverUrl, serverPort, contextPath, (boolean) useHttpsConnection);
            }
        }
        return fullServerUrl;
    }

    /**
     * Gets the base URL of the ATX installation by given server settings.
     *
     * @param serverUrl
     *            the server URL
     * @param serverPort
     *            the server port
     * @param contextPath
     *            the context path
     * @param useHttpsConnection
     *            specifies whether to use secured connection
     * @return the ATX base URL or {@code null} if invalid URL
     */
    public static String getBaseUrl(final String serverUrl, final String serverPort, final String contextPath,
            final boolean useHttpsConnection) {
        String fullServerUrl = null;
        if (serverUrl != null && serverPort != null && contextPath != null) {
            final String protocol = useHttpsConnection ? "https" : "http";
            fullServerUrl = contextPath.isEmpty() ? String.format("%s://%s:%s", protocol, serverUrl, serverPort)
                    : String.format("%s://%s:%s/%s", protocol, serverUrl, serverPort, contextPath);
        }
        return fullServerUrl;
    }

    /**
     * Returns the current ATX setting {@code mapSeparateProjectExecutionAsSingleTestplan}.
     *
     * @param config
     *            the ATX configuration
     * @return the value of this setting as boolean
     */
    @SuppressWarnings("rawtypes")
    public static boolean isSingleTestplanMap(final ATXConfig config) {
        boolean isMapEnabled = true; // true by default when setting not exists
        if (config != null) {
            final List<ATXSetting> specialSettings = config.getConfigByName("specialConfig");
            final Object settingValue = config.getSettingValueByName("mapSeparateProjectExecutionAsSingleTestplan",
                    specialSettings);
            if (settingValue != null) {
                isMapEnabled = (boolean) settingValue;
            }
        }
        return isMapEnabled;
    }
}
