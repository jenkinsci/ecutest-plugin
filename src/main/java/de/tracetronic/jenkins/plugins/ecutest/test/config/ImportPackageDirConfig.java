/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
public class ImportPackageDirConfig extends ImportPackageConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportPackageDirConfig}.
     *
     * @param tmsPath       the package directory path in test management system
     * @param importPath    the import path
     * @param credentialsId the credentials id
     * @param timeout       the import timeout
     */
    @DataBoundConstructor
    public ImportPackageDirConfig(final String tmsPath, final String importPath,
                                  final String credentialsId, final String timeout) {
        super(tmsPath, importPath, credentialsId, timeout);
    }

    @Override
    public ImportPackageDirConfig expand(final EnvVars envVars) {
        final ImportPackageConfig config = super.expand(envVars);
        return new ImportPackageDirConfig(config.getTmsPath(), config.getImportPath(),
            config.getCredentialsId(), config.getTimeout());
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ImportPackageDirConfig;
    }

    /**
     * DescriptorImpl for {@link ImportPackageDirConfig}.
     */
    @Extension(ordinal = 2)
    public static class DescriptorImpl extends ImportPackageConfig.DescriptorImpl {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportPackageDirConfig_DisplayName();
        }
    }
}
