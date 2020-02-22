/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import hudson.EnvVars;
import hudson.Extension;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Class holding the configuration for importing a project from an archive.
 */
public class ImportProjectArchiveConfig extends ImportConfig {

    private static final long serialVersionUID = 1L;

    private final String importConfigPath;
    private final boolean replaceFiles;

    /**
     * Instantiates a new {@link ImportProjectArchiveConfig}.
     *
     * @param tmsPath          the project path
     * @param importPath       the import path
     * @param importConfigPath the import config path
     * @param replaceFiles     the replace files
     */
    @DataBoundConstructor
    public ImportProjectArchiveConfig(final String tmsPath, final String importPath,
                                      final String importConfigPath, final boolean replaceFiles) {
        super(tmsPath, importPath, null, null);
        this.importConfigPath = StringUtils.trimToEmpty(importConfigPath);
        this.replaceFiles = replaceFiles;
    }

    /**
     * @return the import configuration path
     */
    public String getImportConfigPath() {
        return importConfigPath;
    }

    /**
     * @return {@code true} when existing files should be overwritten, {@code false} otherwise
     */
    public boolean isReplaceFiles() {
        return replaceFiles;
    }

    @Override
    public ImportProjectArchiveConfig expand(final EnvVars envVars) {
        final String expProjectPath = envVars.expand(getTmsPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expImportConfigPath = envVars.expand(getImportConfigPath());
        return new ImportProjectArchiveConfig(expProjectPath, expImportPath, expImportConfigPath, isReplaceFiles());
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ImportProjectArchiveConfig) {
            final ImportProjectArchiveConfig that = (ImportProjectArchiveConfig) other;
            result = that.canEqual(this)
                && super.equals(that)
                && Objects.equals(importConfigPath, that.importConfigPath)
                && replaceFiles == that.replaceFiles;
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ImportProjectArchiveConfig;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode())
            .append(importConfigPath).append(replaceFiles).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ImportProjectArchiveConfig}.
     */
    @Extension(ordinal = 4)
    public static class DescriptorImpl extends ImportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckTmsPath(@QueryParameter final String value) {
            return tmsValidator.validateArchivePath(value);
        }

        /**
         * Validates the import configuration target path.
         *
         * @param value the import configuration path
         * @return the form validation
         */
        public FormValidation doCheckImportConfigPath(@QueryParameter final String value) {
            return tmsValidator.validateImportConfigPath(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportProjectArchiveConfig_DisplayName();
        }
    }
}
