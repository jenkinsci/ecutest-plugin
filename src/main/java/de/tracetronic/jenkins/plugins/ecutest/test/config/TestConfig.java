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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;

/**
 * Class holding the test configurations (e.g. TBC, TCF).
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestConfig extends AbstractDescribableImpl<TestConfig> implements Serializable, ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String tbcFile;
    private final String tcfFile;
    /**
     * @since 1.4
     */
    private final boolean forceReload;
    /**
     * @since 1.6
     */
    private final boolean loadOnly;
    private final List<GlobalConstant> constants;

    /**
     * Instantiates a new {@link TestConfig}.
     *
     * @param tbcFile
     *            the test bench configuration file
     * @param tcfFile
     *            the test configuration file
     * @param forceReload
     *            specifies whether to reload the configuration
     * @param loadOnly
     *            specifies whether to load the configuration only
     * @param constants
     *            the list of global constants
     */
    @DataBoundConstructor
    public TestConfig(final String tbcFile, final String tcfFile, final boolean forceReload, final boolean loadOnly,
            final List<GlobalConstant> constants) {
        super();
        this.tbcFile = StringUtils.trimToEmpty(tbcFile);
        this.tcfFile = StringUtils.trimToEmpty(tcfFile);
        this.forceReload = forceReload;
        this.loadOnly = loadOnly;
        this.constants = constants == null ? new ArrayList<GlobalConstant>() : removeEmptyConstants(constants);
    }

    /**
     * Instantiates a new {@link TestConfig} with empty global constants.
     *
     * @param tbcFile
     *            the test bench configuration file
     * @param tcfFile
     *            the test configuration file
     */
    public TestConfig(final String tbcFile, final String tcfFile) {
        this(tbcFile, tcfFile, false, false, null);
    }

    /**
     * Instantiates a new {@link TestConfig} with empty global constants.
     *
     * @param tbcFile
     *            the test bench configuration file
     * @param tcfFile
     *            the test configuration file
     * @param forceReload
     *            specifies whether to reload the configuration
     * @param loadOnly
     *            specifies whether to load the configuration only
     */
    public TestConfig(final String tbcFile, final String tcfFile, final boolean forceReload, final boolean loadOnly) {
        this(tbcFile, tcfFile, forceReload, loadOnly, null);
    }

    /**
     * Instantiates a new {@link TestConfig}.
     *
     * @param tbcFile
     *            the test bench configuration file
     * @param tcfFile
     *            the test configuration file
     * @param constants
     *            the list of global constants
     * @deprecated since 1.4, use {@link #TestConfig(String, String, boolean, boolean, List)}
     */
    @Deprecated
    public TestConfig(final String tbcFile, final String tcfFile, final List<GlobalConstant> constants) {
        this(tbcFile, tcfFile, false, false, constants);
    }

    /**
     * @return the TBC file path
     */
    public String getTbcFile() {
        return tbcFile;
    }

    /**
     * @return the TCF file path
     */
    public String getTcfFile() {
        return tcfFile;
    }

    /**
     * @return specifies whether to reload the configuration
     */
    public boolean isForceReload() {
        return forceReload;
    }

    /**
     * @return specifies whether to load the configuration only
     */
    public boolean isLoadOnly() {
        return loadOnly;
    }

    /**
     * @return the global constants
     */
    public List<GlobalConstant> getConstants() {
        return constants;
    }

    /**
     * Removes empty global constants.
     *
     * @param constants
     *            the constants
     * @return the list of valid global constants
     */
    private static List<GlobalConstant> removeEmptyConstants(final List<GlobalConstant> constants) {
        final List<GlobalConstant> validConstants = new ArrayList<GlobalConstant>();
        for (final GlobalConstant constant : constants) {
            if (StringUtils.isNotBlank(constant.getName())) {
                validConstants.add(constant);
            }
        }
        return validConstants;
    }

    @Override
    public TestConfig expand(final EnvVars envVars) {
        final String expTbcFile = envVars.expand(getTbcFile());
        final String expTcfFile = envVars.expand(getTcfFile());
        final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();
        for (final GlobalConstant constant : getConstants()) {
            constants.add(constant.expand(envVars));
        }
        return new TestConfig(expTbcFile, expTcfFile, isForceReload(), isLoadOnly(), constants);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof TestConfig) {
            final TestConfig that = (TestConfig) other;
            result = (tbcFile == null ? that.tbcFile == null : tbcFile.equals(that.tbcFile))
                    && (tcfFile == null ? that.tcfFile == null : tcfFile.equals(that.tcfFile))
                    && (constants == null ? that.constants == null : constants.equals(that.constants))
                    && forceReload == that.forceReload && loadOnly == that.loadOnly;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(tbcFile).append(tcfFile).append(forceReload).append(loadOnly)
                .append(constants).toHashCode();
    }

    /**
     * @return the instance of a {@link TestConfig}.
     */
    public static TestConfig newInstance() {
        return new TestConfig(null, null, false, false, null);
    }

    /**
     * DescriptorImpl for {@link TestConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<TestConfig> {

        private final TestValidator testValidator = new TestValidator();

        @Override
        public String getDisplayName() {
            return "Test Configuration";
        }

        /**
         * Validates the TBC file.
         *
         * @param value
         *            the TBC file
         * @return the form validation
         */
        public FormValidation doCheckTbcFile(@QueryParameter final String value) {
            return testValidator.validateTbcFile(value);
        }

        /**
         * Validates the TCF file.
         *
         * @param value
         *            the TCF file
         * @return the form validation
         */
        public FormValidation doCheckTcfFile(@QueryParameter final String value) {
            return testValidator.validateTcfFile(value);
        }
    }
}
