/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
