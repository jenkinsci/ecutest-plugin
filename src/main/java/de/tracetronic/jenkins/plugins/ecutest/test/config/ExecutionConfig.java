/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
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
 * Class holding the test execution settings.
 */
public class ExecutionConfig extends AbstractDescribableImpl<ExecutionConfig>
    implements Serializable, ExpandableConfig {

    /**
     * Defines the default timeout running a test.
     */
    protected static final int DEFAULT_TIMEOUT = 3600;

    private static final long serialVersionUID = 1L;

    private final String timeout;
    private final boolean stopOnError;
    /**
     * Specifies whether to check the test file.
     *
     * @since 1.4
     */
    private final boolean checkTestFile;

    /**
     * Specifies whether to record returned test file checks as issues of
     * <a href="https://github.com/jenkinsci/warnings-ng-plugin">Warnings Next Generation</a>
     * plugin, therefore {@link #checkTestFile} must be enabled.
     *
     * @since 2.19
     */
    private final boolean recordWarnings;

    /**
     * Instantiates a new {@link ExecutionConfig}.
     *
     * @param timeout       the timeout to run the test
     * @param stopOnError   specifies whether to stop ecu.test and Tool-Server instances if an error occurred
     * @param checkTestFile specifies whether to check the test file
     * @param recordWarnings specifies whether to record returned test file checks as Warnings NG issues
     */
    @DataBoundConstructor
    public ExecutionConfig(final String timeout, final boolean stopOnError, final boolean checkTestFile,
                           final boolean recordWarnings) {
        super();
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(DEFAULT_TIMEOUT));
        this.stopOnError = stopOnError;
        this.checkTestFile = checkTestFile;
        this.recordWarnings = recordWarnings;
    }

    /**
     * Instantiates a new {@link ExecutionConfig}.
     *
     * @param timeout       the timeout to run the test
     * @param stopOnError   specifies whether to stop ecu.test and Tool-Server instances if an error occurred
     * @param checkTestFile specifies whether to check the test file
     * @param recordWarnings specifies whether to record returned test file checks as Warnings NG issues
     */
    public ExecutionConfig(final int timeout, final boolean stopOnError, final boolean checkTestFile,
                           final boolean recordWarnings) {
        this(String.valueOf(timeout), stopOnError, checkTestFile, recordWarnings);
    }

    /**
     * Parses a string-based parameter to integer.
     *
     * @param param the parameter string
     * @return the parsed integer value represented by the String parameter, defaults to {@link #DEFAULT_TIMEOUT} if
     * null or invalid value
     */
    public static int parse(final String param) {
        try {
            return Integer.parseInt(param);
        } catch (final NumberFormatException e) {
            return DEFAULT_TIMEOUT;
        }
    }

    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * Instantiates a new {@link ExecutionConfig} with default values.
     *
     * @return the default {@link ExecutionConfig}
     */
    public static ExecutionConfig newInstance() {
        return new ExecutionConfig(null, true, true, false);
    }

    public int getParsedTimeout() {
        return parse(getTimeout());
    }

    public String getTimeout() {
        return timeout;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public boolean isCheckTestFile() {
        return checkTestFile;
    }

    public boolean isRecordWarnings() {
        return recordWarnings;
    }

    @Override
    public ExecutionConfig expand(final EnvVars envVars) {
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars,
                String.valueOf(DEFAULT_TIMEOUT));
        return new ExecutionConfig(expTimeout, isStopOnError(), isCheckTestFile(), recordWarnings);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ExecutionConfig) {
            final ExecutionConfig that = (ExecutionConfig) other;
            result = Objects.equals(timeout, that.timeout)
                    && stopOnError == that.stopOnError
                    && checkTestFile == that.checkTestFile
                    && recordWarnings == that.recordWarnings;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(timeout).append(stopOnError).append(checkTestFile)
                .append(recordWarnings).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ExecutionConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ExecutionConfig> {

        private final TestValidator testValidator = new TestValidator();

        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Execution Configuration";
        }

        /**
         * Validates the timeout.
         *
         * @param value the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return testValidator.validateTimeout(value, getDefaultTimeout());
        }
    }
}
