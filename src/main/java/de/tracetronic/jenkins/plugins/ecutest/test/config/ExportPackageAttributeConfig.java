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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportPackageAttributeConfig extends ExportAttributeConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ExportPackageAttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to export
     * @param credentialsId the credentials id
     * @param timeout       the export timeout
     */
    @DataBoundConstructor
    public ExportPackageAttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(filePath, credentialsId, timeout);
    }

    @Override
    public ExportPackageAttributeConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ExportPackageAttributeConfig(expFilePath, expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ExportPackageAttributeConfig}.
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends ExportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validatePackageFile(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ExportPackageAttributeConfig_DisplayName();
        }
    }
}
