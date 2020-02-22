/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import com.google.common.collect.Maps;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class providing pipeline methods in order to get or create {@link ATXServer} instances.
 */
public class ATXPipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String KEY_ATX_NAME = "atxName";
    private static final String KEY_TOOL_NAME = "toolName";
    private static final String KEY_SERVER_URL = "fullServerURL";
    private static final String KEY_CONFIG = "config";

    private final CpsScript script;

    /**
     * Instantiates a new {@link ATXPipeline}.
     *
     * @param script the pipeline script
     */
    public ATXPipeline(final CpsScript script) {
        this.script = script;
    }

    /**
     * Gets a {@link ATXServer} instance by name.
     *
     * @param serverName the server name
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer server(final String serverName) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_ATX_NAME, serverName);

        final ATXServer server = (ATXServer) script.invokeMethod("getATXServer", stepVariables);
        server.setScript(script);
        return server;
    }

    /**
     * Gets a {@link ATXServer} instance by named argument.
     *
     * @param serverArgs the server arguments
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer server(final Map<String, Object> serverArgs) {
        final List<String> keysAsList = Collections.singletonList(KEY_ATX_NAME);
        if (!keysAsList.containsAll(serverArgs.keySet())) {
            throw new IllegalArgumentException("The server method requires the following arguments at least: "
                + keysAsList);
        }

        final ATXServer server = (ATXServer) script.invokeMethod("getATXServer", serverArgs);
        server.setScript(script);
        return server;
    }

    /**
     * Creates a new {@link ATXServer} instance with default settings.
     *
     * @param atxName  the ATX name
     * @param toolName the tool name
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName) {
        return newServer(atxName, toolName, new ATXConfig());
    }

    /**
     * Creates a new {@link ATXServer} instance with given {@link ATXConfig}.
     *
     * @param atxName  the ATX name
     * @param toolName the tool name
     * @param config   the ATX configuration
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName, final ATXConfig config) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_ATX_NAME, atxName);
        stepVariables.put(KEY_TOOL_NAME, toolName);
        stepVariables.put(KEY_CONFIG, config);

        final ATXServer server = (ATXServer) script.invokeMethod("newATXServer", stepVariables);
        server.setScript(script);
        return server;
    }

    /**
     * Creates a new {@link ATXServer} instance with given commonly used settings.
     *
     * @param atxName        the ATX name
     * @param toolName       the tool name
     * @param fullServerUrl  the full server URL
     * @param uploadToServer specifies whether ATX upload is enabled or not
     * @param authKey        the upload authentication key
     * @param projectId      the project id
     * @return the ATX server
     * @throws MalformedURLException the malformed url exception
     */
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName, final String fullServerUrl,
                               final boolean uploadToServer, final String authKey, final String projectId)
        throws MalformedURLException {
        final Map<String, Object> additionalSettings = Maps.newLinkedHashMap();
        additionalSettings.put("uploadToServer", uploadToServer);
        additionalSettings.put("uploadAuthenticationKey", authKey);
        additionalSettings.put("projectId", projectId);

        return newServer(atxName, toolName, fullServerUrl, additionalSettings);
    }

    /**
     * Creates a new {@link ATXServer} instance with named arguments.
     *
     * @param serverArgs the server arguments
     * @return the ATX server
     * @throws MalformedURLException the malformed URL exception
     */
    @Whitelisted
    public ATXServer newServer(final Map<String, Object> serverArgs) throws MalformedURLException {
        final List<String> requiredKeys = new ArrayList<>(Arrays.asList(KEY_ATX_NAME, KEY_TOOL_NAME));
        if (!serverArgs.keySet().containsAll(requiredKeys)) {
            throw new IllegalArgumentException("The newServer method requires the following arguments at least: "
                + requiredKeys);
        }

        if (serverArgs.containsKey(KEY_CONFIG) && serverArgs.get(KEY_CONFIG) instanceof ATXConfig) {
            return newServer(serverArgs.get(KEY_ATX_NAME).toString(), serverArgs.get(KEY_TOOL_NAME).toString(),
                (ATXConfig) serverArgs.get(KEY_CONFIG));
        }

        requiredKeys.add(KEY_SERVER_URL);
        final Map<String, Object> additionalArgs = new HashMap<>(serverArgs);
        requiredKeys.forEach(additionalArgs::remove);

        if (serverArgs.keySet().containsAll(requiredKeys)) {
            return newServer(serverArgs.get(KEY_ATX_NAME).toString(), serverArgs.get(KEY_TOOL_NAME).toString(),
                serverArgs.get(KEY_SERVER_URL).toString(), additionalArgs);
        } else {
            return newServer(serverArgs.get(KEY_ATX_NAME).toString(), serverArgs.get(KEY_TOOL_NAME).toString(),
                additionalArgs);
        }
    }

    /**
     * Creates a new {@link ATXServer} instance with server specific settings.
     *
     * @param atxName            the ATX name
     * @param toolName           the tool name
     * @param fullServerUrl      the full server URL
     * @param additionalSettings the additional settings
     * @return the ATX server
     * @throws MalformedURLException the malformed URL exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ATXServer newServer(final String atxName, final String toolName, final String fullServerUrl,
                                final Map<String, Object> additionalSettings) throws MalformedURLException {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_ATX_NAME, atxName);
        stepVariables.put(KEY_TOOL_NAME, toolName);

        final URL url = new URL(fullServerUrl);
        final String protocol = url.getProtocol();
        final boolean useHttpsConnection = "https".equals(protocol);
        final String port = String.valueOf(url.getPort());
        final String host = url.getHost();
        final String path = url.getPath().replaceFirst("/", "");

        final ATXConfig config = new ATXConfig();
        config.getSettingByName("serverURL").ifPresent(setting -> setting.setValue(host));
        config.getSettingByName("serverPort").ifPresent(setting -> setting.setValue(port));
        config.getSettingByName("serverContextPath").ifPresent(setting -> setting.setValue(path));
        config.getSettingByName("useHttpsConnection").ifPresent(setting -> setting.setValue(useHttpsConnection));

        additionalSettings.forEach((settingName, settingValue) -> {
            config.getSettingByName(settingName).ifPresent(setting -> setting.setValue(settingValue));
        });
        stepVariables.put(KEY_CONFIG, config);

        final ATXServer server = (ATXServer) script.invokeMethod("newATXServer", stepVariables);
        server.setScript(script);
        return server;
    }


    /**
     * Creates a new {@link ATXServer} instance with server specific settings.
     *
     * @param atxName    the ATX name
     * @param toolName   the tool name
     * @param serverArgs the server arguments
     * @return the ATX server
     */
    @SuppressWarnings({"unchecked"})
    private ATXServer newServer(final String atxName, final String toolName, final Map<String, Object> serverArgs) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_ATX_NAME, atxName);
        stepVariables.put(KEY_TOOL_NAME, toolName);

        final ATXConfig config = new ATXConfig();
        serverArgs.forEach((settingName, settingValue) -> {
            config.getSettingByName(settingName).ifPresent(setting -> setting.setValue(settingValue));
        });
        stepVariables.put(KEY_CONFIG, config);

        final ATXServer server = (ATXServer) script.invokeMethod("newATXServer", stepVariables);
        server.setScript(script);
        return server;
    }
}
