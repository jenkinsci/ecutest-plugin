/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Class holding the information of a secret-based ATX setting.
 */
public class ATXSecretSetting extends ATXSetting<Secret> {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ATXSecretSetting}.
     *
     * @param name  the name
     * @param group the settings group
     * @param value the current value
     */
    @DataBoundConstructor
    public ATXSecretSetting(final String name, final SettingsGroup group, final Secret value) {
        super(name, group, value);
    }

    /**
     * Instantiates a new {@link ATXSecretSetting} with default values.
     *
     * @param name         the name
     * @param group        the settings group
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
     */
    public ATXSecretSetting(final String name, final SettingsGroup group,
                            final String descGerman, final String descEnglish,
                            final Secret defaultValue) {
        super(name, group, descGerman, descEnglish, defaultValue);
    }

    @Whitelisted
    public String getSecretValue() {
        return Secret.toString(value);
    }

    /**
     * DescriptorImpl of {@link ATXSecretSetting}.
     */
    @Symbol("atxSecretSetting")
    @Extension
    public static class DescriptorImpl extends Descriptor<ATXSetting<?>> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXSecretSetting_DisplayName();
        }
    }
}
