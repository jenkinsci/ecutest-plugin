/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import com.google.common.collect.Maps;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class providing pipeline methods in order to get or create {@link ATXServer} instances.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXPipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String KEY_CONFIG = "config";
    private static final String KEY_ATX_NAME = "atxName";
    private static final String KEY_TOOL_NAME = "toolName";

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
            throw new IllegalArgumentException("The newServer method requires the following arguments at least: "
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
        return newServer(atxName, toolName, null);
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
     * Creates a new {@link ATXServer} instance with server specific settings.
     *
     * @param atxName        the ATX name
     * @param toolName       the tool name
     * @param serverUrl      the server URL
     * @param uploadToServer the upload to server
     * @param authKey        the authentication key
     * @param projectId      the project id
     * @return the ATX server
     * @throws MalformedURLException the malformed URL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName, final String serverUrl,
                               final boolean uploadToServer, final String authKey, final String projectId)
        throws MalformedURLException {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_ATX_NAME, atxName);
        stepVariables.put(KEY_TOOL_NAME, toolName);

        final URL url = new URL(serverUrl);
        final String protocol = url.getProtocol();
        final boolean useHttpsConnection = "https".equals(protocol);
        final String port = String.valueOf(url.getPort());
        final String host = url.getHost();
        final String path = url.getPath().replaceFirst("/", "");

        ATXConfig config = new ATXConfig();
        final List<ATXSetting> uploadSettings = config.getConfigByName("uploadConfig");
        for (final ATXSetting setting : uploadSettings) {
            switch (setting.getName()) {
                case "uploadToServer":
                    setting.setCurrentValue(uploadToServer);
                    break;
                case "serverURL":
                    setting.setCurrentValue(host);
                    break;
                case "useHttpsConnection":
                    setting.setCurrentValue(useHttpsConnection);
                    break;
                case "serverPort":
                    setting.setCurrentValue(port);
                    break;
                case "serverContextPath":
                    setting.setCurrentValue(path);
                    break;
                case "uploadAuthenticationKey":
                    setting.setCurrentValue(authKey);
                    break;
                case "projectId":
                    setting.setCurrentValue(projectId);
                    break;
                default:
                    break;
            }
        }

        final Map<String, List<ATXSetting>> configMap = config.getConfigMap();
        configMap.put("uploadConfig", uploadSettings);
        config = new ATXConfig(configMap, config.getCustomSettings());
        stepVariables.put(KEY_CONFIG, config);

        final ATXServer server = (ATXServer) script.invokeMethod("newATXServer", stepVariables);
        server.setScript(script);
        return server;
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
            return newServer((String) serverArgs.get(KEY_ATX_NAME), (String) serverArgs.get(KEY_TOOL_NAME),
                (ATXConfig) serverArgs.get(KEY_CONFIG));
        }

        requiredKeys.addAll(Arrays.asList("serverUrl", "uploadToServer", "authKey", "projectId"));
        if (serverArgs.keySet().containsAll(requiredKeys)) {
            return newServer(serverArgs.get(KEY_ATX_NAME).toString(), serverArgs.get(KEY_TOOL_NAME).toString(),
                serverArgs.get("serverUrl").toString(), (boolean) serverArgs.get("uploadToServer"),
                serverArgs.get("authKey").toString(), serverArgs.get("projectId").toString());
        } else {
            return newServer(serverArgs.get(KEY_ATX_NAME).toString(), serverArgs.get(KEY_TOOL_NAME).toString());
        }
    }
}
