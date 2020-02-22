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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class holding the test configurations (e.g. TBC, TCF).
 */
public class TestConfig extends AbstractDescribableImpl<TestConfig> implements Serializable, ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String tbcFile;
    private final String tcfFile;
    /**
     * Specifies whether to reload the configuration.
     *
     * @since 1.4
     */
    private final boolean forceReload;
    /**
     * Specifies whether to load the configuration only.
     *
     * @since 1.6
     */
    private final boolean loadOnly;
    /**
     * Specifies whether to keep the previously loaded configuration.
     *
     * @since 1.17
     */
    private final boolean keepConfig;
    private final List<GlobalConstant> constants;

    /**
     * Instantiates a new {@link TestConfig}.
     *
     * @param tbcFile     the test bench configuration file
     * @param tcfFile     the test configuration file
     * @param forceReload specifies whether to reload the configuration
     * @param loadOnly    specifies whether to load the configuration only
     * @param keepConfig  specifies whether to keep the previously loaded configuration
     * @param constants   the list of global constants
     */
    @DataBoundConstructor
    public TestConfig(final String tbcFile, final String tcfFile, final boolean forceReload, final boolean loadOnly,
                      final boolean keepConfig, final List<GlobalConstant> constants) {
        super();
        this.tbcFile = StringUtils.trimToEmpty(tbcFile);
        this.tcfFile = StringUtils.trimToEmpty(tcfFile);
        this.forceReload = forceReload;
        this.loadOnly = loadOnly;
        this.keepConfig = keepConfig;
        this.constants = constants == null ? new ArrayList<>() : removeEmptyConstants(constants);
    }

    /**
     * Instantiates a new {@link TestConfig} with empty global constants.
     *
     * @param tbcFile the test bench configuration file
     * @param tcfFile the test configuration file
     */
    public TestConfig(final String tbcFile, final String tcfFile) {
        this(tbcFile, tcfFile, false, false, false, null);
    }

    /**
     * Instantiates a new {@link TestConfig} with empty global constants.
     *
     * @param tbcFile     the test bench configuration file
     * @param tcfFile     the test configuration file
     * @param forceReload specifies whether to reload the configuration
     * @param loadOnly    specifies whether to load the configuration only
     */
    public TestConfig(final String tbcFile, final String tcfFile, final boolean forceReload, final boolean loadOnly) {
        this(tbcFile, tcfFile, forceReload, loadOnly, false, null);
    }

    /**
     * Removes empty global constants.
     *
     * @param constants the constants
     * @return the list of valid global constants
     */
    private static List<GlobalConstant> removeEmptyConstants(final List<GlobalConstant> constants) {
        final List<GlobalConstant> validConstants = new ArrayList<>();
        for (final GlobalConstant constant : constants) {
            if (StringUtils.isNotBlank(constant.getName())) {
                validConstants.add(constant);
            }
        }
        return validConstants;
    }

    /**
     * Instantiates a new {@link TestConfig} with default values.
     *
     * @return the default {@link TestConfig}
     */
    public static TestConfig newInstance() {
        return new TestConfig(null, null, false, false, false, null);
    }

    public String getTbcFile() {
        return tbcFile;
    }

    public String getTcfFile() {
        return tcfFile;
    }

    public boolean isForceReload() {
        return forceReload;
    }

    public boolean isLoadOnly() {
        return loadOnly;
    }

    public boolean isKeepConfig() {
        return keepConfig;
    }

    public List<GlobalConstant> getConstants() {
        return constants;
    }

    @Override
    public TestConfig expand(final EnvVars envVars) {
        final String expTbcFile = envVars.expand(getTbcFile());
        final String expTcfFile = envVars.expand(getTcfFile());
        final List<GlobalConstant> constants = new ArrayList<>();
        for (final GlobalConstant constant : getConstants()) {
            constants.add(constant.expand(envVars));
        }
        return new TestConfig(expTbcFile, expTcfFile, isForceReload(), isLoadOnly(), isKeepConfig(), constants);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof TestConfig) {
            final TestConfig that = (TestConfig) other;
            result = Objects.equals(tbcFile, that.tbcFile)
                && Objects.equals(tcfFile, that.tcfFile)
                && Objects.equals(constants, that.constants)
                && forceReload == that.forceReload && loadOnly == that.loadOnly && keepConfig == that.keepConfig;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(tbcFile).append(tcfFile).append(forceReload).append(loadOnly)
            .append(keepConfig).append(constants).toHashCode();
    }

    /**
     * DescriptorImpl for {@link TestConfig}.
     *
     * @param <T>
     */
    @Extension
    public static class DescriptorImpl<T> extends Descriptor<TestConfig> {

        private final TestValidator testValidator = new TestValidator();

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Test Configuration";
        }

        /**
         * Validates the TBC file.
         *
         * @param value the TBC file
         * @return the form validation
         */
        public FormValidation doCheckTbcFile(@QueryParameter final String value) {
            return testValidator.validateTbcFile(value);
        }

        /**
         * Validates the TCF file.
         *
         * @param value the TCF file
         * @return the form validation
         */
        public FormValidation doCheckTcfFile(@QueryParameter final String value) {
            return testValidator.validateTcfFile(value);
        }
    }
}
