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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.Extension;
import hudson.util.FormValidation;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Class holding the information of an additional text ATX setting.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXCustomTextSetting extends ATXCustomSetting {

    private static final long serialVersionUID = 1L;

    private final String value;

    /**
     * Instantiates a new {@link ATXCustomTextSetting}.
     *
     * @param name
     *            the name of the setting
     * @param value
     *            the value of the setting
     */
    @DataBoundConstructor
    public ATXCustomTextSetting(final String name, final String value) {
        super(name);
        this.value = value;
    }

    /**
     * @return the value of the setting
     */
    public String getValue() {
        return value;
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ATXCustomTextSetting) {
            final ATXCustomTextSetting that = (ATXCustomTextSetting) other;
            result = that.canEqual(this) && super.equals(that)
                    && (Objects.equals(value, that.value));
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ATXCustomTextSetting;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode()).append(value).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ATXTextSetting}.
     */
    @Extension
    public static class DescriptorImpl extends ATXCustomSetting.DescriptorImpl {

        /**
         * Validates the setting value.
         *
         * @param value
         *            the value
         * @return the form validation
         */
        public FormValidation doCheckValue(@QueryParameter final String value) {
            return atxValidator.validateCustomSettingValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXCustomTextSetting_DisplayName();
        }
    }
}
