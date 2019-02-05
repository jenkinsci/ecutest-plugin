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
 * Class holding the configuration for exporting a project to test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportProjectConfig extends ExportConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ExportProjectConfig}.
     *
     * @param filePath      the file path
     * @param exportPath    the export path
     * @param createNewPath specifies whether missing export path will be created
     * @param credentialsId the credentials id
     * @param timeout       the timeout
     */
    @DataBoundConstructor
    public ExportProjectConfig(final String filePath, final String exportPath, final boolean createNewPath,
                               final String credentialsId, final String timeout) {
        super(filePath, exportPath, createNewPath, credentialsId, timeout);
    }

    @Override
    public ExportProjectConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expExportPath = envVars.expand(getExportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ExportProjectConfig(expFilePath, expExportPath, isCreateNewPath(), expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ExportProjectConfig}.
     */
    @Extension(ordinal = 2)
    public static class DescriptorImpl extends ExportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validateProjectFile(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ExportProjectConfig_DisplayName();
        }
    }
}
