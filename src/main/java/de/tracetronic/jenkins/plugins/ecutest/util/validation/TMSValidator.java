/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

/**
 * Validator to check project exporter related form fields.
 */
public class TMSValidator extends AbstractValidator {

    /**
     * Validates the export target path.
     *
     * @param exportPath the export path
     * @return the form validation
     */
    public FormValidation validateExportPath(final String exportPath) {
        return validateRequiredParamValue(exportPath);
    }

    /**
     * Validates the test path to import.
     *
     * @param testPath the test path to import
     * @return the form validation
     */
    public FormValidation validateTestPath(final String testPath) {
        return validateRequiredParamValue(testPath);
    }

    /**
     * Validates the project archive path to import.
     *
     * @param archivePath the project archive to import
     * @return the form validation
     */
    public FormValidation validateArchivePath(final String archivePath) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(archivePath)) {
            returnValue = FormValidation.validateRequired(archivePath);
        } else if (archivePath.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!archivePath.endsWith(".prz")) {
            returnValue = FormValidation.error(Messages.ImportProjectBuilder_PrzFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the import target path.
     *
     * @param importPath the import path
     * @return the form validation
     */
    public FormValidation validateImportPath(final String importPath) {
        return validateParameterizedValue(importPath);
    }

    /**
     * Validates the import configuration target path.
     *
     * @param importConfigPath the import configuration path
     * @return the form validation
     */
    public FormValidation validateImportConfigPath(final String importConfigPath) {
        return validateParameterizedValue(importConfigPath);
    }
}
