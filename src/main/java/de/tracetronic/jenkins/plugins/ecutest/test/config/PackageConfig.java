/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class holding the package configuration.
 */
public class PackageConfig extends AbstractDescribableImpl<PackageConfig> implements Serializable,
    ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final boolean runTest;
    private final boolean runTraceAnalysis;
    private final List<PackageParameter> parameters;
    private final List<PackageOutputParameter> outputParameters;

    /**
     * Instantiates a new {@link PackageConfig}.
     *
     * @param runTest          specifies whether to run the test case
     * @param runTraceAnalysis specifies whether to run the trace analysis
     * @param parameters       the list of package parameters
     * @param outputParameters        the list of package parameters
     */
    @DataBoundConstructor
    public PackageConfig(final boolean runTest, final boolean runTraceAnalysis,
                         final List<PackageParameter> parameters, final List<PackageOutputParameter> outputParameters) {
        super();
        this.runTest = runTest;
        this.runTraceAnalysis = runTraceAnalysis;
        this.parameters = parameters == null ? new ArrayList<>() : removeEmptyParameters(parameters);
        this.outputParameters = outputParameters == null ? new ArrayList<>()
            : removeEmptyOutputParameters(outputParameters);
    }

    /**
     * Instantiates a new {@link PackageConfig} with empty package parameters.
     *
     * @param runTest          specifies whether to run the test case
     * @param runTraceAnalysis specifies whether to run the trace analysis
     */
    public PackageConfig(final boolean runTest, final boolean runTraceAnalysis) {
        super();
        this.runTest = runTest;
        this.runTraceAnalysis = runTraceAnalysis;
        parameters = new ArrayList<>();
        outputParameters = new ArrayList<>();
    }

    /**
     * Removes empty package parameters.
     *
     * @param parameters the parameters
     * @return the list of valid package parameters
     */
    private static List<PackageParameter> removeEmptyParameters(final List<PackageParameter> parameters) {
        final List<PackageParameter> validParameters = new ArrayList<>();
        for (final PackageParameter parameter : parameters) {
            if (StringUtils.isNotBlank(parameter.getName())) {
                validParameters.add(parameter);
            }
        }
        return validParameters;
    }

    /**
     * Removes empty package variables.
     *
     * @param outputParameters the variables
     * @return the list of valid package variables
     */
    private static List<PackageOutputParameter> removeEmptyOutputParameters(
            final List<PackageOutputParameter> outputParameters) {
        final List<PackageOutputParameter> validOutputParameters = new ArrayList<>();
        for (final PackageOutputParameter outputParameter : outputParameters) {
            if (StringUtils.isNotBlank(outputParameter.getName())) {
                validOutputParameters.add(outputParameter);
            }
        }
        return validOutputParameters;
    }

    /**
     * Instantiates a new {@link PackageConfig} with default values.
     *
     * @return the default {@link PackageConfig}
     */
    public static PackageConfig newInstance() {
        return new PackageConfig(true, true, null, null);
    }

    public boolean isRunTest() {
        return runTest;
    }

    public boolean isRunTraceAnalysis() {
        return runTraceAnalysis;
    }

    public List<PackageParameter> getParameters() {
        return parameters;
    }

    public List<PackageOutputParameter> getOutputParameters() {
        return outputParameters;
    }

    @Override
    public PackageConfig expand(final EnvVars envVars) {
        final List<PackageParameter> parameters = new ArrayList<>();
        for (final PackageParameter param : getParameters()) {
            parameters.add(param.expand(envVars));
        }
        final List<PackageOutputParameter> outputParameters = new ArrayList<>();
        for (final PackageOutputParameter outParam : getOutputParameters()) {
            outputParameters.add(outParam.expand(envVars));
        }
        return new PackageConfig(isRunTest(), isRunTraceAnalysis(), parameters, outputParameters);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof PackageConfig) {
            final PackageConfig that = (PackageConfig) other;
            result = Objects.equals(parameters, that.parameters) && Objects.equals(outputParameters,
                that.outputParameters) && runTest == that.runTest && runTraceAnalysis == that.runTraceAnalysis;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(runTest).
            append(runTraceAnalysis).append(parameters).append(outputParameters).toHashCode();
    }

    /**
     * DescriptorImpl for {@link PackageConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<PackageConfig> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Package Configuration";
        }
    }
}
