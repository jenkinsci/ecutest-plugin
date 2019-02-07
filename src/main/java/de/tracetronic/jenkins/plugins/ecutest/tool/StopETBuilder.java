/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
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
 * Builder providing to stop ECU-TEST.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StopETBuilder extends AbstractToolBuilder {

    /**
     * Defines the default timeout to stop ECU-TEST.
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Instantiates a new {@link StopETBuilder}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public StopETBuilder(@Nonnull final String toolName) {
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
        // Get selected ECU-TEST installation
        final ETInstallation installation = configureToolInstallation(workspace.toComputer(), listener,
            run.getEnvironment(listener));

        // Stop selected ECU-TEST
        final String toolName = run.getEnvironment(listener).expand(installation.getName());
        final EnvVars buildEnvVars = run.getEnvironment(listener);
        final int expTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), buildEnvVars,
            String.valueOf(DEFAULT_TIMEOUT)));
        final ETClient etClient = new ETClient(toolName, expTimeout);
        if (!etClient.stop(true, workspace, launcher, listener)) {
            throw new ETPluginException(String.format("Stopping %s failed.", toolName));
        }
    }

    /**
     * DescriptorImpl for {@link StopETBuilder}.
     */
    @Symbol("stopET")
    @Extension(ordinal = 10009)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        @Override
        public int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.StopETBuilder_DisplayName();
        }
    }
}
