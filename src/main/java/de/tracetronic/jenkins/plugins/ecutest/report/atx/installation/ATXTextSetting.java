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
 * Class holding the information of a text-based ATX setting.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXTextSetting extends ATXSetting<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ATXTextSetting}.
     *
     * @param name  the name of the setting
     * @param value the current value
     */
    @DataBoundConstructor
    public ATXTextSetting(final String name, final String value) {
        super(name, value);
    }

    /**
     * Instantiates a new {@link ATXTextSetting} with default values.
     *
     * @param name         the name of the setting
     * @param group        the related settings group
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
     */
    public ATXTextSetting(final String name, SettingsGroup group,
                          final String descGerman, final String descEnglish,
                          final String defaultValue) {
        super(name, group, descGerman, descEnglish, defaultValue);
    }

    /**
     * DescriptorImpl of {@link ATXTextSetting}.
     */
    @Symbol("atx-text-setting")
    @Extension
    public static class DescriptorImpl extends Descriptor<ATXSetting<?>> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXTextSetting_DisplayName();
        }
    }
}
