/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import hudson.EnvVars;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Class holding the configuration for importing a project directory from test management system.
 */
public class ImportProjectDirConfig extends ImportProjectConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportProjectDirConfig}.
     *
     * @param tmsPath       the project directory path in test management system
     * @param importPath    the import path
     * @param credentialsId the credentials id
     * @param timeout       the import timeout
     */
    @DataBoundConstructor
    public ImportProjectDirConfig(final String tmsPath, final String importPath, final String credentialsId,
                                  final String timeout) {
        super(tmsPath, importPath, false, credentialsId, timeout);
    }

    @Override
    public ImportProjectDirConfig expand(final EnvVars envVars) {
        final ImportProjectConfig config = super.expand(envVars);
        return new ImportProjectDirConfig(config.getTmsPath(), config.getImportPath(),
            config.getCredentialsId(), config.getTimeout());
    }

    /**
     * DescriptorImpl for {@link ImportProjectDirConfig}.
     */
    @Extension(ordinal = 2)
    public static class DescriptorImpl extends ImportProjectConfig.DescriptorImpl {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportProjectDirConfig_DisplayName();
        }
    }
}
