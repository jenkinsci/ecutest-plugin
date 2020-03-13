/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import hudson.EnvVars;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class providing environment variable operations.
 */
public final class EnvUtil {

    /**
     * Instantiates a {@link EnvUtil}.
     */
    private EnvUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Expands a variable using build environment variables or returns the default value.
     *
     * @param envVar       the environment variable to expand
     * @param buildEnvVars the existing build environment variable
     * @param defaultValue the default value if environment variable is empty
     * @return the expanded environment variable or default value
     */
    public static String expandEnvVar(final String envVar, final EnvVars buildEnvVars,
                                      final String defaultValue) {
        final String expandedEnvVar;
        if (StringUtils.isBlank(envVar)) {
            expandedEnvVar = defaultValue;
        } else {
            expandedEnvVar = buildEnvVars.expand(envVar);
        }
        return expandedEnvVar;
    }
}
