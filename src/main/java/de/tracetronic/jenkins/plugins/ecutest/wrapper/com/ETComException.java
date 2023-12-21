/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

/**
 * Exception thrown if an error occurs while communicating with a COM instance.
 */
public class ETComException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ETComException}.
     *
     * @param cause the cause of the {@link Exception}
     */
    public ETComException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new {@link ETComException}.
     *
     * @param message the message to attach to the {@link Exception}
     * @param cause   the cause of the {@link Exception}
     */
    public ETComException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new {@link ETComException}.
     *
     * @param message the message to attach to the {@link Exception}
     */
    public ETComException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new {@link ETComException}.
     */
    public ETComException() {
        super();
    }
}
