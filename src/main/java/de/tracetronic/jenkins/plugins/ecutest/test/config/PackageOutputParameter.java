/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
 * Class holding a package output parameter name.
 */
public class PackageOutputParameter extends AbstractDescribableImpl<PackageOutputParameter> implements Serializable,
    ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String name;

    /**
     * Instantiates a new {@link PackageOutputParameter}.
     *
     * @param name  the variable name
     */
    @DataBoundConstructor
    public PackageOutputParameter(final String name) {
        super();
        this.name = StringUtils.trimToEmpty(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public PackageOutputParameter expand(final EnvVars envVars) {
        final String expandedVariable = envVars.expand(getName());
        return new PackageOutputParameter(expandedVariable);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof PackageOutputParameter) {
            final PackageOutputParameter that = (PackageOutputParameter) other;
            result = Objects.equals(name, that.name);
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).toHashCode();
    }

    /**
     * DescriptorImpl for {@link PackageOutputParameter}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<PackageOutputParameter> {

        private final TestValidator testValidator = new TestValidator();

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Package Output Parameter Name";
        }

        /**
         * Validates the variable names.
         *
         * @param value the value
         * @return FormValidation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return testValidator.validatePackageOutputParameterName(value);
        }
    }
}
