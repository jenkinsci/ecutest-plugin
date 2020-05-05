/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETComRegisterClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Common base class all report publishers which are using ECU-TEST as tool.
 */
public abstract class AbstractToolPublisher extends AbstractReportPublisher {

    @Nonnull
    private String toolName;
    private ETInstallation installation;

    /**
     * Instantiates a new {@link AbstractToolPublisher}.
     *
     * @param toolName the tool name
     */
    public AbstractToolPublisher(@Nonnull final String toolName) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
    }

    /**
     * Instantiates a new {@link AbstractToolPublisher}.
     *
     * @param toolName     the tool name
     * @param allowMissing specifies whether missing reports are allowed
     * @param runOnFailed  specifies whether this publisher even runs on a failed build
     * @param archiving    specifies whether archiving artifacts is enabled
     * @param keepAll      specifies whether artifacts are archived for all successful builds,
     *                     otherwise only the most recent
     */
    public AbstractToolPublisher(@Nonnull final String toolName, final boolean allowMissing,
                                 final boolean runOnFailed, final boolean archiving, final boolean keepAll) {
        super(allowMissing, runOnFailed, archiving, keepAll);
        this.toolName = StringUtils.trimToEmpty(toolName);
    }

    @Nonnull
    public String getToolName() {
        return toolName;
    }

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
        this.toolName = this.installation.getName();
    }

    /**
     * Configures an ECU-TEST client with given workspace settings.
     * Re-registers the according ECU-TEST COM server if option is enabled.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return the ECU-TEST client
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     * @throws ETPluginException    in case of a COM exception
     */
    protected ETClient getToolClient(final Run<?, ?> run, final FilePath workspace,
                                     final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException, ETPluginException {
        if (!isInstallationVerified(run.getEnvironment(listener))) {
            installation = configureToolInstallation(workspace.toComputer(), listener,
                run.getEnvironment(listener));
        }

        // Register ECU-TEST COM server
        final String expandedToolName = run.getEnvironment(listener).expand(installation.getName());
        if (installation.isRegisterComServer()) {
            final String installPath = getInstallation().getComExecutable(launcher);
            final ETComRegisterClient comClient = new ETComRegisterClient(expandedToolName, installPath);
            comClient.start(false, workspace, launcher, listener);
        }

        final String installPath = installation.getExecutable(launcher);
        final String workspaceDir = getWorkspaceDir(run, workspace);
        final String settingsDir = getSettingsDir(run, workspace);
        return new ETClient(expandedToolName, installPath, workspaceDir,
            settingsDir, StartETBuilder.DEFAULT_TIMEOUT, false);
    }

    /**
     * Configures the tool installation for functioning in the node and the environment.
     *
     * @param computer the computer
     * @param listener the listener
     * @param envVars  the environment variables
     * @return the tool installation
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     * @throws ETPluginException    if the selected tool installation is not configured
     */
    protected ETInstallation configureToolInstallation(final Computer computer,
                                                       final TaskListener listener, final EnvVars envVars)
        throws IOException, InterruptedException, ETPluginException {
        return configureToolInstallation(toolName, computer, listener, envVars);
    }

    /**
     * Verify the installation object and updates properties if needed.
     *
     * @param envVars the environment variables of the run
     */

    public boolean isInstallationVerified(final EnvVars envVars) {
        // Get selected ECU-TEST installation
        if (getInstallation() == null) {
            return false;
        }

        // Check consistency
        if (!getInstallation().getName().equals(envVars.expand(getToolName()))) {
            return false;
        }

        if (!getInstallation().getName().equals(getToolName())) {
            return false;
        }

        return true;
    }
}
