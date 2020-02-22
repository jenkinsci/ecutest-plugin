/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Contributor which adds various tool related variables into the build environment variables.
 */
@Extension
public class ToolEnvContributor extends EnvironmentContributor {

    /**
     * Prefix for all build environment variables created by this {@link ToolEnvContributor}.
     */
    public static final String PREFIX = "TT_";

    /**
     * Build environment variable part for the tool name.
     */
    public static final String TOOL_NAME = "TOOL_NAME_";

    /**
     * Build environment variable part for the tool name.
     */
    public static final String TOOL_VERSION = "TOOL_VERSION_";

    /**
     * Build environment variable part for the tool installation directory.
     */
    public static final String TOOL_INSTALL = "TOOL_INSTALL_";

    /**
     * Build environment variable part for the workspace used by the tool.
     */
    public static final String TOOL_WORKSPACE = "TOOL_WORKSPACE_";

    /**
     * Build environment variable part for the settings used by the tool.
     */
    public static final String TOOL_SETTINGS = "TOOL_SETTINGS_";

    /**
     * Build environment variable part for the timeout connection to the tool.
     */
    public static final String TOOL_TIMEOUT = "TOOL_TIMEOUT_";

    /**
     * Build environment variable part enabling the debug mode of the tool.
     */
    public static final String TOOL_DEBUG_MODE = "TOOL_DEBUG_MODE_";

    /**
     * Build environment variable part for the last loaded TBC file path.
     */
    public static final String TOOL_LAST_TBC = "TOOL_LAST_TBC_";

    /**
     * Build environment variable part for the last loaded TCF file path.
     */
    public static final String TOOL_LAST_TCF = "TOOL_LAST_TCF_";

    @Override
    @SuppressWarnings("rawtypes")
    public void buildEnvironmentFor(@Nonnull final Run r, @Nonnull final EnvVars envs,
                                    @Nonnull final TaskListener listener) throws IOException, InterruptedException {
        final List<ToolEnvInvisibleAction> envActions = r.getActions(ToolEnvInvisibleAction.class);
        for (final ToolEnvInvisibleAction action : envActions) {
            final String id = String.valueOf(action.getToolId());
            envs.put(PREFIX + TOOL_NAME + id, action.getToolName());
            envs.put(PREFIX + TOOL_VERSION + id, action.getToolVersion());
            envs.put(PREFIX + TOOL_INSTALL + id, action.getToolInstallation());
            envs.put(PREFIX + TOOL_WORKSPACE + id, action.getToolWorkspace());
            envs.put(PREFIX + TOOL_SETTINGS + id, action.getToolSettings());
            envs.put(PREFIX + TOOL_TIMEOUT + id, String.valueOf(action.getTimeout()));
            envs.put(PREFIX + TOOL_DEBUG_MODE + id, action.isDebugMode() ? "true" : "false");
            envs.put(PREFIX + TOOL_LAST_TBC + id, action.getLastTbc());
            envs.put(PREFIX + TOOL_LAST_TCF + id, action.getLastTcf());
        }
    }
}
