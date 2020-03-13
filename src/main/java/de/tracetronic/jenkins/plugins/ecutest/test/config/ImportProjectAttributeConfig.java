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
 * Class holding the configuration for importing project attributes from test management system.
 */
public class ImportProjectAttributeConfig extends ImportAttributeConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportProjectAttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to import
     * @param credentialsId the credentials id
     * @param timeout       the import timeout
     */
    @DataBoundConstructor
    public ImportProjectAttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(filePath, credentialsId, timeout);
    }

    @Override
    public ImportProjectAttributeConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ImportProjectAttributeConfig(expFilePath, expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ImportProjectAttributeConfig}.
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends ImportAttributeConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validateProjectFile(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportProjectAttributeConfig_DisplayName();
        }
    }
}
