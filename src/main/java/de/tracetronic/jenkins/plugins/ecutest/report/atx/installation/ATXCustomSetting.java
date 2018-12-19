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

import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;
import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common base class for {@link ATXCustomBooleanSetting} and {@link ATXCustomTextSetting}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ATXCustomSetting implements Serializable, Cloneable, Describable<ATXCustomSetting> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXCustomSetting.class.getName());

    private final String name;

    /**
     * Instantiates a new {@link ATXCustomSetting}.
     *
     * @param name
     *            the name of the setting
     */
    public ATXCustomSetting(final String name) {
        this.name = name;
    }

    /**
     * @return the name of the setting
     */
    public String getName() {
        return name;
    }

    @Override
    public ATXCustomSetting clone() {
        ATXCustomSetting clone = null;
        try {
            clone = (ATXCustomSetting) super.clone();
        } catch (final CloneNotSupportedException e) {
            LOGGER.log(Level.SEVERE, "Could not clone ATXCustomSetting!", e);
        }
        return clone;
    }

    @Override
    public boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ATXCustomSetting) {
            final ATXCustomSetting that = (ATXCustomSetting) other;
            result = that.canEqual(this) && (Objects.equals(name, that.name));
        }
        return result;
    }

    /**
     * Implementation according to <a href="www.artima.com/lejava/articles/equality.html">Equality Pitfall #4</a>.
     *
     * @param other
     *            the other object
     * @return {@code true} if the other object is an instance of the class in which canEqual is (re)defined,
     *         {@code false} otherwise.
     */
    public boolean canEqual(final Object other) {
        return other instanceof ATXCustomSetting;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).toHashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<ATXCustomSetting> getDescriptor() {
        return (Descriptor<ATXCustomSetting>) Jenkins.getInstance().getDescriptor(getClass());
    }

    /**
     * Gets all descriptors of {@link ATXCustomSetting} type.
     *
     * @return the descriptor extension list
     */
    public static DescriptorExtensionList<ATXCustomSetting, Descriptor<ATXCustomSetting>> all() {
        return Jenkins.getInstance().getDescriptorList(ATXCustomSetting.class);
    }

    /**
     * DescriptorImpl for {@link ATXCustomSetting}.
     */
    public abstract static class DescriptorImpl extends Descriptor<ATXCustomSetting> {

        /**
         * Validator to check form fields.
         */
        protected final ATXValidator atxValidator = new ATXValidator();

        /**
         * Validates the setting name.
         *
         * @param value
         *            the value
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return atxValidator.validateCustomSettingName(value);
        }
    }
}
