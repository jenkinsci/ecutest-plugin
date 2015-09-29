/**
 * Copyright (c) 2015 TraceTronic GmbH
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

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;

import java.io.IOException;
import java.util.List;

/**
 * Contributor which adds various tool related variables into the build environment variables.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
    public static final String TOOL_WS = "TOOL_WS_";

    /**
     * Build environment variable part for the timeout connection to the tool.
     */
    public static final String TOOL_TIMEOUT = "TOOL_TIMEOUT_";

    /**
     * Build environment variable part enabling the debug mode of the tool.
     */
    public static final String TOOL_DEBUG = "TOOL_DEBUG_";

    @Override
    @SuppressWarnings("rawtypes")
    public void buildEnvironmentFor(final Run r, final EnvVars envs, final TaskListener listener)
            throws IOException, InterruptedException {

        final List<ToolEnvInvisibleAction> envActions = r.getActions(ToolEnvInvisibleAction.class);
        if (envActions.size() == 0) {
            return;
        }

        for (final ToolEnvInvisibleAction action : envActions) {
            final String id = String.valueOf(action.getToolId());
            envs.put(PREFIX + TOOL_NAME + id, action.getToolName());
            envs.put(PREFIX + TOOL_VERSION + id, action.getToolVersion());
            envs.put(PREFIX + TOOL_INSTALL + id, action.getToolInstallation());
            envs.put(PREFIX + TOOL_WS + id, action.getToolWorkspace());
            envs.put(PREFIX + TOOL_TIMEOUT + id, String.valueOf(action.getTimeout()));
            envs.put(PREFIX + TOOL_DEBUG + id, action.isDebug() ? "true" : "false");
        }
    }
}
