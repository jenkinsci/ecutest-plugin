/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
import hudson.util.FormValidation;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;

/**
 * Class holding the test execution settings.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExecutionConfig extends AbstractDescribableImpl<ExecutionConfig> implements Serializable,
ExpandableConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Defines the default timeout running a test.
     */
    protected static final int DEFAULT_TIMEOUT = 3600;

    private final String timeout;
    private final boolean stopOnError;
    /**
     * @since 1.4
     */
    private final boolean checkTestFile;

    /**
     * Instantiates a new {@link ExecutionConfig}.
     *
     * @param timeout
     *            the timeout to run the test
     * @param stopOnError
     *            specifies whether to stop ECU-TEST and
     *            Tool-Server instances if an error occurred
     * @param checkTestFile
     *            specifies whether to check the test file
     */
    @DataBoundConstructor
    public ExecutionConfig(final String timeout, final boolean stopOnError, final boolean checkTestFile) {
        super();
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(DEFAULT_TIMEOUT));
        this.stopOnError = stopOnError;
        this.checkTestFile = checkTestFile;
    }

    /**
     * Instantiates a new {@link ExecutionConfig}.
     *
     * @param timeout
     *            the timeout to run the test
     * @param stopOnError
     *            specifies whether to stop ECU-TEST and
     *            Tool-Server instances if an error occurred
     * @param checkTestFile
     *            specifies whether to check the test file
     */
    public ExecutionConfig(final int timeout, final boolean stopOnError, final boolean checkTestFile) {
        this(String.valueOf(timeout), stopOnError, checkTestFile);
    }

    /**
     * Instantiates a new {@link ExecutionConfig}.
     *
     * @param timeout
     *            the timeout to run the test
     * @param stopOnError
     *            specifies whether to stop ECU-TEST and
     *            Tool-Server instances if an error occurred
     * @deprecated since 1.4, use {@link #ExecutionConfig(String, boolean, boolean)}
     */
    @Deprecated
    public ExecutionConfig(final String timeout, final boolean stopOnError) {
        this(timeout, stopOnError, true);
    }

    /**
     * Instantiates a new {@link ExecutionConfig}.
     *
     * @param timeout
     *            the timeout to run the test
     * @param stopOnError
     *            specifies whether to stop ECU-TEST and
     *            Tool-Server instances if an error occurred
     * @deprecated since 1.4, use {@link #ExecutionConfig(int, boolean, boolean)}
     */
    @Deprecated
    public ExecutionConfig(final int timeout, final boolean stopOnError) {
        this(timeout, stopOnError, true);
    }

    /**
     * Parses a string-based parameter to integer.
     *
     * @param param
     *            the parameter string
     * @return the parsed integer value represented by the String parameter,
     *         defaults to {@link #DEFAULT_TIMEOUT} if null or invalid value
     */
    public static int parse(final String param) {
        try {
            return Integer.parseInt(param);
        } catch (final NumberFormatException e) {
            return DEFAULT_TIMEOUT;
        }
    }

    /**
     * @return the timeout as integer
     */
    public int getTimeout() {
        return parse(getStringTimeout());
    }

    /**
     * @return the timeout as string
     */
    public String getStringTimeout() {
        return timeout;
    }

    /**
     * @return the default timeout
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return {@code true} to stop ECU-TEST and Tool-Server instances
     *         if an error occurred, {@code false} otherwise
     */
    public boolean isStopOnError() {
        return stopOnError;
    }

    /**
     * @return specifies whether to check the test file
     */
    public boolean isCheckTestFile() {
        return checkTestFile;
    }

    @Override
    public ExecutionConfig expand(final EnvVars envVars) {
        final String expTimeout = EnvUtil.expandEnvVar(getStringTimeout(), envVars,
                String.valueOf(DEFAULT_TIMEOUT));
        return new ExecutionConfig(expTimeout, isStopOnError(), isCheckTestFile());
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ExecutionConfig) {
            final ExecutionConfig that = (ExecutionConfig) other;
            result = (timeout == null ? that.timeout == null : timeout.equals(that.timeout))
                    && stopOnError == that.stopOnError && checkTestFile == that.checkTestFile;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(timeout).append(stopOnError).append(checkTestFile)
                .toHashCode();
    }

    /**
     * @return the instance of a {@link ExecutionConfig}.
     */
    public static ExecutionConfig newInstance() {
        return new ExecutionConfig(null, true, true);
    }

    /**
     * DescriptorImpl for {@link ExecutionConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ExecutionConfig> {

        private final TestValidator testValidator = new TestValidator();

        @Override
        public String getDisplayName() {
            return "Execution Configuration";
        }

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * Validates the timeout.
         *
         * @param value
         *            the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return testValidator.validateTimeout(value, getDefaultTimeout());
        }
    }
}
