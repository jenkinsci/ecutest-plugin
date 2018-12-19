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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
        final ETClient etClient = new ETClient("ECU-TEST", "ECU-TEST", "workspace", "settings", 0, false);
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
        assertEquals("TT_TOOL_DEBUG_0 should match env action", toolEnvAction.isDebug() ? "true" : "false",
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_DEBUG + toolId));
        assertEquals("TT_TOOL_LAST_TBC_0 should match env action", String.valueOf(toolEnvAction.getLastTbc()),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_LAST_TBC + toolId));
        assertEquals("TT_TOOL_LAST_TCF_0 should match env action", String.valueOf(toolEnvAction.getLastTcf()),
            envVars.get(ToolEnvContributor.PREFIX + ToolEnvContributor.TOOL_LAST_TCF + toolId));
    }
}
