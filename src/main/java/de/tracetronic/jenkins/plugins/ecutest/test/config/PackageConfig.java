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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class PackageConfig extends AbstractDescribableImpl<PackageConfig> implements Serializable,
    ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final boolean runTest;
    private final boolean runTraceAnalysis;
    private final List<PackageParameter> parameters;

    /**
     * Instantiates a new {@link PackageConfig}.
     *
     * @param runTest          specifies whether to run the test case
     * @param runTraceAnalysis specifies whether to run the trace analysis
     * @param parameters       the list of package parameters
     */
    @DataBoundConstructor
    public PackageConfig(final boolean runTest, final boolean runTraceAnalysis,
                         final List<PackageParameter> parameters) {
        super();
        this.runTest = runTest;
        this.runTraceAnalysis = runTraceAnalysis;
        this.parameters = parameters == null ? new ArrayList<>() : removeEmptyParameters(parameters);
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
     * @return the instance of a {@link PackageConfig}.
     */
    public static PackageConfig newInstance() {
        return new PackageConfig(true, true, null);
    }

    /**
     * @return {@code true} if run test case, {@code false} otherwise
     */
    public boolean isRunTest() {
        return runTest;
    }

    /**
     * @return {@code true} if run trace analysis, {@code false} otherwise
     */
    public boolean isRunTraceAnalysis() {
        return runTraceAnalysis;
    }

    /**
     * @return the parameters
     */
    public List<PackageParameter> getParameters() {
        return parameters;
    }

    @Override
    public PackageConfig expand(final EnvVars envVars) {
        final List<PackageParameter> parameters = new ArrayList<>();
        for (final PackageParameter param : getParameters()) {
            parameters.add(param.expand(envVars));
        }
        return new PackageConfig(isRunTest(), isRunTraceAnalysis(), parameters);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof PackageConfig) {
            final PackageConfig that = (PackageConfig) other;
            result = Objects.equals(parameters, that.parameters)
                && runTest == that.runTest && runTraceAnalysis == that.runTraceAnalysis;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(runTest).append(runTraceAnalysis).append(parameters)
            .toHashCode();
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
