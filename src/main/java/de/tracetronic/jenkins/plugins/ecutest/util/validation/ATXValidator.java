/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import hudson.Util;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import org.apache.commons.lang.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

/**
 * Validator to check ATX related form fields.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXValidator extends AbstractValidator {

    /**
     * Instantiates a new {@link ATXValidator}.
     * ATX settings needs permission check.
     */
    public ATXValidator() {
        super();
    }

    /**
     * Checks if given URL is valid.
     *
     * @param url the URL
     * @return {@code true} if URL is valid, {@code false} otherwise
     */
    private static boolean isValidURL(final String url) {
        try {
            final URL u = new URL(url);
            u.toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }

    /**
     * Ignores SSL certification errors by trusting all certificates and host names.
     *
     * @param connection the current connection
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyManagementException   the key management exception
     */
    public static void ignoreSSLIssues(final HttpsURLConnection connection)
        throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
            }
        },};

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        // Create all-trusting host name verifier
        final HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Install the all-trusting host verifier
        connection.setHostnameVerifier(allHostsValid);
    }

    /**
     * Validates the TEST-GUIDE name.
     *
     * @param value the value
     * @return the form validation
     */
    public FormValidation validateName(final String value) {
        return FormValidation.validateRequired(value);
    }

    /**
     * Validates the server URL.
     *
     * @param serverUrl the server URL
     * @return the form validation
     */
    public FormValidation validateServerUrl(final String serverUrl) {
        FormValidation returnValue = FormValidation.validateRequired(serverUrl);
        if (returnValue == FormValidation.ok()) {
            if (serverUrl.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedValue());
            } else if (serverUrl.startsWith("http://") || serverUrl.startsWith("https://")) {
                returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerUrlProtocol());
            } else if (isValidURL(serverUrl)) {
                returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerUrl(serverUrl));
            }
        }
        return returnValue;
    }

    /**
     * Validates the server port which must be non negative and in the range of 1-65535. A port less than 1024
     * requires root permission on an Unix-based system.
     *
     * @param serverPort the server port
     * @return the form validation
     */
    public FormValidation validateServerPort(final String serverPort) {
        FormValidation returnValue = FormValidation.validateRequired(serverPort);
        if (returnValue == FormValidation.ok()) {
            if (serverPort.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedValue());
            } else {
                returnValue = FormValidation.validatePositiveInteger(serverPort);
                if (returnValue == FormValidation.ok()) {
                    final int port = Integer.parseInt(serverPort);
                    if (port > 65535) {
                        returnValue = FormValidation.error(Messages.ATXInstallation_InvalidPort());
                    } else if (port <= 1024) {
                        returnValue = FormValidation.warning(Messages.ATXInstallation_NeedsAdmin());
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     * Validates the server context path.
     *
     * @param contextPath the context path
     * @return the form validation
     */
    public FormValidation validateServerContextPath(final String contextPath) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isEmpty(contextPath)) {
            if (contextPath.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedValue());
            } else {
                final String pattern = "^[A-Za-z0-9./\\-_]+";
                if (!Pattern.matches(pattern, contextPath)) {
                    returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerContextPath());
                }
            }
        }
        return returnValue;
    }

    /**
     * Validates the archive miscellaneous files.
     *
     * @param expression the files expression
     * @return the form validation
     */
    public FormValidation validateArchiveMiscFiles(final String expression) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isEmpty(expression)) {
            if (expression.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedValue());
            } else {
                final String pattern = "[A-Za-z0-9./*]+";
                for (final String token : Util.tokenize(expression, ";")) {
                    if (!Pattern.matches(pattern, token)) {
                        returnValue = FormValidation.error(Messages.ATXInstallation_InvalidFileExpression());
                        break;
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     * Validates the covered attributes.
     *
     * @param expression the attribute expression
     * @return the form validation
     */
    public FormValidation validateCoveredAttributes(final String expression) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isEmpty(expression)) {
            if (expression.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedValue());
            } else {
                final String pattern = "(Designer|Name|Status|Testlevel|Tools|VersionCounter|"
                    + "Design Contact|Design Department|Estimated Duration \\[min]|"
                    + "Execution Priority|Test Comment)";
                for (final String token : Util.tokenize(expression, ";")) {
                    if (!Pattern.matches(pattern, token)) {
                        returnValue = FormValidation.warning(Messages.ATXInstallation_CustomAttributeExpression());
                        break;
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     * Validates a setting field.
     *
     * @param name  the setting name
     * @param value the current setting value
     * @return the form validation
     */
    public FormValidation validateSetting(final String name, final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (name != null) {
            switch (name) {
                case "serverURL":
                    returnValue = validateServerUrl(value);
                    break;
                case "serverPort":
                    returnValue = validateServerPort(value);
                    break;
                case "serverContextPath":
                    returnValue = validateServerContextPath(value);
                    break;
                case "archiveMiscFiles":
                    returnValue = validateArchiveMiscFiles(value);
                    break;
                case "coveredAttributes":
                    returnValue = validateCoveredAttributes(value);
                    break;
                default:
                    returnValue = validateParameterizedValue(value);
                    break;
            }
        }
        return returnValue;
    }

    /**
     * Validates the custom setting name and checks for duplicate entries.
     *
     * @param name the setting name
     * @return the form validation
     */
    public FormValidation validateCustomSettingName(final String name) {
        FormValidation returnValue = FormValidation.validateRequired(name);
        if (!StringUtils.isAlpha(name)) {
            returnValue = FormValidation.error(Messages.ATXInstallation_InvalidSettingName());
        } else {
            final ATXInstallation[] installations = ATXInstallation.all();
            if (installations.length > 0) {
                final ATXConfig config = installations[0].getConfig();
                if (config != null && config.getSettingByName(name).isPresent()) {
                    returnValue = FormValidation.warning(Messages.ATXInstallation_DuplicateSetting());
                }
            }
        }
        return returnValue;
    }

    /**
     * Validates the custom setting value.
     *
     * @param value the setting value
     * @return the form validation
     */
    public FormValidation validateCustomSettingValue(final String value) {
        return validateParameterizedValue(value);
    }

    /**
     * Tests the server connection by given server settings.
     *
     * @param serverUrl          the server URL
     * @param serverPort         the server port
     * @param serverContextPath  the server context path
     * @param useHttpsConnection if secure connection is used
     * @param ignoreSSL          specifies whether to ignore SSL issues
     * @return the form validation
     */
    public FormValidation testConnection(final String serverUrl, final String serverPort,
                                         final String serverContextPath, final boolean useHttpsConnection,
                                         final boolean ignoreSSL) {
        final String baseUrl = ATXUtil.getBaseUrl(serverUrl, serverPort, serverContextPath, useHttpsConnection);
        return testConnection(baseUrl, ignoreSSL);
    }

    /**
     * Tests the server connection by given base server URL.
     *
     * @param baseUrl   the base server URL
     * @param ignoreSSL specifies whether to ignore SSL issues
     * @return the form validation
     */
    public FormValidation testConnection(final String baseUrl, final boolean ignoreSSL) {
        FormValidation returnValue;
        if (StringUtils.isBlank(baseUrl)) {
            returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerUrl(null));
        } else if (baseUrl.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedConnection());
        } else {
            returnValue = checkConnection(baseUrl, ignoreSSL);
        }
        return returnValue;
    }

    /**
     * Checks the server connection by requesting the TEST-GUIDE API version endpoint.
     *
     * @param baseUrl   the base server URL
     * @param ignoreSSL specifies whether to ignore SSL issues
     * @return the form validation
     */
    private FormValidation checkConnection(final String baseUrl, final boolean ignoreSSL) {
        FormValidation returnValue = FormValidation.okWithMarkup(String.format(
            "<span style=\"font-weight: bold; color: #208CA3\">%s</span>",
            Messages.ATXInstallation_ValidConnection(baseUrl)));

        HttpURLConnection connection = null;
        try {
            final String appVersionUrl = String.format("%s/api/app-version-info", baseUrl);
            final URL url = new URL(appVersionUrl);

            // Handle SSL connection
            if (appVersionUrl.startsWith("https://")) {
                connection = (HttpsURLConnection) url.openConnection();
                if (ignoreSSL) {
                    ignoreSSLIssues((HttpsURLConnection) connection);
                }
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            // Check URL connection
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.connect();

            final int httpResponse = connection.getResponseCode();
            if (httpResponse != HttpURLConnection.HTTP_OK) {
                returnValue = FormValidation.warning(Messages.ATXInstallation_ServerNotReachable(baseUrl,
                    "Status code: " + httpResponse));
            } else {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                    final String content = in.readLine();
                    returnValue = parseServerInfo(content, baseUrl, returnValue);
                }
            }
        } catch (final MalformedURLException e) {
            returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerUrl(baseUrl));
        } catch (final IOException | NoSuchAlgorithmException | KeyManagementException e) {
            returnValue = FormValidation.warning(Messages.ATXInstallation_ServerNotReachable(baseUrl, e.getMessage()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return returnValue;
    }

    private FormValidation parseServerInfo(String content, String baseUrl, FormValidation returnValue){
        final JSONObject jsonObject = (JSONObject) new JsonSlurper().parseText(content);
        if (jsonObject != null) {
            final JSONObject info = jsonObject.optJSONObject("info");
            if (info != null) {
                final String license = info.getString("license");
                if (!license.contains("TraceTronic") ) {
                    returnValue = FormValidation.warning(Messages.ATXInstallation_InvalidServer(baseUrl));
                } else {
                    final String version = info.getString("version");
                    final ETPlugin.ToolVersion atxVersion = ETPlugin.ToolVersion.parse(version);
                    if (atxVersion.compareWithoutQualifierTo(ETPlugin.ATX_MIN_VERSION) < 0) {
                        returnValue = FormValidation.warning(
                            Messages.ATXInstallation_IncompatibleVersion(version,
                                ETPlugin.ATX_MIN_VERSION.toMicroString()));
                    }
                }
            }
        } else {
            returnValue = FormValidation.warning(Messages.ATXInstallation_InvalidServer(baseUrl));
        }
        return returnValue;
    }
}
