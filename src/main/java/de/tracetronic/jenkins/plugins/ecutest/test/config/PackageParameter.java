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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;

/**
 * Class holding a package parameter.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class PackageParameter extends AbstractDescribableImpl<PackageParameter> implements Serializable,
ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String value;

    /**
     * Instantiates a new {@link PackageParameter}.
     *
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     */
    @DataBoundConstructor
    public PackageParameter(final String name, final String value) {
        super();
        this.name = StringUtils.trimToEmpty(name);
        this.value = StringUtils.trimToEmpty(value);
    }

    /**
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the parameter value
     */
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
    public final boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof PackageParameter)) {
            return false;
        }
        final PackageParameter other = (PackageParameter) that;
        if ((name == null ? other.name != null
                : !name.equals(other.name)) || (value == null ? other.value != null : !value
                .equals(other.value))) {
            return false;
        }
        return true;
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

        @Override
        public String getDisplayName() {
            return "Package Parameter";
        }

        /**
         * Validates the parameter name.
         *
         * @param value
         *            the value
         * @return FormValidation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return testValidator.validateParameterName(value);
        }

        /**
         * Validates the parameter value.
         *
         * @param value
         *            the value
         * @return FormValidation
         */
        public FormValidation doCheckValue(@QueryParameter final String value) {
            return testValidator.validateParameterValue(value);
        }
    }
}
