/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import com.google.common.collect.Maps;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.io.Serializable;
import java.util.Map;

/**
 * Class holding ECU-TEST installation specific settings in order to start and stop instances.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ETInstallation installation;

    private transient CpsScript script;

    /**
     * Instantiates a new {@link ETInstance}.
     *
     * @param installation the ECU-TEST installation
     */
    public ETInstance(final ETInstallation installation) {
        this.installation = installation;
    }

    /**
     * @return the ECU-TEST installation
     */
    @Whitelisted
    public ETInstallation getInstallation() {
        return installation;
    }

    /**
     * Sets the pipeline script.
     *
     * @param script the pipeline script
     */
    public void setScript(final CpsScript script) {
        this.script = script;
    }

    /**
     * Starts ECU-TEST with default settings.
     */
    @Whitelisted
    public void start() {
        start("", "", StartETBuilder.DEFAULT_TIMEOUT, false, false, false);
    }

    /**
     * Starts ECU-TEST with given workspace settings.
     *
     * @param workspaceDir the workspace directory
     * @param settingsDir  the settings directory
     */
    @Whitelisted
    public void start(final String workspaceDir, final String settingsDir) {
        start(workspaceDir, settingsDir, StartETBuilder.DEFAULT_TIMEOUT, false, false, false);
    }

    /**
     * Starts ECU-TEST with all available settings.
     *
     * @param workspaceDir   the workspace directory
     * @param settingsDir    the settings directory
     * @param timeout        the timeout
     * @param debug          specifies whether to enable debug mode
     * @param keepInstance   specifies whether to re-use the previous instance
     * @param updateUserLibs specifies whether to update all user libraries
     */
    @Whitelisted
    public void start(final String workspaceDir, final String settingsDir, final int timeout, final boolean debug,
                      final boolean keepInstance, final boolean updateUserLibs) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("toolName", installation.getName());
        stepVariables.put("installation", installation);
        stepVariables.put("workspaceDir", workspaceDir);
        stepVariables.put("settingsDir", settingsDir);
        stepVariables.put("timeout", String.valueOf(timeout));
        stepVariables.put("debug", debug);
        stepVariables.put("keepInstance", keepInstance);
        stepVariables.put("updateUserLibs", updateUserLibs);
        script.invokeMethod("startET", stepVariables);
    }

    /**
     * Stops ECU-TEST with default settings.
     */
    @Whitelisted
    public void stop() {
        stop(StopETBuilder.DEFAULT_TIMEOUT);
    }

    /**
     * Stops ECU-TEST with all available settings.
     *
     * @param timeout the timeout
     */
    @Whitelisted
    public void stop(final int timeout) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("toolName", installation.getName());
        stepVariables.put("installation", installation);
        stepVariables.put("timeout", String.valueOf(timeout));
        script.invokeMethod("stopET", stepVariables);
    }

    /**
     * Starts Tool-Server with default settings.
     */
    @Whitelisted
    public void startTS() {
        startTS("", StartTSBuilder.DEFAULT_TCP_PORT, StartETBuilder.DEFAULT_TIMEOUT, false);
    }

    /**
     * Starts Tool-Server with all available settings.
     *
     * @param toolLibsIniPath the alternative ToolLibs.ini path
     * @param tcpPort         the alternative TCP port
     * @param timeout         the timeout
     * @param keepInstance    specifies whether to re-use the previous instance
     */
    @Whitelisted
    public void startTS(final String toolLibsIniPath, final int tcpPort,
                        final int timeout, final boolean keepInstance) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("toolName", installation.getName());
        stepVariables.put("installation", installation);
        stepVariables.put("toolLibsIniPath", toolLibsIniPath);
        stepVariables.put("tcpPort", tcpPort);
        stepVariables.put("timeout", String.valueOf(timeout));
        stepVariables.put("keepInstance", keepInstance);
        script.invokeMethod("startTS", stepVariables);
    }

    /**
     * Stops Tool-Server with default settings.
     */
    @Whitelisted
    public void stopTS() {
        stop(StopTSBuilder.DEFAULT_TIMEOUT);
    }

    /**
     * Stops Tool-Server with all available settings.
     *
     * @param timeout the timeout
     */
    @Whitelisted
    public void stopTS(final int timeout) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("toolName", installation.getName());
        stepVariables.put("installation", installation);
        stepVariables.put("timeout", String.valueOf(timeout));
        script.invokeMethod("stopTS", stepVariables);
    }

    // TODO: publish UNIT, Generator, TA, TMS
}
