/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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

    /**
     * @return the {@link ETInstallation} name
     */
    @Nonnull
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the ECU-TEST installation
     */
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
        if (installation == null) {
            installation = configureToolInstallation(workspace.toComputer(), listener,
                run.getEnvironment(listener));
        }
        final String installPath = installation.getExecutable(launcher);
        final String workspaceDir = getWorkspaceDir(run, workspace);
        final String settingsDir = getSettingsDir(run, workspace);
        final String expandedToolName = run.getEnvironment(listener).expand(installation.getName());
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
}
