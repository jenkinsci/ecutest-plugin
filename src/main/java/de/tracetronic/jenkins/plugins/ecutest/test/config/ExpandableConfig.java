/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;

/**
 * Interface describing a factory to expand specific test configurations.
 */
public interface ExpandableConfig {

    /**
     * Expands the test configuration parameters by using
     * the current build environment variables.
     *
     * @param envVars the build environment variables
     * @return the expanded specific configuration
     */
    ExpandableConfig expand(EnvVars envVars);
}
