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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExpandableConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ReportGeneratorValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Class holding the report generator settings.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorSetting extends AbstractDescribableImpl<ReportGeneratorSetting> implements
ExpandableConfig, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    /**
     * Instantiates a new {@link ReportGeneratorSetting}.
     *
     * @param name
     *            the name of the setting
     * @param value
     *            the value of the setting
     */
    @DataBoundConstructor
    public ReportGeneratorSetting(final String name, final String value) {
        this.name = StringUtils.trimToEmpty(name);
        this.value = StringUtils.trimToEmpty(value);
    }

    /**
     * @return the setting name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the setting value
     */
    public String getValue() {
        return value;
    }

    @Override
    public ReportGeneratorSetting expand(final EnvVars envVars) {
        final String expandedName = envVars.expand(getName());
        final String expandedValue = envVars.expand(getValue());
        return new ReportGeneratorSetting(expandedName, expandedValue);
    }

    /**
     * DescriptorImpl for {@link ReportGeneratorSetting}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ReportGeneratorSetting> {

        private final ReportGeneratorValidator reportValidator = new ReportGeneratorValidator();

        /**
         * Validates the setting name.
         *
         * @param value
         *            the value
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return reportValidator.validateSettingName(value);
        }

        /**
         * Validates the setting value.
         *
         * @param value
         *            the value
         * @return the form validation
         */
        public FormValidation doCheckValue(@QueryParameter final String value) {
            return reportValidator.validateSettingValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Report Generator Setting";
        }
    }
}
