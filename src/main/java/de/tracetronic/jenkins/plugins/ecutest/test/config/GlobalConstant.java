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
 * Class holding a global constant.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class GlobalConstant extends AbstractDescribableImpl<GlobalConstant> implements Serializable,
    ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    /**
     * Instantiates a new {@link GlobalConstant}.
     *
     * @param name  the global constant name
     * @param value the global constant value
     */
    @DataBoundConstructor
    public GlobalConstant(final String name, final String value) {
        super();
        this.name = StringUtils.trimToEmpty(name);
        this.value = StringUtils.trimToEmpty(value);
    }

    /**
     * @return the global constant name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the global constant value
     */
    public String getValue() {
        return value;
    }

    @Override
    public GlobalConstant expand(final EnvVars envVars) {
        final String expandedName = envVars.expand(getName());
        final String expandedValue = envVars.expand(getValue());
        return new GlobalConstant(expandedName, expandedValue);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof GlobalConstant) {
            final GlobalConstant that = (GlobalConstant) other;
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
     * DescriptorImpl for {@link GlobalConstant}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<GlobalConstant> {

        private final TestValidator testValidator = new TestValidator();

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Global Constant";
        }

        /**
         * Validates the global constant name.
         *
         * @param value the value
         * @return FormValidation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return testValidator.validateGlobalConstantName(value);
        }

        /**
         * Validates the global constant value.
         *
         * @param value the value
         * @return FormValidation
         */
        public FormValidation doCheckValue(@QueryParameter final String value) {
            return testValidator.validateGlobalConstantValue(value);
        }
    }
}
