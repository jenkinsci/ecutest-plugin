/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;
import hudson.DescriptorExtensionList;
import hudson.model.AbstractDescribableImpl;
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
 */
public abstract class ATXCustomSetting extends AbstractDescribableImpl<ATXCustomSetting>
    implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXCustomSetting.class.getName());

    private final String name;

    /**
     * Instantiates a new {@link ATXCustomSetting}.
     *
     * @param name the name of the setting
     */
    public ATXCustomSetting(final String name) {
        this.name = name;
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
            result = that.canEqual(this) && Objects.equals(name, that.name);
        }
        return result;
    }

    /**
     * Implementation according to <a href="www.artima.com/lejava/articles/equality.html">Equality Pitfall #4</a>.
     *
     * @param other the other object
     * @return {@code true} if the other object is an instance of the class in which canEqual is (re)defined,
     * {@code false} otherwise.
     */
    public boolean canEqual(final Object other) {
        return other instanceof ATXCustomSetting;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).toHashCode();
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
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return atxValidator.validateCustomSettingName(value);
        }
    }
}
