/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import hudson.EnvVars;
import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

/**
 * Class holding the configuration for exporting package attributes to test management system.
 */
public class ExportProjectAttributeConfig extends ExportAttributeConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ExportProjectAttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to export
     * @param credentialsId the credentials id
     * @param timeout       the export timeout
     */
    @DataBoundConstructor
    public ExportProjectAttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(filePath, credentialsId, timeout);
    }

    @Override
    public ExportProjectAttributeConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ExportProjectAttributeConfig(expFilePath, expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ExportProjectAttributeConfig}.
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends ExportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validateProjectFile(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ExportProjectAttributeConfig_DisplayName();
        }
    }
}
