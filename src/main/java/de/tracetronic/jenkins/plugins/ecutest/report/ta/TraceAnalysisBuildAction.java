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
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action to show a link to {@link TraceAnalysisReport}s at the build page.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TraceAnalysisBuildAction extends AbstractTraceAnalysisAction implements SimpleBuildStep.LastBuildAction {

    private final List<TraceAnalysisReport> taReports = new ArrayList<>();

    /**
     * Instantiates a new {@link TraceAnalysisBuildAction}.
     *
     * @param projectLevel
     *            specifies whether archiving is restricted to project level only
     */
    public TraceAnalysisBuildAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the trace analysis reports.
     *
     * @return the trace analysis reports
     */
    public List<TraceAnalysisReport> getTraceAnalysisReports() {
        return taReports;
    }

    /**
     * Adds a trace analysis report.
     *
     * @param report
     *            the trace analysis report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final TraceAnalysisReport report) {
        return getTraceAnalysisReports().add(report);
    }

    /**
     * Adds a bundle of trace analysis reports.
     *
     * @param reports
     *            the collection of trace analysis reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<TraceAnalysisReport> reports) {
        return getTraceAnalysisReports().addAll(reports);
    }

    /**
     * Returns {@link TraceAnalysisReport} specified by the URL.
     *
     * @param token
     *            the URL token
     * @return the {@link TraceAnalysisReport} or {@code null} if no proper report exists
     */
    public AbstractTestReport getDynamic(final String token) {
        for (final TraceAnalysisReport report : getTraceAnalysisReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final TraceAnalysisReport potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link TraceAnalysisReport} matching the given token id.
     *
     * @param token
     *            the token id
     * @param report
     *            the report
     * @return the {@link TraceAnalysisReport} or {@code null} if no proper report exists
     */
    private TraceAnalysisReport traverseSubReports(final String token, final TraceAnalysisReport report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (TraceAnalysisReport) subReport;
            } else {
                final TraceAnalysisReport potentialReport = traverseSubReports(token, (TraceAnalysisReport) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.TraceAnalysisBuildAction_DisplayName();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new TraceAnalysisProjectAction(isProjectLevel()));
    }
}
