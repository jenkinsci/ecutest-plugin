/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.TSClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Builder providing the tear down of the Tool-Server.
 */
public class StopTSBuilder extends AbstractToolBuilder {

    /**
     * Defines the default timeout to stop the Tool-Server.
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Instantiates a new {@link StopTSBuilder}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public StopTSBuilder(@Nonnull final String toolName) {
        super(toolName);
    }

    /**
     * @return the default timeout
     */
    @Override
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public void performTool(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        // Stop selected Tool-Server of related ECU-TEST installation
        final EnvVars envVars = run.getEnvironment(listener);
        final int expTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), envVars,
            String.valueOf(DEFAULT_TIMEOUT)));
        final TSClient tsClient = new TSClient(getToolName(), expTimeout);
        if (!tsClient.stop(true, workspace, launcher, listener)) {
            throw new ETPluginException("Stopping Tool-Server failed.");
        }
    }

    /**
     * DescriptorImpl for {@link StopTSBuilder}.
     */
    @Symbol("stopTS")
    @Extension(ordinal = 10008)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        @Override
        public int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.StopTSBuilder_DisplayName();
        }
    }
}
