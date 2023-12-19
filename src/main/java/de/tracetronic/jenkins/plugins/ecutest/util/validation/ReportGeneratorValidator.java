/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

/**
 * Validator to check report generator related form fields.
 */
public class ReportGeneratorValidator extends AbstractValidator {

    /**
     * Validates the generator name.
     *
     * @param name the generator name
     * @return the form validation
     */
    public FormValidation validateGeneratorName(final String name) {
        final FormValidation returnValue;
        if (StringUtils.isBlank(name)) {
            returnValue = FormValidation.validateRequired(name);
        } else {
            returnValue = validateParameterizedValue(name);
        }
        return returnValue;
    }

    /**
     * Validates the setting name.
     *
     * @param name the setting name
     * @return the form validation
     */
    public FormValidation validateSettingName(final String name) {
        final FormValidation returnValue;
        if (StringUtils.isBlank(name)) {
            returnValue = FormValidation.validateRequired(name);
        } else {
            returnValue = validateParameterizedValue(name);
        }
        return returnValue;
    }

    /**
     * Validates the setting value.
     *
     * @param value the setting value
     * @return the form validation
     */
    public FormValidation validateSettingValue(final String value) {
        return validateParameterizedValue(value);
    }
}
