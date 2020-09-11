/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
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
import java.util.List;

/**
 * Builder providing the license check of ECU-TEST.
 */
public class LicenseETBuilder extends AbstractToolBuilder {

    /**
     * Instantiates a new {@link LicenseETBuilder}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public LicenseETBuilder(@Nonnull final String toolName) {
        super(toolName);
    }

    @Override
    public int getDefaultTimeout() {
        return 0;
    }

    @Override
    public void performTool(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        // Check running instance
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        if (!foundProcesses.isEmpty()) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("Running ECU-TEST instance found, therefore a license is available...");
        } else {
            // Verify selected ECU-TEST installation
            final EnvVars envVars = run.getEnvironment(listener);
            // Verify selected ECU-TEST installation
            if (!isInstallationVerified(envVars)) {
                setInstallation(configureToolInstallation(workspace.toComputer(), listener, envVars));
            }

            // Check license of ECU-TEST
            final String installPath = getInstallation().getExecutable(launcher);
            final String toolName = getInstallation().getName();
            final ETClient etClient = new ETClient(toolName, installPath, "", "", getDefaultTimeout(), false);
            etClient.setLicenseCheck(true);
            if (!etClient.checkLicense(launcher, listener)) {
                throw new ETPluginException(String.format("License check %s failed.", toolName));
            }
        }
    }

    /**
     * DescriptorImpl for {@link LicenseETBuilder}.
     */
    @Symbol("checkETLicense")
    @Extension(ordinal = 10010)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        @Override
        public int getDefaultTimeout() {
            return 0;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.LicenseETBuilder_DisplayName();
        }
    }
}
