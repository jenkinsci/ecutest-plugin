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
 * Class holding the configuration for importing a package from test management system.
 */
public class ImportPackageConfig extends ImportConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportPackageConfig}.
     *
     * @param tmsPath       the package path in test management system
     * @param importPath    the import path
     * @param credentialsId the credentials id
     * @param timeout       the import timeout
     */
    @DataBoundConstructor
    public ImportPackageConfig(final String tmsPath, final String importPath,
                               final String credentialsId, final String timeout) {
        super(tmsPath, importPath, credentialsId, timeout);
    }

    @Override
    public ImportPackageConfig expand(final EnvVars envVars) {
        final String expTmsPath = envVars.expand(getTmsPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ImportPackageConfig(expTmsPath, expImportPath, expCredentialsId, expTimeout);
    }

    @Override
    public final boolean equals(final Object other) {
        return super.equals(other);
    }

    @Override
    public boolean canEqual(final Object other) {
        return other instanceof ImportPackageConfig;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * DescriptorImpl for {@link ImportPackageConfig}.
     */
    @Extension(ordinal = 3)
    public static class DescriptorImpl extends ImportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckTmsPath(@QueryParameter final String value) {
            return tmsValidator.validateTestPath(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportPackageConfig_DisplayName();
        }
    }
}
