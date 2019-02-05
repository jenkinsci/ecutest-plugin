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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

/**
 * Class holding the configuration for importing a project from test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectConfig extends ImportConfig {

    private static final long serialVersionUID = 1L;

    /**
     * @since 1.17
     */
    private final boolean importMissingPackages;

    /**
     * Instantiates a new {@link ImportProjectConfig}.
     *
     * @param tmsPath               the project path in test management system
     * @param importPath            the import path
     * @param importMissingPackages specifies whether to import missing packages
     * @param credentialsId         the credentials id
     * @param timeout               the import timeout
     */
    @DataBoundConstructor
    public ImportProjectConfig(final String tmsPath, final String importPath,
                               final boolean importMissingPackages, final String credentialsId, final String timeout) {
        super(tmsPath, importPath, credentialsId, timeout);
        this.importMissingPackages = importMissingPackages;
    }

    /**
     * @return specifies whether to import missing packages
     */
    public boolean isImportMissingPackages() {
        return importMissingPackages;
    }

    @Override
    public ImportProjectConfig expand(final EnvVars envVars) {
        final String expTmsPath = envVars.expand(getTmsPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ImportProjectConfig(expTmsPath, expImportPath, isImportMissingPackages(),
            expCredentialsId, expTimeout);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ImportProjectConfig) {
            final ImportProjectConfig that = (ImportProjectConfig) other;
            result = that.canEqual(this) && super.equals(that) && importMissingPackages == that.importMissingPackages;
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ImportProjectConfig;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode()).append(importMissingPackages).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ImportProjectConfig}.
     */
    @Extension(ordinal = 3)
    public static class DescriptorImpl extends ImportConfig.DescriptorImpl {

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Override
        public FormValidation doCheckTmsPath(@QueryParameter final String value) {
            return tmsValidator.validateTestPath(value);
        }

        /**
         * Validates the timeout.
         *
         * @param value the timeout
         * @return the form validation
         */
        @Override
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return tmsValidator.validateTimeout(value, getDefaultTimeout());
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportProjectConfig_DisplayName();
        }
    }
}
