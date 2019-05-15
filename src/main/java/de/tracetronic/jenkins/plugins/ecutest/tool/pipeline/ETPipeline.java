/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import com.google.common.collect.Maps;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline.ATXServer;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETToolProperty;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class providing pipeline methods in order to get or create {@link ETInstallation} instances.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETPipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String KEY_TOOL_NAME = "toolName";
    private static final String KEY_INSTALL_PATH = "installPath";
    private static final String KEY_PROPERTY = "property";

    private final CpsScript script;

    /**
     * Instantiates a new {@link ETPipeline}.
     *
     * @param script the pipeline script
     */
    public ETPipeline(final CpsScript script) {
        this.script = script;
    }

    /**
     * Gets a {@link ETInstallation} instance by name.
     *
     * @param toolName the tool name
     * @return the ECU-TEST installation
     */
    @Whitelisted
    public ETInstance installation(final String toolName) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, toolName);
        final ETInstance installation = (ETInstance) script.invokeMethod("getETInstallation", stepVariables);
        installation.setScript(script);
        return installation;
    }

    /**
     * Gets a {@link ETInstallation} instance by named argument.
     *
     * @param installArgs the installation arguments
     * @return the ECU-TEST installation
     */
    @Whitelisted
    public ETInstance installation(final Map<String, Object> installArgs) {
        final List<String> keysAsList = Collections.singletonList(KEY_TOOL_NAME);
        if (!keysAsList.containsAll(installArgs.keySet())) {
            throw new IllegalArgumentException("The installation method requires the following arguments at least: "
                + keysAsList);
        }
        final ETInstance installation = (ETInstance) script.invokeMethod("getETInstallation", installArgs);
        installation.setScript(script);
        return installation;
    }

    /**
     * Creates a new {@link ETInstance} instance with given name and installation path.
     *
     * @param toolName    the toolName name
     * @param installPath the installation path
     * @return the ECU-TEST installation server
     */
    @Whitelisted
    public ETInstance newInstallation(final String toolName, final String installPath) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, toolName);
        stepVariables.put(KEY_INSTALL_PATH, installPath);
        final ETInstance server = (ETInstance) script.invokeMethod("newETInstallation", stepVariables);
        server.setScript(script);
        return server;
    }

    /**
     * Creates a new {@link ATXServer} instance with server specific settings.
     *
     * @param toolName    the tool name
     * @param installPath the installation path
     * @param progId      the COM programmatic identifier
     * @param timeout     the COM timeout
     * @return the ECU-TEST installation server
     */
    @Whitelisted
    public ETInstance newInstallation(final String toolName, final String installPath,
                                      final String progId, final int timeout) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, toolName);
        stepVariables.put(KEY_INSTALL_PATH, installPath);
        ETToolProperty property = new ETToolProperty(progId, timeout);
        stepVariables.put(KEY_PROPERTY, property);

        final ETInstance installation = (ETInstance) script.invokeMethod("newETInstallation", stepVariables);
        installation.setScript(script);
        return installation;
    }

    /**
     * Creates a new {@link ETInstallation} instance with named arguments.
     *
     * @param installArgs the installation arguments
     * @return the ECU-TEST installation
     */
    @Whitelisted
    public ETInstance newInstallation(final Map<String, Object> installArgs) {
        final List<String> requiredKeys = new ArrayList<>(Arrays.asList(KEY_TOOL_NAME, KEY_INSTALL_PATH));
        if (!installArgs.keySet().containsAll(requiredKeys)) {
            throw new IllegalArgumentException("The newInstallation method requires the following arguments at least: "
                + requiredKeys);
        }

        if (installArgs.containsKey(KEY_PROPERTY) && installArgs.get(KEY_PROPERTY) instanceof ETToolProperty) {
            ETToolProperty property = (ETToolProperty) installArgs.get(KEY_PROPERTY);
            return newInstallation((String) installArgs.get(KEY_TOOL_NAME), (String) installArgs.get(KEY_INSTALL_PATH),
                property.getProgId(), property.getTimeout());
        }

        requiredKeys.addAll(Arrays.asList("progId", "timeout"));
        if (installArgs.keySet().containsAll(requiredKeys)) {
            return newInstallation(installArgs.get(KEY_TOOL_NAME).toString(),
                installArgs.get(KEY_INSTALL_PATH).toString(),
                installArgs.get("progId").toString(), (int) installArgs.get("timeout"));
        } else {
            return newInstallation(installArgs.get(KEY_TOOL_NAME).toString(),
                installArgs.get(KEY_INSTALL_PATH).toString());
        }
    }
}
