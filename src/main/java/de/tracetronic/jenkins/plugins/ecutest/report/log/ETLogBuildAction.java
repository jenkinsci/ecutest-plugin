/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action to show a link to {@link ETLogReport}s at the build page.
 */
public class ETLogBuildAction extends AbstractETLogAction implements SimpleBuildStep.LastBuildAction {

    private final List<ETLogReport> logReports = new ArrayList<>();

    /**
     * Instantiates a new {@link ETLogBuildAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public ETLogBuildAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the ECU-TEST log reports.
     *
     * @return the log reports
     */
    public List<ETLogReport> getLogReports() {
        return logReports;
    }

    /**
     * Adds a ECU-TEST log report.
     *
     * @param report the ECU-TEST log report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final ETLogReport report) {
        return getLogReports().add(report);
    }

    /**
     * Adds a bundle of ECU-TEST log reports.
     *
     * @param reports the collection of ECU-TEST log reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<ETLogReport> reports) {
        return getLogReports().addAll(reports);
    }

    /**
     * Returns {@link ETLogReport} specified by the URL.
     *
     * @param token the URL token
     * @return the {@link ETLogReport} or {@code null} if no proper report exists
     */
    public ETLogReport getDynamic(final String token) {
        for (final ETLogReport report : getLogReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final ETLogReport potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link ETLogReport} matching the given token id.
     *
     * @param token  the token id
     * @param report the report
     * @return the {@link ETLogReport} or {@code null} if no proper report exists
     */
    private ETLogReport traverseSubReports(final String token, final ETLogReport report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (ETLogReport) subReport;
            } else {
                final ETLogReport potentialReport = traverseSubReports(token, (ETLogReport) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.ETLogBuildAction_DisplayName();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new ETLogProjectAction(isProjectLevel()));
    }
}
