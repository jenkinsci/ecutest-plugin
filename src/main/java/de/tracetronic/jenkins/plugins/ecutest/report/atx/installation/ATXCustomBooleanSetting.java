/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.Extension;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Class holding the information of an additional boolean ATX setting.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXCustomBooleanSetting extends ATXCustomSetting {

    private static final long serialVersionUID = 1L;

    private final boolean checked;

    /**
     * Instantiates a new {@link ATXCustomBooleanSetting}.
     *
     * @param name    the name of the setting
     * @param checked the checkbox status
     */
    @DataBoundConstructor
    public ATXCustomBooleanSetting(final String name, final boolean checked) {
        super(name);
        this.checked = checked;
    }

    /**
     * @return {@code true} if the checkbox is checked, {@code false} otherwise
     */
    public boolean isChecked() {
        return checked;
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ATXCustomBooleanSetting) {
            final ATXCustomBooleanSetting that = (ATXCustomBooleanSetting) other;
            result = that.canEqual(this) && super.equals(that) && checked == that.checked;
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ATXCustomBooleanSetting;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode()).append(checked).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ATXBooleanSetting}.
     */
    @Extension
    public static class DescriptorImpl extends ATXCustomSetting.DescriptorImpl {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXCustomBooleanSetting_DisplayName();
        }
    }
}
