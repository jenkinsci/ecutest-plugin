/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.Extension;
import hudson.model.Descriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Class holding the information of a boolean ATX setting.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXBooleanSetting extends ATXSetting<Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ATXBooleanSetting}.
     *
     * @param name  the name
     * @param group the settings group
     * @param value the current value
     */
    @DataBoundConstructor
    public ATXBooleanSetting(final String name, final SettingsGroup group, final boolean value) {
        super(name, group, value);
    }

    /**
     * Instantiates a new {@link ATXBooleanSetting} with default values.
     *
     * @param name         the name
     * @param group        the settings group
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
     */
    public ATXBooleanSetting(final String name, SettingsGroup group,
                             final String descGerman, final String descEnglish,
                             final boolean defaultValue) {
        super(name, group, descGerman, descEnglish, defaultValue);
    }

    /**
     * DescriptorImpl of {@link ATXBooleanSetting}.
     */
    @Symbol("atxBooleanSetting")
    @Extension
    public static class DescriptorImpl extends Descriptor<ATXSetting<?>> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXBooleanSetting_DisplayName();
        }
    }
}
