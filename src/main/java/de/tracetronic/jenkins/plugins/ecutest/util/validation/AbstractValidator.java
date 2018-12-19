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
