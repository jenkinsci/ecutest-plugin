/*
 * Copyright (c) 2015-2016 TraceTronic GmbH
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

import hudson.FilePath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogAnnotation.Severity;

/**
 * Parser for the ECU-TEST log files.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogParser {

    private static final Logger LOGGER = Logger.getLogger(ETLogParser.class.getName());

    private static final String LOG_PATTERN = "^[^\\s]+(.*)";
    private static final String WARNING_PATTERN = LOG_PATTERN + "WARNING:$";
    private static final String ERROR_PATTERN = LOG_PATTERN + "ERROR:$";

    private final FilePath logFile;

    /**
     * Instantiates a new {@link ETLogParser}.
     *
     * @param logFile
     *            the log file
     */
    public ETLogParser(final FilePath logFile) {
        this.logFile = logFile;
    }

    /**
     * Parses the ECU-TEST log file.
     *
     * @return the list of annotated log messages
     */
    public List<ETLogAnnotation> parse() {
        final List<ETLogAnnotation> logReports = new ArrayList<ETLogAnnotation>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.read(),
                Charset.forName("UTF-8")))) {
            String line;
            int warnLogCount = 0;
            int errorLogCount = 0;
            final int maxLogCount = AbstractETLogAction.getMaxLogSize();
            try (LineNumberReader lineReader = new LineNumberReader(reader)) {
                while ((line = lineReader.readLine()) != null
                        && (warnLogCount < maxLogCount || errorLogCount < maxLogCount)) {
                    ETLogAnnotation logAnnotation = null;
                    if (warnLogCount < maxLogCount && isWarningLog(line)) {
                        logAnnotation = parseLine(line, lineReader, Severity.WARNING);
                        warnLogCount++;
                    } else if (errorLogCount < maxLogCount && isErrorLog(line)) {
                        logAnnotation = parseLine(line, lineReader, Severity.ERROR);
                        errorLogCount++;
                    }
                    if (logAnnotation != null) {
                        logReports.add(logAnnotation);
                    }
                }
            }
        } catch (final IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,
                    String.format("Failed parsing log file %s: %s", logFile.getRemote(), e.getMessage()));
        }
        return logReports;
    }

    /**
     * Parses the total count of log messages matching the given severity.
     *
     * @param severity
     *            the severity to match
     * @return the total log count by severity
     */
    public int parseLogCount(final Severity severity) {
        int logCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.read(),
                Charset.forName("UTF-8")))) {
            String line;
            try (LineNumberReader lineReader = new LineNumberReader(reader)) {
                while ((line = lineReader.readLine()) != null) {
                    if (severity == Severity.WARNING && isWarningLog(line)
                            || severity == Severity.ERROR && isErrorLog(line)) {
                        logCount++;
                    }
                }
            }
        } catch (final IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE,
                    String.format("Failed parsing log file %s: %s", logFile.getRemote(), e.getMessage()));
        }
        return logCount;
    }

    /**
     * Parses a single log message.
     *
     * @param line
     *            the current line
     * @param lineReader
     *            the line number reader
     * @param severity
     *            the severity to annotate the message.
     * @return the annotated message, can be {@code null}
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    @CheckForNull
    private ETLogAnnotation parseLine(String line, final LineNumberReader lineReader, final Severity severity)
            throws IOException {
        ETLogAnnotation logAnnotation = null;
        if (line != null) {
            final String[] lineSplit = line.split("\\s+");
            if (lineSplit.length == 5) {
                final int lineNumber = lineReader.getLineNumber();
                final StringBuilder msg = new StringBuilder();
                while ((line = lineReader.readLine()) != null) {
                    if (Pattern.matches(LOG_PATTERN, line)) {
                        if (lineReader.markSupported()) {
                            lineReader.reset();
                        }
                        break;
                    } else if (StringUtils.isNotBlank(line)) {
                        msg.append(line.trim() + "\n");
                    }
                    lineReader.mark(4096);
                }
                logAnnotation = new ETLogAnnotation(lineNumber, lineSplit[0] + " "
                        + lineSplit[1], lineSplit[3], severity, msg.toString());
            }
        }
        return logAnnotation;
    }

    /**
     * Checks whether the given log line is a warning message.
     *
     * @param line
     *            the log line
     * @return {@code true} if warning message, {@code false} otherwise
     */
    private boolean isWarningLog(final String line) {
        return Pattern.matches(WARNING_PATTERN, line);
    }

    /**
     * Checks whether the given log line is an error message.
     *
     * @param line
     *            the log line
     * @return {@code true} if error message, {@code false} otherwise
     */
    private boolean isErrorLog(final String line) {
        return Pattern.matches(ERROR_PATTERN, line);
    }
}
