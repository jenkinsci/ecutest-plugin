/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import java.io.Serializable;

/**
 * Annotates each parsed line in a ECU-TEST log file.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogAnnotation implements Serializable {

    private static final long serialVersionUID = 1L;
    private final int lineNumber;
    private final String timestamp;
    private final String context;
    private final Severity severity;
    private final String message;

    /**
     * Instantiates a new {@link ETLogAnnotation}.
     *
     * @param lineNumber the line number
     * @param timestamp  the timestamp
     * @param context    the context
     * @param severity   the severity
     * @param message    the message
     */
    public ETLogAnnotation(final int lineNumber, final String timestamp, final String context,
                           final Severity severity, final String message) {
        super();
        this.lineNumber = lineNumber;
        this.timestamp = timestamp;
        this.context = context;
        this.severity = severity;
        this.message = message;
    }

    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Defines the severities for a log message.
     */
    public enum Severity {
        /**
         * Severity indicating this log message is informational only.
         */
        INFO,

        /**
         * Severity indicating this log message contains debug information.
         */
        DEBUG,

        /**
         * Severity indicating this log message represents a warning.
         */
        WARNING,

        /**
         * Severity indication this log message represents an error.
         */
        ERROR
    }
}
