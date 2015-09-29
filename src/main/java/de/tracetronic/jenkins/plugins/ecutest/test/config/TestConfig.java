/**
 * Copyright (c) 2015 TraceTronic GmbH
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
 */
public class TestConfig extends AbstractDescribableImpl<TestConfig> implements Serializable, ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String tbcFile;
    private final String tcfFile;
    private final List<GlobalConstant> constants;

    /**
     * Instantiates a new {@link TestConfig}.
     *
     * @param tbcFile
     *            the test bench configuration file
     * @param tcfFile
     *            the test configuration file
     * @param constants
     *            the list of global constants
     */
    @DataBoundConstructor
    public TestConfig(final String tbcFile, final String tcfFile, final List<GlobalConstant> constants) {
        super();
        this.tbcFile = StringUtils.trimToEmpty(tbcFile);
        this.tcfFile = StringUtils.trimToEmpty(tcfFile);
        this.constants = constants == null ? new ArrayList<GlobalConstant>() : constants;
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
        super();
        this.tbcFile = StringUtils.trimToEmpty(tbcFile);
        this.tcfFile = StringUtils.trimToEmpty(tcfFile);
        constants = new ArrayList<GlobalConstant>();
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
     * @return the global constants
     */
    public List<GlobalConstant> getConstants() {
        return constants;
    }

    @Override
    public TestConfig expand(final EnvVars envVars) {
        final String expTbcFile = envVars.expand(getTbcFile());
        final String expTcfFile = envVars.expand(getTcfFile());
        final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();
        for (final GlobalConstant constant : getConstants()) {
            constants.add(constant.expand(envVars));
        }
        return new TestConfig(expTbcFile, expTcfFile, constants);
    }

    @Override
    public final boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof TestConfig)) {
            return false;
        }
        final TestConfig other = (TestConfig) that;
        if (constants == null ? other.constants != null : !constants.equals(other.constants)
                || tbcFile == null ? other.tbcFile != null : !tbcFile.equals(other.tbcFile)
                || tcfFile == null ? other.tcfFile != null : !tcfFile.equals(other.tcfFile)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(tbcFile).append(tcfFile).append(constants).toHashCode();
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
