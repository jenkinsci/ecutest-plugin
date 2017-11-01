/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.log;

import hudson.model.TaskListener;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * A helper class which offers various types of logging. Currently it provides plain logging directly into console log
 * and annotated log via {@link TTConsoleAnnotator}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TTConsoleLogger {

    private final TaskListener listener;
    private final TTConsoleAnnotator annotator;

    /**
     * Instantiates a new {@link TTConsoleLogger}.
     *
     * @param listener
     *            the listener
     */
    public TTConsoleLogger(final TaskListener listener) {
        this.listener = listener;
        annotator = new TTConsoleAnnotator(this.listener.getLogger());
    }

    /**
     * Gets the logger.
     *
     * @return the {@link PrintStream} logger
     */
    public PrintStream getLogger() {
        return listener.getLogger();
    }

    /**
     * Logs annotated message.
     *
     * @param message
     *            the message to log
     */
    public void logAnnot(final String message) {
        logAnnot("", message);
    }

    /**
     * Logs info message.
     *
     * @param message
     *            the message to log
     */
    public void logInfo(final String message) {
        logAnnot("[TT] INFO: ", message);
    }

    /**
     * Logs warning message.
     *
     * @param message
     *            the message to log
     */
    public void logWarn(final String message) {
        logAnnot("[TT] WARN: ", message);
    }

    /**
     * Logs error message.
     *
     * @param message
     *            the message to log
     */
    public void logError(final String message) {
        logAnnot("[TT] ERROR: ", message);
    }

    /**
     * Logs error message caused by COM exception.
     *
     * @param message
     *            the message to log
     */
    public void logComException(final String message) {
        logError(String
                .format("Caught ComException: %s\n"
                        + "For further information see FAQ: "
                        + "https://wiki.jenkins-ci.org/x/joLtB#TraceTronicECU-TESTPlugin-FAQ",
                        message));
    }

    /**
     * Logs debug message. Can be enabled either by
     * providing -Decutest.debugLog=true to Jenkins master JVM or
     * setting system property ecutest.debugLog directly.
     * 
     * @param message
     *            the message to log
     */
    public void logDebug(final String message) {
        if (Boolean.getBoolean("ecutest.debugLog")) {
            logAnnot("[TT] DEBUG: ", message);
        }
    }

    /**
     * Logs annotated message.
     *
     * @param prefix
     *            the prefix
     * @param message
     *            message to be annotated
     */
    public void logAnnot(final String prefix, final String message) {
        final String log = prefix + message + "\n";
        final byte[] msg = log.getBytes(Charset.defaultCharset());
        try {
            annotator.eol(msg, msg.length);
        } catch (final IOException e) {
            listener.getLogger().println("Problem with writing into console log: " + e.getMessage());
        }
    }

    /**
     * Logs plain text messages directly into console.
     *
     * @param message
     *            message in plain text
     */
    public void log(final String message) {
        listener.getLogger().println(message);
    }
}
