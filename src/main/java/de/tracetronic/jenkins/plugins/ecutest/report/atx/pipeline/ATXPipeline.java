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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import com.google.common.collect.Maps;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;

/**
 * Class providing pipeline methods in order to get or create {@link ATXServer} instances.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXPipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CpsScript script;

    /**
     * Instantiates a new {@link ATXPipeline}.
     *
     * @param script
     *            the pipeline script
     */
    public ATXPipeline(final CpsScript script) {
        this.script = script;
    }

    /**
     * Gets a {@link ATXServer} instance by name.
     *
     * @param serverName
     *            the server name
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer server(final String serverName) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("atxName", serverName);
        final ATXServer server = (ATXServer) script.invokeMethod("getATXServer", stepVariables);
        server.setScript(script);
        return server;
    }

    /**
     * Gets a {@link ATXServer} instance by named argument.
     *
     * @param serverArgs
     *            the server arguments
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer server(final Map<String, Object> serverArgs) {
        final List<String> keysAsList = Arrays.asList("atxName");
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
     * @param atxName
     *            the ATX name
     * @param toolName
     *            the tool name
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName) {
        return newServer(atxName, toolName, null);
    }

    /**
     * Creates a new {@link ATXServer} instance with given {@link ATXConfig}.
     *
     * @param atxName
     *            the ATX name
     * @param toolName
     *            the tool name
     * @param config
     *            the ATX configuration
     * @return the ATX server
     */
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName, final ATXConfig config) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("atxName", atxName);
        stepVariables.put("toolName", toolName);
        stepVariables.put("config", config);
        final ATXServer server = (ATXServer) script.invokeMethod("newATXServer", stepVariables);
        server.setScript(script);
        return server;
    }

    /**
     * Creates a new {@link ATXServer} instance with server specific settings.
     *
     * @param atxName
     *            the ATX name
     * @param toolName
     *            the tool name
     * @param serverUrl
     *            the server URL
     * @param uploadToServer
     *            the upload to server
     * @param authKey
     *            the authentication key
     * @param projectId
     *            the project id
     * @return the ATX server
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Whitelisted
    public ATXServer newServer(final String atxName, final String toolName, final String serverUrl,
            final boolean uploadToServer, final String authKey, final String projectId) throws MalformedURLException {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("atxName", atxName);
        stepVariables.put("toolName", toolName);

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
                    setting.setCurrentValue(true);
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
                    continue;
            }
        }

        final Map<String, List<ATXSetting>> configMap = config.getConfigMap();
        configMap.put("uploadConfig", uploadSettings);
        config = new ATXConfig(configMap, config.getCustomSettings());
        stepVariables.put("config", config);

        final ATXServer server = (ATXServer) script.invokeMethod("newATXServer", stepVariables);
        server.setScript(script);
        return server;
    }

    /**
     * Creates a new {@link ATXServer} instance with named arguments.
     *
     * @param serverArgs
     *            the server arguments
     * @return the ATX server
     * @throws MalformedURLException
     *             the malformed URL exception
     */
    @Whitelisted
    public ATXServer newServer(final Map<String, Object> serverArgs) throws MalformedURLException {
        final List<String> requiredKeys = new ArrayList<String>(Arrays.asList("atxName", "toolName"));
        if (!serverArgs.keySet().containsAll(requiredKeys)) {
            throw new IllegalArgumentException("The newServer method requires the following arguments at least: "
                    + requiredKeys);
        }

        if (serverArgs.containsKey("config") && serverArgs.get("config") instanceof ATXConfig) {
            return newServer((String) serverArgs.get("atxName"), (String) serverArgs.get("toolName"),
                    (ATXConfig) serverArgs.get("config"));
        }

        requiredKeys.addAll(Arrays.asList("serverUrl", "uploadToServer", "authKey", "projectId"));
        if (serverArgs.keySet().containsAll(requiredKeys)) {
            return newServer(serverArgs.get("atxName").toString(), serverArgs.get("toolName").toString(),
                    serverArgs.get("serverUrl").toString(), (boolean) serverArgs.get("uploadToServer"),
                    serverArgs.get("authKey").toString(), serverArgs.get("projectId").toString());
        } else {
            return newServer(serverArgs.get("atxName").toString(), serverArgs.get("toolName").toString());
        }
    }
}
