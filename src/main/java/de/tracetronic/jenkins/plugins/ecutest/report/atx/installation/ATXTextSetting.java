/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.kohsuke.stapler.DataBoundConstructor;

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
     * @param name         the name of the setting
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
     */
    @DataBoundConstructor
    public ATXTextSetting(final String name, final String descGerman, final String descEnglish,
                          final String defaultValue) {
        super(name, descGerman, descEnglish, defaultValue);
    }
}
