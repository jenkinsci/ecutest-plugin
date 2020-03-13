/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
 */
public class TraceAnalysisBuildAction extends AbstractTraceAnalysisAction implements SimpleBuildStep.LastBuildAction {

    private final List<TraceAnalysisReport> taReports = new ArrayList<>();

    /**
     * Instantiates a new {@link TraceAnalysisBuildAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
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
     * @param report the trace analysis report to add
     */
    public void add(final TraceAnalysisReport report) {
        taReports.add(report);
    }

    /**
     * Adds a bundle of trace analysis reports.
     *
     * @param reports the collection of trace analysis reports
     */
    public void addAll(final Collection<TraceAnalysisReport> reports) {
        this.taReports.addAll(reports);
    }

    /**
     * Returns {@link TraceAnalysisReport} specified by the URL.
     *
     * @param token the URL token
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
     * @param token  the token id
     * @param report the report
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
