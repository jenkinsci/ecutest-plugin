/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env;

import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import hudson.model.InvisibleAction;

/**
 * Helper invisible action which is used for exchanging information between {@link ETClient}s
 * and other objects like {@link ToolEnvContributor}.
 */
public class ToolEnvInvisibleAction extends InvisibleAction {

    private final int toolId;
    private final String toolName;
    private final String toolVersion;
    private final String toolInstallation;
    private final String toolWorkspace;
    private final String toolSettings;
    private final int timeout;
    private final boolean debugMode;
    private final String lastTbc;
    private final String lastTcf;

    /**
     * Instantiates a new {@link ToolEnvInvisibleAction}.
     *
     * @param toolId     identifies this invisible action and is used as the suffix
     *                   for the tool related build environment variables
     * @param toolClient the tool client holding the relevant information
     */
    public ToolEnvInvisibleAction(final int toolId, final ETClient toolClient) {
        super();
        this.toolId = toolId;
        toolName = toolClient.getToolName();
        toolVersion = toolClient.getVersion();
        toolInstallation = toolClient.getInstallPath();
        toolWorkspace = toolClient.getWorkspaceDir();
        toolSettings = toolClient.getSettingsDir();
        timeout = toolClient.getTimeout();
        debugMode = toolClient.isDebugMode();
        lastTbc = toolClient.getLastTbc();
        lastTcf = toolClient.getLastTcf();
    }

    public int getToolId() {
        return toolId;
    }

    public String getToolName() {
        return toolName;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public String getToolInstallation() {
        return toolInstallation;
    }

    public String getToolWorkspace() {
        return toolWorkspace;
    }

    public String getToolSettings() {
        return toolSettings;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public String getLastTbc() {
        return lastTbc;
    }

    public String getLastTcf() {
        return lastTcf;
    }
}
