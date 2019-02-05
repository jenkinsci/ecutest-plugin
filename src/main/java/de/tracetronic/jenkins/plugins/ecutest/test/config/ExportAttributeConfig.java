/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

/**
 * Common base class for {@link ExportPackageAttributeConfig} and {@link ExportProjectAttributeConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ExportAttributeConfig extends AttributeConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ExportAttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to export
     * @param credentialsId the credentials id
     * @param timeout       the export timeout
     */
    public ExportAttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(filePath, credentialsId, timeout);
    }

    /**
     * DescriptorImpl for {@link ExportAttributeConfig}.
     */
    public abstract static class DescriptorImpl extends TMSConfig.DescriptorImpl {

        /**
         * Validates the file path to export.
         *
         * @param value the file path to export
         * @return the form validation
         */
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validatePackageFile(value);
        }

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
