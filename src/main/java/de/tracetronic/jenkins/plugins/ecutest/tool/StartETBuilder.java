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
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

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

    private final String workspaceDir;
    /**
     * @since 1.5
     */
    private final String settingsDir;
    private final boolean debugMode;

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
     */
    @DataBoundConstructor
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
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the workspace directory
     */
    public String getWorkspaceDir() {
        return workspaceDir;
    }

    /**
     * @return the settings directory
     */
    public String getSettingsDir() {
        return settingsDir;
    }

    /**
     * @return the debug mode
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        // Check OS running this build
        if (!ProcessUtil.checkOS(launcher, listener)) {
            return false;
        }

        // Initialize logger
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Expand build parameters
        final EnvVars buildEnvVars = build.getEnvironment(listener);
        final int expandedTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), buildEnvVars,
                String.valueOf(DEFAULT_TIMEOUT)));

        // Absolutize ECU-TEST workspace directory, if not absolute assume relative to build workspace
        String expandedWorkspaceDir = buildEnvVars.expand(workspaceDir);
        String expandedSettingsDir = buildEnvVars.expand(settingsDir);
        FilePath buildWorkspace = build.getWorkspace();
        if (buildWorkspace != null) {
            buildWorkspace = buildWorkspace.absolutize();
            expandedWorkspaceDir = EnvUtil.expandEnvVar(workspaceDir, buildEnvVars, buildWorkspace.getRemote());
            expandedWorkspaceDir = PathUtil.makeAbsolutePath(expandedWorkspaceDir, buildWorkspace);
            expandedSettingsDir = EnvUtil.expandEnvVar(settingsDir, buildEnvVars, buildWorkspace.getRemote());
            expandedSettingsDir = PathUtil.makeAbsolutePath(expandedSettingsDir, buildWorkspace);
        }

        // Check workspace validity
        final FilePath expandedWorkspacePath = new FilePath(launcher.getChannel(), expandedWorkspaceDir);
        final FilePath expandedSettingsPath = new FilePath(launcher.getChannel(), expandedSettingsDir);
        if (!checkWorkspace(expandedWorkspacePath, expandedSettingsPath, listener)) {
            return false;
        }

        // Delete logs if log publisher is present
        if (build.getProject().getPublishersList().get(ETLogPublisher.class) != null) {
            ETLogPublisher.RunListenerImpl.onStarted(expandedSettingsPath, listener);
        }

        // Get selected ECU-TEST installation
        final AbstractToolInstallation installation = configureToolInstallation(listener,
                build.getEnvironment(listener));

        // Start selected ECU-TEST
        if (installation instanceof ETInstallation) {
            final String toolName = build.getEnvironment(listener).expand(installation.getName());
            logger.logInfo(String.format("Starting %s...", toolName));
            final String installPath = installation.getExecutable(launcher);
            final ETClient etClient = new ETClient(toolName, installPath, expandedWorkspaceDir,
                    expandedSettingsDir, expandedTimeout, debugMode);
            if (etClient.start(true, launcher, listener)) {
                logger.logInfo(String.format("%s started successfully.", toolName));
            } else {
                logger.logError(String.format("Starting %s failed!", toolName));
                return false;
            }

            // Add action for injecting environment variables
            final int toolId = getToolId(build);
            final ToolEnvInvisibleAction envAction = new ToolEnvInvisibleAction(toolId, etClient);
            build.addAction(envAction);
        } else {
            logger.logError("The selected ECU-TEST installation is not configured for this node!");
            return false;
        }

        return true;
    }

    /**
     * Verifying the ECU-TEST workspace indicated by the existence of a ".workspace" directory.
     *
     * @param workspacePath
     *            the workspace path
     * @param settingsPath
     *            the settings path
     * @param listener
     *            the listener
     * @return {@code true} if valid workspace, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private boolean checkWorkspace(final FilePath workspacePath, final FilePath settingsPath,
            final BuildListener listener) throws IOException, InterruptedException {
        boolean isValid = false;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (!workspacePath.exists()) {
            logger.logError(String.format("ECU-TEST workspace at %s does not exist!",
                    workspacePath.getRemote()));
        } else if (!workspacePath.child(".workspace").exists()) {
            logger.logError(String.format("%s seems not to be a valid ECU-TEST workspace!",
                    workspacePath.getRemote()));
        } else if (!settingsPath.exists()) {
            logger.logError(String.format("ECU-TEST settings directory at %s does not exist!",
                    settingsPath.getRemote()));
        } else {
            isValid = true;
        }
        return isValid;
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
