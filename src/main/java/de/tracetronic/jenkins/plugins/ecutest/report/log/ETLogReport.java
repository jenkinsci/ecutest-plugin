/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractArchiveFileReport;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogAnnotation.Severity;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a link to the ECU-TEST log report. The build that holds the artifact is resolved by
 * {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogReport extends AbstractArchiveFileReport {

    private final List<ETLogAnnotation> logs;
    private final int warningLogCount;
    private final int errorLogCount;

    /**
     * Instantiates a new {@link ETLogReport}.
     *
     * @param id              the id used in the report URL
     * @param title           the report title
     * @param fileName        the log file name
     * @param fileSize        the log file size
     * @param logs            the list of annotated logs
     * @param warningLogCount the total count of warning logs
     * @param errorLogCount   the total count of error logs
     */
    public ETLogReport(final String id, final String title, final String fileName, final long fileSize,
                       final List<ETLogAnnotation> logs, final int warningLogCount, final int errorLogCount) {
        super(id, title, fileName, fileSize);
        this.logs = logs == null ? new ArrayList<>() : logs;
        this.warningLogCount = warningLogCount;
        this.errorLogCount = errorLogCount;
    }

    /**
     * @return the annotated logs
     */
    public List<ETLogAnnotation> getLogs() {
        return logs;
    }

    /**
     * Gets the warning logs.
     *
     * @return the warning logs
     */
    public List<ETLogAnnotation> getWarningLogs() {
        return getLogs(Severity.WARNING);
    }

    /**
     * Gets the error logs.
     *
     * @return the error logs
     */
    public List<ETLogAnnotation> getErrorLogs() {
        return getLogs(Severity.ERROR);
    }

    /**
     * Gets the logs by severity.
     *
     * @param severity the severity
     * @return the list of logs matched the severity
     */
    private List<ETLogAnnotation> getLogs(final Severity severity) {
        final List<ETLogAnnotation> logs = new ArrayList<>();
        for (final ETLogAnnotation log : getLogs()) {
            if (log.getSeverity().equals(severity)) {
                logs.add(log);
            }
        }
        return logs;
    }

    /**
     * Gets the total count of warning logs.
     *
     * @return the warningLogCount
     */
    public int getWarningLogCount() {
        return warningLogCount;
    }

    /**
     * Gets the total count of error logs.
     *
     * @return the errorLogCount
     */
    public int getErrorLogCount() {
        return errorLogCount;
    }

    /**
     * Gets the total count of warning logs including all sub reports.
     *
     * @return the total warning log count
     */
    public int getTotalWarningCount() {
        int warningLogCount = getWarningLogCount();
        for (final AbstractTestReport subReport : getSubReports()) {
            warningLogCount += ((ETLogReport) subReport).getTotalWarningCount();
        }
        return warningLogCount;
    }

    /**
     * Gets the total count of warning logs including all sub reports.
     *
     * @return the total error log count
     */
    public int getTotalErrorCount() {
        int errorLogCount = getErrorLogCount();
        for (final AbstractTestReport subReport : getSubReports()) {
            errorLogCount += ((ETLogReport) subReport).getTotalErrorCount();
        }
        return errorLogCount;
    }

    @Override
    public String getArchiveDir() {
        return ETLogPublisher.URL_NAME;
    }
}
