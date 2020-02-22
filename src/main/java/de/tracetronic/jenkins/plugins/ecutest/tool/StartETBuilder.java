/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETComRegisterClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Builder providing the start up of ECU-TEST.
 */
public class StartETBuilder extends AbstractToolBuilder {

    /**
     * Defines the default timeout to start up ECU-TEST.
     */
    public static final int DEFAULT_TIMEOUT = 120;

    @CheckForNull
    private String workspaceDir;
    @CheckForNull
    private String settingsDir;
    private boolean debugMode;
    /**
     * Specifies whether to re-use the previous instance.
     *
     * @since 1.18
     */
    private boolean keepInstance;
    /**
     * Specifies whether to update all user libraries.
     *
     * @since 2.6
     */
    private boolean updateUserLibs;

    /**
     * Instantiates a new {@link StartETBuilder}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public StartETBuilder(@Nonnull final String toolName) {
        super(toolName);
    }

    @Override
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Nonnull
    public String getWorkspaceDir() {
        return StringUtils.trimToEmpty(workspaceDir);
    }

    @DataBoundSetter
    public void setWorkspaceDir(@CheckForNull final String workspaceDir) {
        this.workspaceDir = Util.fixNull(workspaceDir);
    }

    @Nonnull
    public String getSettingsDir() {
        return StringUtils.trimToEmpty(settingsDir);
    }

    @DataBoundSetter
    public void setSettingsDir(@CheckForNull final String settingsDir) {
        this.settingsDir = Util.fixNull(settingsDir);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    @DataBoundSetter
    public void setDebugMode(final boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isKeepInstance() {
        return keepInstance;
    }

    @DataBoundSetter
    public void setKeepInstance(final boolean keepInstance) {
        this.keepInstance = keepInstance;
    }

    public boolean isUpdateUserLibs() {
        return updateUserLibs;
    }

    @DataBoundSetter
    public void setUpdateUserLibs(final boolean updateUserLibs) {
        this.updateUserLibs = updateUserLibs;
    }

    @Override
    public void performTool(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        if (isKeepInstance() && !foundProcesses.isEmpty()) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("Re-using already running ECU-TEST instance...");
        } else {
            // Expand build parameters
            final EnvVars envVars = run.getEnvironment(listener);
            final int expTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), envVars,
                String.valueOf(DEFAULT_TIMEOUT)));

            // Absolutize ECU-TEST workspace directory, if not absolute assume relative to build workspace
            String expWorkspaceDir = EnvUtil.expandEnvVar(getWorkspaceDir(), envVars, workspace.getRemote());
            expWorkspaceDir = PathUtil.makeAbsolutePath(expWorkspaceDir, workspace);
            String expSettingsDir = EnvUtil.expandEnvVar(getSettingsDir(), envVars, workspace.getRemote());
            expSettingsDir = PathUtil.makeAbsolutePath(expSettingsDir, workspace);

            // Check existence of workspace and settings directory
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
            if (getInstallation() == null) {
                setInstallation(configureToolInstallation(workspace.toComputer(), listener, envVars));
            }

            // Register ECU-TEST COM server
            final String toolName = envVars.expand(getToolName());
            if (getInstallation().isRegisterComServer()) {
                final String installPath = getInstallation().getComExecutable(launcher);
                final ETComRegisterClient comClient = new ETComRegisterClient(toolName, installPath);
                comClient.start(false, workspace, launcher, listener);
            }

            // Start selected ECU-TEST
            final String installPath = getInstallation().getExecutable(launcher);
            final ETClient etClient = new ETClient(toolName, installPath, expWorkspaceDir, expSettingsDir,
                expTimeout, isDebugMode());
            if (!etClient.start(true, workspace, launcher, listener)) {
                throw new ETPluginException(String.format("Starting %s failed!", toolName));
            }

            if (isUpdateUserLibs() && !etClient.updateUserLibs(launcher, listener)) {
                throw new ETPluginException("Updating user libraries failed!");
            }

            // Add action for injecting environment variables
            final int toolId = getToolId(run);
            final ToolEnvInvisibleAction envAction = new ToolEnvInvisibleAction(toolId, etClient);
            run.addAction(envAction);
        }
    }

    /**
     * Checks whether the ECU-TEST workspace and settings directory exist.
     *
     * @param workspacePath the workspace path
     * @param settingsPath  the settings path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     * @throws ETPluginException    in case of missing workspace or settings directory
     */
    private void checkWorkspace(final FilePath workspacePath, final FilePath settingsPath)
        throws IOException, InterruptedException, ETPluginException {
        if (!workspacePath.exists()) {
            throw new ETPluginException(String.format("ECU-TEST workspace at %s does not exist!",
                workspacePath.getRemote()));
        }
        if (!settingsPath.exists()) {
            throw new ETPluginException(String.format("ECU-TEST settings directory at %s does not exist!",
                settingsPath.getRemote()));
        }
    }

    /**
     * DescriptorImpl for {@link StartETBuilder}.
     */
    @Symbol("startET")
    @Extension(ordinal = 10011)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        @Override
        public int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.StartETBuilder_DisplayName();
        }

        /**
         * Validates the workspace directory.
         *
         * @param value the workspace directory
         * @return the form validation
         */
        public FormValidation doCheckWorkspaceDir(@QueryParameter final String value) {
            return toolValidator.validateWorkspaceDir(value);
        }

        /**
         * Validates the settings directory.
         *
         * @param value the settings directory
         * @return the form validation
         */
        public FormValidation doCheckSettingsDir(@QueryParameter final String value) {
            return toolValidator.validateSettingsDir(value);
        }
    }
}
