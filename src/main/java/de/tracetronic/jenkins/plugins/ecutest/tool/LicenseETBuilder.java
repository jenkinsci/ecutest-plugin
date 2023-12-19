/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Builder providing the license check of ecu.test.
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
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        if (!foundProcesses.isEmpty()) {
            logger.logInfo("Running ecu.test instance found, therefore a license is available...");
        } else {
            // Verify selected ecu.test installation
            final EnvVars envVars = run.getEnvironment(listener);
            if (!isInstallationVerified(envVars)) {
                setInstallation(configureToolInstallation(workspace.toComputer(), listener, envVars));
            }
            final String installPath = getInstallation().getExecutable(launcher);
            final String toolName = getInstallation().getName();
            if (StringUtils.isEmpty(installPath)) {
                throw new ETPluginException(String.format("ecu.test executable for '%s' could not be found!",
                    toolName));
            }

            // Check license of ecu.test
            final ETClient etClient = new ETClient(toolName, installPath, "", "", getDefaultTimeout(), false);
            etClient.setLicenseCheck(true);
            if (!etClient.checkLicense(launcher, listener)) {
                logger.logError(String.format("-> No valid license for '%s' found.", toolName));
                throw new ETPluginException(String.format("License check '%s' failed.", toolName));
            } else {
                logger.logInfo(String.format("-> Valid license for '%s' found.", toolName));
            }
        }
    }

    /**
     * DescriptorImpl for {@link LicenseETBuilder}.
     */
    @Symbol("checkETLicense")
    @Extension(ordinal = 10012)
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
