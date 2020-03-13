/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

/**
 * Exception thrown if the maximum timeout was exceeded while communicating with a COM instance.
 */
public class ETComTimeoutException extends ETComException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ETComTimeoutException}.
     *
     * @param cause the cause of the {@link Exception}
     */
    public ETComTimeoutException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new {@link ETComTimeoutException}.
     *
     * @param message the message to attach to the {@link Exception}
     * @param cause   the cause of the {@link Exception}
     */
    public ETComTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new {@link ETComTimeoutException}.
     *
     * @param message the message to attach to the {@link Exception}
     */
    public ETComTimeoutException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new {@link ETComTimeoutException}.
     */
    public ETComTimeoutException() {
        super();
    }
}
