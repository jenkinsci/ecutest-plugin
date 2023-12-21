/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
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
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Validator to check ATX related form fields.
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
        } };

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
     * Validates the test.guide name.
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
        return testConnection(baseUrl, "", ignoreSSL);
    }

    /**
     * Tests the server connection by given server settings.
     *
     * @param serverUrl          the server URL
     * @param serverPort         the server port
     * @param serverContextPath  the server context path
     * @param useHttpsConnection if secure connection is used
     * @param proxyUrl           the proxy URL
     * @param ignoreSSL          specifies whether to ignore SSL issues
     * @return the form validation
     */
    public FormValidation testConnection(final String serverUrl, final String serverPort,
                                         final String serverContextPath, final boolean useHttpsConnection,
                                         final String proxyUrl, final boolean ignoreSSL) {
        final String baseUrl = ATXUtil.getBaseUrl(serverUrl, serverPort, serverContextPath, useHttpsConnection);
        return testConnection(baseUrl, proxyUrl, ignoreSSL);
    }

    /**
     * Tests the server connection by given base server URL.
     *
     * @param baseUrl   the base server URL
     * @param ignoreSSL specifies whether to ignore SSL issues
     * @return the form validation
     */
    public FormValidation testConnection(final String baseUrl, final boolean ignoreSSL) {
        return testConnection(baseUrl, "", ignoreSSL);
    }

    /**
     * Tests the server connection by given base server URL.
     *
     * @param baseUrl   the base server URL
     * @param proxyUrl  the proxy URL
     * @param ignoreSSL specifies whether to ignore SSL issues
     * @return the form validation
     */
    public FormValidation testConnection(final String baseUrl, final String proxyUrl, final boolean ignoreSSL) {
        final FormValidation returnValue;
        if (StringUtils.isBlank(baseUrl)) {
            returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerUrl(null));
        } else if (baseUrl.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.ATXInstallation_NoValidatedConnection());
        } else {
            returnValue = checkConnection(baseUrl, proxyUrl, ignoreSSL);
        }
        return returnValue;
    }

    /**
     * Checks the server connection by requesting the test.guide API version endpoint.
     *
     * @param baseUrl   the base server URL
     * @param proxyUrl  the proxy URL
     * @param ignoreSSL specifies whether to ignore SSL issues
     * @return the form validation
     */
    private FormValidation checkConnection(final String baseUrl, final String proxyUrl, final boolean ignoreSSL) {
        FormValidation returnValue = FormValidation.okWithMarkup(String.format(
            "<span style=\"font-weight: bold; color: #208CA3\">%s</span>",
            Messages.ATXInstallation_ValidConnection(baseUrl)));

        HttpURLConnection connection = null;
        try {
            final String appVersionUrl = String.format("%s/api/app-version-info", baseUrl);
            final URL url = new URL(appVersionUrl);

            // Handle proxy setting
            final Proxy proxy = configureProxy(proxyUrl);

            // Handle SSL connection
            if (appVersionUrl.startsWith("https://")) {
                connection = (HttpsURLConnection) url.openConnection(proxy);
                if (ignoreSSL) {
                    ignoreSSLIssues((HttpsURLConnection) connection);
                }
            } else {
                connection = (HttpURLConnection) url.openConnection(proxy);
            }

            // Check URL connection
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.connect();

            final int httpResponse = connection.getResponseCode();
            if (httpResponse != HttpURLConnection.HTTP_OK) {
                returnValue = FormValidation.error(Messages.ATXInstallation_ServerNotReachable(baseUrl,
                    "Status code: " + httpResponse));
            } else {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                    final String content = in.readLine();
                    returnValue = Optional.ofNullable(parseServerInfo(content, baseUrl)).orElse(returnValue);
                }
            }
        } catch (final MalformedURLException e) {
            returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServerUrl(baseUrl));
        } catch (final IOException | NoSuchAlgorithmException | KeyManagementException e) {
            returnValue = FormValidation.error(Messages.ATXInstallation_ServerNotReachable(baseUrl, e.getMessage()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            Authenticator.setDefault(null);
        }
        return returnValue;
    }

    /**
     * Configures a HTTP proxy based on given proxy URL.
     * Extracts user name and password from URL and sets as the default {@link Authenticator}.
     *
     * @param proxyUrl the proxy URL
     * @return the proxy instance or direct connection if proxy URL is empty
     */
    private Proxy configureProxy(final String proxyUrl) throws MalformedURLException, UnsupportedEncodingException {
        if (StringUtils.isBlank(proxyUrl)) {
            return Proxy.NO_PROXY;
        }

        final URL url = new URL(proxyUrl);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url.getHost(), url.getPort()));
        final String userInfo = url.getUserInfo();
        if (StringUtils.isNotBlank(userInfo)) {
            final String userName;
            final String password;
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                userName = URLDecoder.decode(userInfo, StandardCharsets.UTF_8.name());
                password = "";
            } else {
                userName = URLDecoder.decode(userInfo.substring(0, delimiter++), StandardCharsets.UTF_8.name());
                password = URLDecoder.decode(userInfo.substring(delimiter), StandardCharsets.UTF_8.name());
            }

            Authenticator.setDefault(new ProxyAuthenticator(userName, password));
        }
        return proxy;
    }

    /**
     * Parses the test.guide server information from JSON response.
     * Checks for valid license content and minimum supported test.guide version.
     *
     * @param content     the JSON content
     * @param baseUrl     the base server URL
     * @return the form validation
     */
    private FormValidation parseServerInfo(final String content, final String baseUrl) {
        FormValidation returnValue = null;
        final JSONObject jsonObject = (JSONObject) new JsonSlurper().parseText(content);
        if (jsonObject != null) {
            final JSONObject info = jsonObject.optJSONObject("info");
            if (info != null) {
                final String license = info.getString("license");
                if (!license.contains("TraceTronic") && !license.contains("tracetronic")) {
                    returnValue = FormValidation.warning(Messages.ATXInstallation_InvalidServer(baseUrl));
                } else {
                    final String version = info.getString("version");
                    final ToolVersion atxVersion = ToolVersion.parse(version);
                    if (atxVersion.compareWithoutQualifierTo(ETPlugin.ATX_MIN_VERSION) < 0) {
                        returnValue = FormValidation.warning(
                            Messages.ATXInstallation_IncompatibleVersion(version,
                                ETPlugin.ATX_MIN_VERSION.toMicroString()));
                    }
                }
            }
        } else {
            returnValue = FormValidation.error(Messages.ATXInstallation_InvalidServer(baseUrl));
        }
        return returnValue;
    }

    /**
     * Authenticator class for authentication with proxied network connections using user name and password.
     */
    private static final class ProxyAuthenticator extends Authenticator {

        private final String userName;
        private final String password;

        /**
         * Instantiates a {@link ProxyAuthenticator}.
         *
         * @param userName the proxy user name
         * @param password the proxy password
         */
        private ProxyAuthenticator(final String userName, final String password) {
            this.userName = userName;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password.toCharArray());
        }
    }
}
