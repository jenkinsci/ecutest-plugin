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
 * Class holding the configuration for exporting a package to test management system.
 */
public class ExportPackageConfig extends ExportConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ExportPackageConfig}.
     *
     * @param filePath      the file path
     * @param exportPath    the export path
     * @param createNewPath specifies whether missing export path will be created
     * @param credentialsId the credentials id
     * @param timeout       the timeout
     */
    @DataBoundConstructor
    public ExportPackageConfig(final String filePath, final String exportPath, final boolean createNewPath,
                               final String credentialsId, final String timeout) {
        super(filePath, exportPath, createNewPath, credentialsId, timeout);
    }

    @Override
    public ExportPackageConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expExportPath = envVars.expand(getExportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ExportPackageConfig(expFilePath, expExportPath, isCreateNewPath(), expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ExportPackageConfig}.
     */
    @Extension(ordinal = 2)
    public static class DescriptorImpl extends ExportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validatePackageFile(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ExportPackageConfig_DisplayName();
        }
    }
}
