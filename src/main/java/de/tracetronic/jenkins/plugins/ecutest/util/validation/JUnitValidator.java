/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.report.junit.Messages;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

/**
 * Validator to check UNIT related form fields.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitValidator extends AbstractValidator {

    /**
     * Validates the unstable threshold.
     *
     * @param value the threshold
     * @return the form validation
     */
    public FormValidation validateUnstableThreshold(final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            returnValue = FormValidation.warning(Messages.JUnitPublisher_NoUnstableThreshold());
        } else if (value.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.JUnitPublisher_NoValidatedValue());
        } else if (!isPercentageValue(value)) {
            returnValue = FormValidation.error(Messages.JUnitPublisher_InvalidPercentage());
        }
        return returnValue;
    }

    /**
     * Validates the failed threshold.
     *
     * @param value the threshold
     * @return the form validation
     */
    public FormValidation validateFailedThreshold(final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            returnValue = FormValidation.warning(Messages.JUnitPublisher_NoFailedThreshold());
        } else if (value.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.JUnitPublisher_NoValidatedValue());
        } else if (!isPercentageValue(value)) {
            returnValue = FormValidation.error(Messages.JUnitPublisher_InvalidPercentage());
        }
        return returnValue;
    }

    /**
     * Checks for percentage value.
     *
     * @param value the value
     * @return {@code true} if is percentage value
     */
    private boolean isPercentageValue(final String value) {
        try {
            final double doubleValue = Double.parseDouble(value);
            return doubleValue <= 100 && doubleValue >= 0;
        } catch (final NumberFormatException e) {
            return false;
        }
    }
}
