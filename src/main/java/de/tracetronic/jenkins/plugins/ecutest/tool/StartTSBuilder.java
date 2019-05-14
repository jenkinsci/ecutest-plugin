/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.TSClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
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
 * Builder providing the start up of the Tool-Server.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StartTSBuilder extends AbstractToolBuilder {

    /**
     * Defines the default timeout to start up the Tool-Server.
     */
    public static final int DEFAULT_TIMEOUT = 120;

    /**
     * Defines the default port used for TCP communication with the Tool-Server.
     */
    public static final int DEFAULT_TCP_PORT = 5017;

    @CheckForNull
    private String toolLibsIni;
    @Nonnull
    private String tcpPort = String.valueOf(DEFAULT_TCP_PORT);
    /**
     * @since 1.18
     */
    private boolean keepInstance;

    /**
     * Instantiates a new {@link StartTSBuilder}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public StartTSBuilder(@Nonnull final String toolName) {
        super(toolName);
    }

    /**
     * @return the default timeout
     */
    @Override
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the default TCP port
     */
    public int getDefaultTcpPort() {
        return DEFAULT_TCP_PORT;
    }

    /**
     * @return the ToolLibs.ini path
     */
    @Nonnull
    public String getToolLibsIni() {
        return StringUtils.trimToEmpty(toolLibsIni);
    }

    /**
     * @param toolLibsIni the ToolLibs.ini path
     */
    @DataBoundSetter
    public void setToolLibsIni(@CheckForNull final String toolLibsIni) {
        this.toolLibsIni = Util.fixNull(toolLibsIni);
    }

    /**
     * @return the TCP port
     */
    @Nonnull
    public String getTcpPort() {
        return tcpPort;
    }

    /**
     * @param tcpPort the TCP port
     */
    @DataBoundSetter
    public void setTcpPort(@CheckForNull final String tcpPort) {
        this.tcpPort = StringUtils.defaultIfBlank(tcpPort, String.valueOf(getDefaultTcpPort()));
    }

    /**
     * @return specifies whether to re-use the previous instance
     */
    public boolean isKeepInstance() {
        return keepInstance;
    }

    /**
     * @param keepInstance the specifies whether to re-use the previous instance
     */
    @DataBoundSetter
    public void setKeepInstance(final boolean keepInstance) {
        this.keepInstance = keepInstance;
    }

    @Override
    public void performTool(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final List<String> foundProcesses = TSClient.checkProcesses(launcher, false);
        if (isKeepInstance() && !foundProcesses.isEmpty()) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("Re-using already running Tool-Server instance...");
        } else {
            // Expand build parameters
            final EnvVars buildEnvVars = run.getEnvironment(listener);
            final int expTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), buildEnvVars,
                String.valueOf(DEFAULT_TIMEOUT)));

            final int expTcpPort = Integer.parseInt(EnvUtil.expandEnvVar(getTcpPort(), buildEnvVars,
                String.valueOf(DEFAULT_TCP_PORT)));

            final String expToolLibs = buildEnvVars.expand(getToolLibsIni());
            final FilePath expToolLibsPath = new FilePath(launcher.getChannel(), expToolLibs);

            // Check ToolLibs.ini path
            if (!expToolLibsPath.exists()) {
                throw new ETPluginException(String.format("ToolLibs.ini path at %s does not exist!",
                    expToolLibsPath.getRemote()));
            }

            // Get selected ECU-TEST installation
            if (installation == null) {
                installation = configureToolInstallation(workspace.toComputer(), listener,
                    run.getEnvironment(listener));
            }

            // Start selected Tool-Server of related ECU-TEST installation
            final String installPath = installation.getTSExecutable(launcher);
            final TSClient tsClient = new TSClient(getToolName(), installPath, expTimeout, expToolLibs, expTcpPort);
            if (!tsClient.start(true, workspace, launcher, listener)) {
                throw new ETPluginException("Starting Tool-Server failed!");
            }
        }
    }

    /**
     * DescriptorImpl for {@link StartTSBuilder}.
     */
    @Symbol("startTS")
    @Extension(ordinal = 10009)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        @Override
        public int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * @return the default TCP port
         */
        public int getDefaultTcpPort() {
            return DEFAULT_TCP_PORT;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.StartTSBuilder_DisplayName();
        }

        /**
         * Validates the ToolLibs.ini path.
         *
         * @param value the ToolLibs.ini path
         * @return the form validation
         */
        public FormValidation doCheckToolLibsIni(@QueryParameter final String value) {
            return toolValidator.validateToolLibsIni(value);
        }

        /**
         * Validates the TCP port.
         *
         * @param value the TCP port
         * @return the form validation
         */
        public FormValidation doCheckTcpPort(@QueryParameter final String value) {
            return toolValidator.validateTcpPort(value);
        }
    }
}
