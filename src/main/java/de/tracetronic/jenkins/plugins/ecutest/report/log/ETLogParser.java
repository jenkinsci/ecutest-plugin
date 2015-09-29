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

import hudson.FilePath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.LineIterator;
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

    /**
     * Parses the ECU-TEST log file.
     *
     * @param logFile
     *            the log file
     * @return the list of annotated log messages
     */
    public List<ETLogAnnotation> parse(final FilePath logFile) {
        final List<ETLogAnnotation> logReports = new ArrayList<ETLogAnnotation>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.read(),
                Charset.forName("UTF-8")))) {
            String line = "";
            int lineNumber = 0;
            final LineIterator it = new LineIterator(reader);
            while (it.hasNext()) {
                final String[] lineSplit = line.split("\\s+");
                if (lineSplit.length == 5) {
                    if (Pattern.matches(WARNING_PATTERN, line)) {
                        final ETLogAnnotation warnLog = new ETLogAnnotation(lineNumber++, lineSplit[0] + " "
                                + lineSplit[1], lineSplit[3], Severity.WARNING, it.nextLine().trim());
                        logReports.add(warnLog);
                    } else if (Pattern.matches(ERROR_PATTERN, line)) {
                        final StringBuilder errorMsg = new StringBuilder();
                        int errorLines = 0;
                        while (it.hasNext()) {
                            line = it.nextLine();
                            if (Pattern.matches(LOG_PATTERN, line)) {
                                break;
                            } else {
                                if (StringUtils.isNotBlank(line)) {
                                    errorMsg.append(line.trim() + (it.hasNext() ? "\n" : ""));
                                }
                                errorLines++;
                            }
                        }
                        final ETLogAnnotation errorLog = new ETLogAnnotation(lineNumber++, lineSplit[0] + " "
                                + lineSplit[1], lineSplit[3], Severity.ERROR, errorMsg.toString());
                        logReports.add(errorLog);
                        lineNumber += errorLines;
                        continue;
                    }
                }
                line = it.nextLine();
                lineNumber++;
            }
            it.close();
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE,
                    String.format("Failed parsing log file %s: %s", logFile.getRemote(), e.getMessage()));
        }
        return logReports;
    }
}
