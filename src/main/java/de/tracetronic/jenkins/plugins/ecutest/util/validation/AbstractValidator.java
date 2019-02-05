/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

/**
 * Common base class for the {@link TestValidator} and {@link ToolValidator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractValidator {

    /**
     * Character identifying parameterized values.
     */
    protected static final String PARAMETER = "$";

    /**
     * Validates the timeout.
     *
     * @param timeout        the timeout
     * @param defaultTimeout the default timeout
     * @return the form validation
     */
    public FormValidation validateTimeout(@QueryParameter final String timeout, final int defaultTimeout) {
        FormValidation returnValue;
        if (StringUtils.isBlank(timeout)) {
            returnValue = FormValidation.warning(Messages.Builder_NoTimeout(defaultTimeout));
        } else if (timeout.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else {
            returnValue = FormValidation.validateNonNegativeInteger(timeout);
            if (returnValue.equals(FormValidation.ok()) && Integer.parseInt(timeout) == 0) {
                returnValue = FormValidation.warning(Messages.Builder_DisabledTimeout());
            }
        }
        return returnValue;
    }

    /**
     * Validates required form value.
     *
     * @param value the form value
     * @return the form validation
     */
    protected FormValidation validateRequiredValue(final String value) {
        return FormValidation.validateRequired(value);
    }

    /**
     * Validates parameterized form value.
     *
     * @param value the form value
     * @return the form validation
     */
    protected FormValidation validateParameterizedValue(final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isEmpty(value) && value.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        }
        return returnValue;
    }

    /**
     * Validates required and parameterized form value.
     *
     * @param value the form value
     * @return the form validation
     */
    protected FormValidation validateRequiredParamValue(final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            returnValue = FormValidation.validateRequired(value);
        } else if (value.contains(PARAMETER)) {
            returnValue = validateParameterizedValue(value);
        }
        return returnValue;
    }

    /**
     * Validates the package file.
     *
     * @param testFile the test file
     * @return the form validation
     */
    public FormValidation validatePackageFile(final String testFile) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(testFile)) {
            returnValue = FormValidation.validateRequired(testFile);
        } else if (testFile.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!testFile.endsWith(".pkg")) {
            returnValue = FormValidation.error(Messages.TestBuilder_PkgFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the project file.
     *
     * @param testFile the test file
     * @return the form validation
     */
    public FormValidation validateProjectFile(final String testFile) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(testFile)) {
            returnValue = FormValidation.validateRequired(testFile);
        } else if (testFile.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!testFile.endsWith(".prj")) {
            returnValue = FormValidation.error(Messages.TestBuilder_PrjFileExtension());
        }
        return returnValue;
    }
}
