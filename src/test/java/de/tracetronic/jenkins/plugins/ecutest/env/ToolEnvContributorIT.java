/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for {@link ToolEnvContributor}.
 */
public class ToolEnvContributorIT extends IntegrationTestBase {

    private final ToolEnvContributor contributor = new ToolEnvContributor();

    @Test
    public void testWithoutToolEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final EnvVars envVars = new EnvVars();
        contributor.buildEnvironmentFor(build, envVars, jenkins.createTaskListener());

        final List<ToolEnvInvisibleAction> envActions = build.getActions(ToolEnvInvisibleAction.class);
        assertEquals("No tool env action should exist", 0, envActions.size());
    }

    @Test
    public void testWithToolEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final int toolId = 0;
        final EnvVars envVars = new EnvVars();
        final ETClient etClient = new ETClient("ecu.test", "ecu.test", "workspace", "settings", 0, false);
        final ToolEnvInvisibleAction toolEnvAction = new ToolEnvInvisibleAction(toolId, etClient);
        build.addAction(toolEnvAction);

        contributor.buildEnvironmentFor(build, envVars, jenkins.createTaskListener());

        final List<ToolEnvInvisibleAction> envActions = build.getActions(ToolEnvInvisibleAction.class);
        assertEquals("Only one tool env action should exist", 1, envActions.size());

        assertEquals("TT_TOOL_NAME_0 should match env action", toolEnvAction.getToolName(),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_NAME + toolId));
        assertEquals("TT_TOOL_VERSION_0 should match env action", toolEnvAction.getToolVersion(),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_VERSION + toolId));
        assertEquals("TT_TOOL_INSTALL_0 should match env action", toolEnvAction.getToolInstallation(),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_INSTALL + toolId));
        assertEquals("TT_TOOL_WORKSPACE_0 should match env action", toolEnvAction.getToolWorkspace(),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_WORKSPACE + toolId));
        assertEquals("TT_TOOL_SETTINGS_0 should match env action", toolEnvAction.getToolSettings(),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_SETTINGS + toolId));
        assertEquals("TT_TOOL_TIMEOUT_0 should match env action", String.valueOf(toolEnvAction.getTimeout()),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_TIMEOUT + toolId));
        assertEquals("TT_TOOL_DEBUG_MODE_0 should match env action", toolEnvAction.isDebugMode() ? "true" : "false",
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_DEBUG_MODE + toolId));
        assertEquals("TT_TOOL_LAST_TBC_0 should match env action", String.valueOf(toolEnvAction.getLastTbc()),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_LAST_TBC + toolId));
        assertEquals("TT_TOOL_LAST_TCF_0 should match env action", String.valueOf(toolEnvAction.getLastTcf()),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_LAST_TCF + toolId));
    }
}
