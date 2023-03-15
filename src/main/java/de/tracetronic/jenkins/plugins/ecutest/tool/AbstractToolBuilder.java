/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.security.MasterToSlaveCallable;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Common base class for all tool related task builders implemented in this plugin.
 */
@SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "Never used in a "
    + "critical way. Do not change in working legacy code.")
public abstract class AbstractToolBuilder extends Builder implements SimpleBuildStep {

    @Nonnull
    private final String toolName;
    @Nonnull
    private String timeout = String.valueOf(getDefaultTimeout());
    private ETInstallation installation;

    /**
     * Instantiates a {@link AbstractToolBuilder}.
     *
     * @param toolName the tool name
     */
    public AbstractToolBuilder(@Nonnull final String toolName) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
    }

    @Nonnull
    public String getToolName() {
        return toolName;
    }

    @Nonnull
    public String getTimeout() {
        return timeout;
    }

    @DataBoundSetter
    public void setTimeout(@CheckForNull final String timeout) {
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(getDefaultTimeout()));
    }

    public void setTimeout(final int timeout) {
        this.timeout = String.valueOf(timeout);
    }

    /**
     * Gets the default timeout.
     *
     * @return the default timeout
     */
    public abstract int getDefaultTimeout();

    public ETInstallation getInstallation() {
        return installation;
    }

    /**
     * Sets the ECU-TEST installation and the derived name.
     *
     * @param installation the ECU-TEST installation
     */
    @DataBoundSetter
    public void setInstallation(final ETInstallation installation) {
        this.installation = installation;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws InterruptedException, IOException {

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
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @throws InterruptedException the interrupted exception
     * @throws IOException          signals that an I/O exception has occurred
     * @throws ETPluginException    in case of tool operation errors
     */
    protected abstract void performTool(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
        throws InterruptedException, IOException, ETPluginException;

    /**
     * Gets the test identifier by the size of {@link ToolEnvInvisibleAction}s already added to the build.
     *
     * @param run the run
     * @return the tool id
     */
    protected int getToolId(final Run<?, ?> run) {
        final List<ToolEnvInvisibleAction> toolEnvActions = run.getActions(ToolEnvInvisibleAction.class);
        return toolEnvActions.size();
    }

    /**
     * Verify the installation object and updates properties if needed.
     *
     * @param envVars the environment variables of the run
     * @return {@code true} if installation for given tool name exists, {@code false} otherwise
     */
    protected boolean isInstallationVerified(final EnvVars envVars) {
        if (getInstallation() == null) {
            return false;
        } else if (!getInstallation().getName().equals(envVars.expand(getToolName()))) {
            return false;
        }

        return true;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param envVars the environment variables
     * @return the tool installation
     */
    @CheckForNull
    public ETInstallation getToolInstallation(final EnvVars envVars) {
        final String expToolName = envVars.expand(getToolName());
        return getDescriptor().getToolDescriptor().getInstallation(expToolName);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public AbstractToolDescriptor getDescriptor() {
        return (AbstractToolDescriptor) super.getDescriptor();
    }

    /**
     * Configures the tool installation for functioning in the node and the environment.
     *
     * @param computer the node
     * @param listener the listener
     * @param envVars  the environment variables
     * @return the tool installation
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     * @throws ETPluginException    if the selected tool installation is not configured
     */
    public ETInstallation configureToolInstallation(final Computer computer, final TaskListener listener,
                                                    final EnvVars envVars)
        throws IOException, InterruptedException, ETPluginException {
        ETInstallation installation = getToolInstallation(envVars);
        if (installation != null && computer != null) {
            final Node node = computer.getNode();
            if (node != null) {
                installation = installation.forNode(node, listener);
                installation = installation.forEnvironment(envVars);
            }
        } else {
            throw new ETPluginException("The selected ECU-TEST installation is not configured for this node!");
        }
        // Set the COM settings for the current ECU-TEST instance
        final VirtualChannel channel = computer.getChannel();
        if (channel != null) {
            channel.call(new SetComPropertyCallable(installation.getProgId(), installation.getTimeout()));
        }
        ETComProperty.getInstance().setProgId(installation.getProgId());
        ETComProperty.getInstance().setTimeout(installation.getTimeout());
        return installation;
    }

    /**
     * {@link Callable} providing remote access to set the current COM properties.
     */
    public static final class SetComPropertyCallable extends MasterToSlaveCallable<Void, IOException> {

        private static final long serialVersionUID = 1L;

        private final String progId;
        private final int timeout;

        /**
         * Instantiates a new {@link SetComPropertyCallable}.
         *
         * @param progId  the programmatic identifier
         * @param timeout the timeout
         */
        public SetComPropertyCallable(final String progId, final int timeout) {
            this.progId = progId;
            this.timeout = timeout;
        }

        @Override
        public Void call() throws IOException {
            ETComProperty.getInstance().setProgId(progId);
            ETComProperty.getInstance().setTimeout(timeout);
            return null;
        }
    }
}
