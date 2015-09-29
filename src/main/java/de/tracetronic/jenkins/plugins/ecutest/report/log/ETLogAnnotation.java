/**
 * Copyright (c) 2015 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        ERROR;
    }

    private final int lineNumber;
    private final String timestamp;
    private final String context;
    private final Severity severity;
    private final String message;

    /**
     * Instantiates a new {@link ETLogAnnotation}.
     *
     * @param lineNumber
     *            the line number
     * @param timestamp
     *            the timestamp
     * @param context
     *            the context
     * @param severity
     *            the severity
     * @param message
     *            the message
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
}
