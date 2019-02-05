/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

/**
 * Common base class for {@link ImportPackageAttributeConfig} and {@link ImportProjectAttributeConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ImportAttributeConfig extends AttributeConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportAttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to import
     * @param credentialsId the credentials id
     * @param timeout       the import timeout
     */
    public ImportAttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(filePath, credentialsId, timeout);
    }

    /**
     * DescriptorImpl for {@link ImportAttributeConfig}.
     */
    public abstract static class DescriptorImpl extends TMSConfig.DescriptorImpl {

        /**
         * Validates the file path to import.
         *
         * @param value the file path to import
         * @return the form validation
         */
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return tmsValidator.validatePackageFile(value);
        }

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
