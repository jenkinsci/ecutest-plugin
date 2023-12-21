/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.QueryParameter;

import java.util.Objects;

/**
 * Common base class for {@link ImportPackageConfig} and {@link ImportProjectConfig}.
 */
public abstract class ImportConfig extends TMSConfig {

    private static final long serialVersionUID = 1L;

    private final String tmsPath;
    private final String importPath;

    /**
     * Instantiates a new {@link ImportConfig}.
     *
     * @param tmsPath       the path in the test management system
     * @param importPath    the import target path
     * @param credentialsId the credentials id
     * @param timeout       the timeout
     */
    public ImportConfig(final String tmsPath, final String importPath,
                        final String credentialsId, final String timeout) {
        super(credentialsId, timeout);
        this.tmsPath = StringUtils.trimToEmpty(tmsPath);
        this.importPath = StringUtils.trimToEmpty(importPath);
    }

    public String getTmsPath() {
        return tmsPath;
    }

    public String getImportPath() {
        return importPath;
    }

    @Override
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public boolean equals(final Object other) {
        boolean result = false;
        if (canEqual(other) && other instanceof ImportConfig) {
            final ImportConfig that = (ImportConfig) other;
            final String tmsPath = getTmsPath();
            final String importPath = getImportPath();
            final String thatFilePath = that.getTmsPath();
            final String thatImportPath = that.getImportPath();
            result = that.canEqual(this)
                    && Objects.equals(tmsPath, thatFilePath)
                    && Objects.equals(importPath, thatImportPath)
                    && (getCredentialsId() == null ? that.getCredentialsId() == null
                    : getCredentialsId().equals(that.getCredentialsId()))
                    && (getTimeout() == null ? that.getTimeout() == null
                    : getTimeout().equals(that.getTimeout()));
        }
        return result;
    }

    /**
     * Implementation according to <a href="www.artima.com/lejava/articles/equality.html">Equality Pitfall #4</a>.
     *
     * @param other the other object
     * @return {@code true} if the other object is an instance of the class in which canEqual is (re)defined,
     * {@code false} otherwise.
     */
    public boolean canEqual(final Object other) {
        return other instanceof ImportConfig;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(getTmsPath()).append(getImportPath()).append(getCredentialsId())
            .append(getTimeout()).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ImportConfig}.
     */
    public abstract static class DescriptorImpl extends TMSConfig.DescriptorImpl {

        /**
         * Validates the file path in the test management system.
         *
         * @param value the file path
         * @return the form validation
         */
        public abstract FormValidation doCheckTmsPath(@QueryParameter String value);

        /**
         * Validates the import target path.
         *
         * @param value the import path
         * @return the form validation
         */
        public FormValidation doCheckImportPath(@QueryParameter final String value) {
            return tmsValidator.validateImportPath(value);
        }
    }
}
