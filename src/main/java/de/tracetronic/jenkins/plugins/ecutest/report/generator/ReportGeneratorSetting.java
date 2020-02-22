/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
 */
public class ReportGeneratorSetting extends AbstractDescribableImpl<ReportGeneratorSetting> implements
    ExpandableConfig, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    /**
     * Instantiates a new {@link ReportGeneratorSetting}.
     *
     * @param name  the name of the setting
     * @param value the value of the setting
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
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return reportValidator.validateSettingName(value);
        }

        /**
         * Validates the setting value.
         *
         * @param value the value
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
