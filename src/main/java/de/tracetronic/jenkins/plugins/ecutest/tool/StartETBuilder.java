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
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Project;
import hudson.model.Run;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;

/**
 * Builder providing the start up of ECU-TEST.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StartETBuilder extends AbstractToolBuilder {

    /**
     * Defines the default timeout to start up ECU-TEST.
     */
    public static final int DEFAULT_TIMEOUT = 120;

    @CheckForNull
    private String workspaceDir;
    /**
     * @since 1.5
     */
    @CheckForNull
    private String settingsDir;
    private boolean debugMode;

    /**
     * Instantiates a new {@link StartETBuilder}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public StartETBuilder(@Nonnull final String toolName) {
        super(toolName);
    }

    /**
     * Instantiates a new {@link StartETBuilder}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param workspaceDir
     *            the ECU-TEST workspace directory
     * @param settingsDir
     *            the ECU-TEST settings directory
     * @param timeout
     *            the timeout
     * @param debugMode
     *            the debug mode
     * @deprecated since 1.11 use {@link #StartETBuilder(String)}
     */
    @Deprecated
    public StartETBuilder(final String toolName, final String workspaceDir, final String settingsDir,
            final String timeout, final boolean debugMode) {
        super(toolName, StringUtils.defaultIfEmpty(timeout, String.valueOf(DEFAULT_TIMEOUT)));
        this.workspaceDir = StringUtils.trimToEmpty(workspaceDir);
        this.settingsDir = StringUtils.trimToEmpty(settingsDir);
        this.debugMode = debugMode;
    }

    /**
     * Instantiates a new {@link StartETBuilder}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param workspaceDir
     *            the ECU-TEST workspace directory
     * @param timeout
     *            the timeout
     * @param debugMode
     *            the debug mode
     * @deprecated since 1.5 use {@link #StartETBuilder(String, String, String, String, boolean)}
     */
    @Deprecated
    public StartETBuilder(final String toolName, final String workspaceDir, final String timeout,
            final boolean debugMode) {
        this(toolName, workspaceDir, null, timeout, debugMode);
    }

    /**
     * Convert legacy configuration into the new class structure.
     *
     * @return an instance of this class with all the new fields transferred from the old structure to the new one
     */
    public final Object readResolve() {
        if (settingsDir == null) {
            return new StartETBuilder(getToolName(), workspaceDir, null, getTimeout(), debugMode);
        }
        return this;
    }

    /**
     * @return the default timeout
     */
    @Override
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the workspace directory
     */
    @Nonnull
    public String getWorkspaceDir() {
        return StringUtils.trimToEmpty(workspaceDir);
    }

    /**
     * @return the settings directory
     */
    @Nonnull
    public String getSettingsDir() {
        return StringUtils.trimToEmpty(settingsDir);
    }

    /**
     * @return the debug mode
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * @param workspaceDir
     *            the workspace directory
     */
    @DataBoundSetter
    public void setWorkspaceDir(@CheckForNull final String workspaceDir) {
        this.workspaceDir = Util.fixNull(workspaceDir);
    }

    /**
     * @param settingsDir
     *            the settings directory
     */
    @DataBoundSetter
    public void setSettingsDir(@CheckForNull final String settingsDir) {
        this.settingsDir = Util.fixNull(settingsDir);
    }

    /**
     * @param debugMode
     *            the debug mode
     */
    @DataBoundSetter
    public void setDebugMode(final boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public void performTool(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        // Expand build parameters
        final EnvVars buildEnvVars = run.getEnvironment(listener);
        final int expTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), buildEnvVars,
                String.valueOf(DEFAULT_TIMEOUT)));

        // Absolutize ECU-TEST workspace directory, if not absolute assume relative to build workspace
        String expWorkspaceDir = EnvUtil.expandEnvVar(getWorkspaceDir(), buildEnvVars, workspace.getRemote());
        expWorkspaceDir = PathUtil.makeAbsolutePath(expWorkspaceDir, workspace);
        String expSettingsDir = EnvUtil.expandEnvVar(getSettingsDir(), buildEnvVars, workspace.getRemote());
        expSettingsDir = PathUtil.makeAbsolutePath(expSettingsDir, workspace);

        // Check workspace validity
        final FilePath expWorkspacePath = new FilePath(launcher.getChannel(), expWorkspaceDir);
        final FilePath expSettingsPath = new FilePath(launcher.getChannel(), expSettingsDir);
        checkWorkspace(expWorkspacePath, expSettingsPath);

        // Delete logs if log publisher is present
        final Object parent = run.getParent();
        if (parent instanceof Project
                && ((Project<?, ?>) run.getParent()).getPublishersList().get(ETLogPublisher.class) != null) {
            ETLogPublisher.RunListenerImpl.onStarted(expSettingsPath, listener);
        }

        // Get selected ECU-TEST installation
        final ETInstallation installation = configureToolInstallation(workspace.toComputer(), listener,
                run.getEnvironment(listener));

        // Start selected ECU-TEST
        final String toolName = run.getEnvironment(listener).expand(installation.getName());
        final String installPath = installation.getExecutable(launcher);
        final ETClient etClient = new ETClient(toolName, installPath, expWorkspaceDir, expSettingsDir,
                expTimeout, isDebugMode());
        if (!etClient.start(true, workspace, launcher, listener)) {
            throw new ETPluginException(String.format("Starting %s failed!", toolName));
        }

        // Add action for injecting environment variables
        final int toolId = getToolId(run);
        final ToolEnvInvisibleAction envAction = new ToolEnvInvisibleAction(toolId, etClient);
        run.addAction(envAction);
    }

    /**
     * Verifying the ECU-TEST workspace indicated by the existence of a ".workspace" directory.
     *
     * @param workspacePath
     *            the workspace path
     * @param settingsPath
     *            the settings path
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     * @throws ETPluginException
     *             in case of an invalid workspace
     */
    private void checkWorkspace(final FilePath workspacePath, final FilePath settingsPath) throws IOException,
    InterruptedException, ETPluginException {
        if (!workspacePath.exists()) {
            throw new ETPluginException(String.format("ECU-TEST workspace at %s does not exist!",
                    workspacePath.getRemote()));
        } else if (!workspacePath.child(".workspace").exists()) {
            throw new ETPluginException(String.format("%s seems not to be a valid ECU-TEST workspace!",
                    workspacePath.getRemote()));
        } else if (!settingsPath.exists()) {
            throw new ETPluginException(String.format("ECU-TEST settings directory at %s does not exist!",
                    settingsPath.getRemote()));
        }
    }

    /**
     * DescriptorImpl for {@link StartETBuilder}.
     */
    @Extension(ordinal = 1006)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        /**
         * Instantiates a {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(StartETBuilder.class);
            load();
        }

        @Override
        public int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * Validates the workspace directory.
         *
         * @param value
         *            the workspace directory
         * @return the form validation
         */
        public FormValidation doCheckWorkspaceDir(@QueryParameter final String value) {
            return toolValidator.validateWorkspaceDir(value);
        }

        /**
         * Validates the settings directory.
         *
         * @param value
         *            the settings directory
         * @return the form validation
         */
        public FormValidation doCheckSettingsDir(@QueryParameter final String value) {
            return toolValidator.validateSettingsDir(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.StartETBuilder_DisplayName();
        }
    }
}
