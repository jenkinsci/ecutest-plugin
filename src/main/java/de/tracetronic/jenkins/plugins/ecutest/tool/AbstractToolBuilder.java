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

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

/**
 * Common base class for all tool related task builders implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractToolBuilder extends Builder implements SimpleBuildStep {

    @Nonnull
    private final String toolName;
    @Nonnull
    private String timeout = String.valueOf(getDefaultTimeout());

    /**
     * Instantiates a {@link AbstractToolBuilder}.
     *
     * @param toolName
     *            the tool name
     */
    public AbstractToolBuilder(@Nonnull final String toolName) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
    }

    /**
     * Instantiates a {@link AbstractToolBuilder}.
     *
     * @param toolName
     *            the tool name
     * @param timeout
     *            the timeout
     * @deprecated since 1.11 use {@link #AbstractToolBuilder(String)}
     */
    @Deprecated
    public AbstractToolBuilder(final String toolName, final String timeout) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
        this.timeout = StringUtils.trimToEmpty(timeout);
    }

    /**
     * @return the tool name
     */
    @Nonnull
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the timeout
     */
    @Nonnull
    public String getTimeout() {
        return timeout;
    }

    /**
     * @param timeout
     *            the timeout
     */
    @DataBoundSetter
    public void setTimeout(@CheckForNull final String timeout) {
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(getDefaultTimeout()));
    }

    /**
     * Gets the default timeout.
     *
     * @return the default timeout
     */
    public abstract int getDefaultTimeout();

    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        // FIXME: workaround because pipeline node allocation does not create the actual workspace directory
        if (!workspace.exists()) {
            workspace.mkdirs();
        }

        try {
            ProcessUtil.checkOS(launcher);
            performTool(run, workspace, launcher, listener);
        } catch (final IOException e) {
            Util.displayIOException(e, listener);
            throw e;
        } catch (final ETPluginException e) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logError(e.getMessage());
            throw new AbortException(e.getMessage());
        }
    }

    /**
     * Performs the tool-specific build step operations.
     *
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @throws InterruptedException
     *             the interrupted exception
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws ETPluginException
     *             in case of tool operation errors
     */
    protected abstract void performTool(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException, ETPluginException;

    /**
     * Gets the test identifier by the size of {@link ToolEnvInvisibleAction}s already added to the build.
     *
     * @param run
     *            the run
     * @return the tool id
     */
    protected int getToolId(final Run<?, ?> run) {
        final List<ToolEnvInvisibleAction> toolEnvActions = run.getActions(ToolEnvInvisibleAction.class);
        return toolEnvActions.size();
    }

    /**
     * Configures the tool installation for functioning in the node and the environment.
     *
     * @param computer
     *            the node
     * @param listener
     *            the listener
     * @param envVars
     *            the environment variables
     * @return the tool installation
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     * @throws ETPluginException
     *             if the selected tool installation is not configured
     */
    protected ETInstallation configureToolInstallation(final Computer computer, final TaskListener listener,
            final EnvVars envVars) throws IOException, InterruptedException, ETPluginException {
        ETInstallation installation = getToolInstallation(envVars);
        if (installation != null && computer != null && computer.getNode() != null) {
            installation = installation.forNode(computer.getNode(), listener);
            installation = installation.forEnvironment(envVars);
        } else {
            throw new ETPluginException("The selected ECU-TEST installation is not configured for this node!");
        }
        return installation;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param envVars
     *            the environment variables
     * @return the tool installation
     */
    @CheckForNull
    public ETInstallation getToolInstallation(final EnvVars envVars) {
        final String expToolName = envVars.expand(getToolName());
        for (final ETInstallation installation : getDescriptor().getInstallations()) {
            if (StringUtils.equals(expToolName, installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public AbstractToolDescriptor getDescriptor() {
        return (AbstractToolDescriptor) super.getDescriptor();
    }
}
