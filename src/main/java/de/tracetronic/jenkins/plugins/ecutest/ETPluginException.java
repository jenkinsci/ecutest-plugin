/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import hudson.model.TaskListener;

/**
 * Exception thrown if an error occurs while performing plugin-specific operations.
 */
public class ETPluginException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ETPluginException}.
     *
     * @param cause the cause of the {@link Exception}
     */
    public ETPluginException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new {@link ETPluginException}.
     *
     * @param message the message to attach to the {@link Exception}
     * @param cause   the cause of the {@link Exception}
     */
    public ETPluginException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new {@link ETPluginException}.
     *
     * @param message the message to attach to the {@link Exception}
     */
    public ETPluginException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new {@link ETPluginException} and logs the message to the build console output.
     *
     * @param message  the message to attach to the {@link Exception}
     * @param listener the listener
     */
    public ETPluginException(final String message, final TaskListener listener) {
        super(message);
        TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logError(message);
    }

    /**
     * Instantiates a new {@link ETPluginException}.
     */
    public ETPluginException() {
        super();
    }
}
