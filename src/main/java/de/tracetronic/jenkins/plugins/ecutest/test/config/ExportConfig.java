/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
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
 * Common base class for {@link ExportPackageConfig} and {@link ExportProjectConfig}.
 */
public abstract class ExportConfig extends TMSConfig {

    private static final long serialVersionUID = 1L;

    private final String filePath;
    private final String exportPath;
    private final boolean createNewPath;

    /**
     * Instantiates a new {@link ExportConfig}.
     *
     * @param filePath      the file path
     * @param exportPath    the export path
     * @param createNewPath specifies whether missing export path will be created
     * @param credentialsId the credentials id
     * @param timeout       the timeout
     */
    public ExportConfig(final String filePath, final String exportPath, final boolean createNewPath,
                        final String credentialsId, final String timeout) {
        super(credentialsId, timeout);
        this.filePath = StringUtils.trimToEmpty(filePath);
        this.exportPath = StringUtils.trimToEmpty(exportPath);
        this.createNewPath = createNewPath;
    }

    /**
     * @return the file path to export
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return the export target path
     */
    public String getExportPath() {
        return exportPath;
    }

    /**
     * @return specifies whether missing export path will be created
     */
    public boolean isCreateNewPath() {
        return createNewPath;
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ExportConfig) {
            final ExportConfig that = (ExportConfig) other;
            final String filePath = getFilePath();
            final String exportPath = getExportPath();
            final String thatFilePath = that.getFilePath();
            final String thatExportPath = that.getExportPath();
            result = Objects.equals(filePath, thatFilePath)
                && Objects.equals(exportPath, thatExportPath)
                && createNewPath == that.isCreateNewPath()
                && (getCredentialsId() == null ? that.getCredentialsId() == null :
                getCredentialsId().equals(that.getCredentialsId()))
                && (getTimeout() == null ? that.getTimeout() == null :
                getTimeout().equals(that.getTimeout()));
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(getFilePath()).append(getExportPath())
            .append(isCreateNewPath()).append(getCredentialsId()).append(getTimeout()).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ExportConfig}.
     */
    public abstract static class DescriptorImpl extends TMSConfig.DescriptorImpl {

        /**
         * Validates the file path to export.
         *
         * @param value the file path to export
         * @return the form validation
         */
        public abstract FormValidation doCheckFilePath(@QueryParameter String value);

        /**
         * Validates the export target path.
         *
         * @param value the export path
         * @return the form validation
         */
        public FormValidation doCheckExportPath(@QueryParameter final String value) {
            return tmsValidator.validateExportPath(value);
        }
    }
}
