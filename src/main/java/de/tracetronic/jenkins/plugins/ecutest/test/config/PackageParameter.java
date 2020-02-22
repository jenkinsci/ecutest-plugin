/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class holding a package parameter.
 */
public class PackageParameter extends AbstractDescribableImpl<PackageParameter> implements Serializable,
    ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    /**
     * Instantiates a new {@link PackageParameter}.
     *
     * @param name  the parameter name
     * @param value the parameter value
     */
    @DataBoundConstructor
    public PackageParameter(final String name, final String value) {
        super();
        this.name = StringUtils.trimToEmpty(name);
        this.value = StringUtils.trimToEmpty(value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public PackageParameter expand(final EnvVars envVars) {
        final String expandedName = envVars.expand(getName());
        final String expandedValue = envVars.expand(getValue());
        return new PackageParameter(expandedName, expandedValue);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof PackageParameter) {
            final PackageParameter that = (PackageParameter) other;
            result = Objects.equals(name, that.name)
                && Objects.equals(value, that.value);
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).append(value).toHashCode();
    }

    /**
     * DescriptorImpl for {@link PackageParameter}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<PackageParameter> {

        private final TestValidator testValidator = new TestValidator();

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Package Parameter";
        }

        /**
         * Validates the parameter name.
         *
         * @param value the value
         * @return FormValidation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return testValidator.validateParameterName(value);
        }

        /**
         * Validates the parameter value.
         *
         * @param value the value
         * @return FormValidation
         */
        public FormValidation doCheckValue(@QueryParameter final String value) {
            return testValidator.validateParameterValue(value);
        }
    }
}
