/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
 * Class holding the configuration for importing package attributes from test management system.
 */
public class ImportPackageAttributeConfig extends ImportAttributeConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportPackageAttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to import
     * @param credentialsId the credentials id
     * @param timeout       the import timeout
     */
    @DataBoundConstructor
    public ImportPackageAttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(filePath, credentialsId, timeout);
    }

    @Override
    public ImportPackageAttributeConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ImportPackageAttributeConfig(expFilePath, expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ImportPackageAttributeConfig}.
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends ImportAttributeConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validatePackageFile(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportPackageAttributeConfig_DisplayName();
        }
    }
}
