/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.tool;

import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForNull;

import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;

/**
 * Common base class for all tool related task builders implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractToolBuilder extends Builder {

    private final String toolName;
    private final String timeout;

    /**
     * Instantiates a {@link AbstractToolBuilder}.
     *
     * @param toolName
     *            the tool name
     * @param timeout
     *            the timeout
     */
    public AbstractToolBuilder(final String toolName, final String timeout) {
        super();
        this.toolName = toolName;
        this.timeout = timeout;
    }

    /**
     * @return the tool name
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * Gets the test identifier by the size of {@link ToolEnvInvisibleAction}s already added to the build.
     *
     * @param build
     *            the build
     * @return the tool id
     */
    protected int getToolId(final AbstractBuild<?, ?> build) {
        final List<ToolEnvInvisibleAction> toolEnvActions = build.getActions(ToolEnvInvisibleAction.class);
        return toolEnvActions.size();
    }

    /**
     * Configures the tool installation for functioning in the node and the environment.
     *
     * @param listener
     *            the listener
     * @param env
     *            the environment
     * @return the tool installation
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    @CheckForNull
    protected AbstractToolInstallation configureToolInstallation(final BuildListener listener,
            final EnvVars env) throws IOException, InterruptedException {
        AbstractToolInstallation installation = getToolInstallation(env);
        if (installation != null) {
            installation = installation.forNode(Computer.currentComputer().getNode(), listener);
            installation = installation.forEnvironment(env);
        }
        return installation;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param env
     *            the environment
     * @return the tool installation
     */
    @CheckForNull
    public AbstractToolInstallation getToolInstallation(final EnvVars env) {
        final String expToolName = env.expand(toolName);
        for (final AbstractToolInstallation installation : getDescriptor().getInstallations()) {
            if (expToolName != null && expToolName.equals(installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    @Override
    public AbstractToolDescriptor getDescriptor() {
        return (AbstractToolDescriptor) super.getDescriptor();
    }
}
