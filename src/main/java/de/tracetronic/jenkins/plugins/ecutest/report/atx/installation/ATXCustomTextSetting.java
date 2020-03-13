/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.Extension;
import hudson.util.FormValidation;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Class holding the information of an additional text ATX setting.
 */
public class ATXCustomTextSetting extends ATXCustomSetting {

    private static final long serialVersionUID = 1L;

    private final String value;

    /**
     * Instantiates a new {@link ATXCustomTextSetting}.
     *
     * @param name  the name of the setting
     * @param value the value of the setting
     */
    @DataBoundConstructor
    public ATXCustomTextSetting(final String name, final String value) {
        super(name);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ATXCustomTextSetting) {
            final ATXCustomTextSetting that = (ATXCustomTextSetting) other;
            result = that.canEqual(this) && super.equals(that)
                && Objects.equals(value, that.value);
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ATXCustomTextSetting;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode()).append(value).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ATXTextSetting}.
     */
    @Symbol("atxCustomTextSetting")
    @Extension
    public static class DescriptorImpl extends ATXCustomSetting.DescriptorImpl {

        /**
         * Validates the setting value.
         *
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckValue(@QueryParameter final String value) {
            return atxValidator.validateCustomSettingValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXTextSetting_DisplayName();
        }
    }
}
