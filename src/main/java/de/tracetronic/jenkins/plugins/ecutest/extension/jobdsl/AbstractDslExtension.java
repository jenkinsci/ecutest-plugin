/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import javaposse.jobdsl.plugin.ContextExtensionPoint;

/**
 * Common base class providing plugin specific DSL extensions.
 */
public abstract class AbstractDslExtension extends ContextExtensionPoint {

    /**
     * Option name for the tool installation name.
     */
    protected static final String OPT_TOOL_NAME = "toolName";

    /**
     * Option name for the timeout.
     */
    protected static final String OPT_TIMEOUT = "timeout";

    /**
     * Exception message for invalid options.
     */
    protected static final String NOT_NULL_MSG = "Setting '%s' cannot be null!";

    /**
     * Exception message for invalid tool installation option.
     */
    protected static final String NO_INSTALL_MSG = "'%s' is not in the list of configured ecu.test installations!";
}
