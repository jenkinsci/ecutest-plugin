/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.kohsuke.stapler.DataBoundConstructor;

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
     * @param name         the name of the setting
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
     */
    @DataBoundConstructor
    public ATXBooleanSetting(final String name, final String descGerman, final String descEnglish,
                             final boolean defaultValue) {
        super(name, descGerman, descEnglish, defaultValue);
    }
}
