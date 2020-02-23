/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogAnnotation.Severity;
import hudson.FilePath;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class providing a parser for the ECU-TEST log files.
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
     * @param logFile the log file
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
        final List<ETLogAnnotation> logReports = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(logFile.read(), StandardCharsets.UTF_8))) {
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
     * @param severity the severity to match
     * @return the total log count by severity
     */
    public int parseLogCount(final Severity severity) {
        int logCount = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(logFile.read(), StandardCharsets.UTF_8))) {
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
     * @param currentLine the current line
     * @param lineReader  the line number reader
     * @param severity    the severity to annotate the message.
     * @return the annotated message, can be {@code null}
     * @throws IOException signals that an I/O exception has occurred
     */
    @CheckForNull
    private ETLogAnnotation parseLine(final String currentLine, final LineNumberReader lineReader,
                                      final Severity severity)
            throws IOException {
        ETLogAnnotation logAnnotation = null;
        if (currentLine != null) {
            final String[] lineSplit = currentLine.split("\\s+");
            if (lineSplit.length == 5) {
                String line;
                final int lineNumber = lineReader.getLineNumber();
                final StringBuilder msg = new StringBuilder();
                while ((line = lineReader.readLine()) != null) {
                    if (Pattern.matches(LOG_PATTERN, line)) {
                        if (lineReader.markSupported()) {
                            lineReader.reset();
                        }
                        break;
                    } else if (StringUtils.isNotBlank(line)) {
                        msg.append(line.trim()).append("\n");
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
     * @param line the log line
     * @return {@code true} if warning message, {@code false} otherwise
     */
    private boolean isWarningLog(final String line) {
        return Pattern.matches(WARNING_PATTERN, line);
    }

    /**
     * Checks whether the given log line is an error message.
     *
     * @param line the log line
     * @return {@code true} if error message, {@code false} otherwise
     */
    private boolean isErrorLog(final String line) {
        return Pattern.matches(ERROR_PATTERN, line);
    }
}
