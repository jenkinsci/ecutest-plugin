/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action to show a link to {@link TRFReport}s at the build page.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TRFBuildAction extends AbstractTRFAction implements SimpleBuildStep.LastBuildAction {

    private final List<TRFReport> trfReports = new ArrayList<>();

    /**
     * Instantiates a new {@link TRFBuildAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public TRFBuildAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the TRF reports.
     *
     * @return the TRF reports
     */
    public List<TRFReport> getTRFReports() {
        return trfReports;
    }

    /**
     * Adds a TRF report.
     *
     * @param report the TRF report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final TRFReport report) {
        return getTRFReports().add(report);
    }

    /**
     * Adds a bundle of TRF reports.
     *
     * @param reports the collection of TRF reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<TRFReport> reports) {
        return getTRFReports().addAll(reports);
    }

    /**
     * Returns {@link TRFReport} specified by the URL.
     *
     * @param token the URL token
     * @return the {@link TRFReport} or {@code null} if no proper report exists
     */
    public AbstractTestReport getDynamic(final String token) {
        for (final TRFReport report : getTRFReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final TRFReport potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link TRFReport} matching the given token id.
     *
     * @param token  the token id
     * @param report the report
     * @return the {@link TRFReport} or {@code null} if no proper report exists
     */
    private TRFReport traverseSubReports(final String token, final TRFReport report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (TRFReport) subReport;
            } else {
                final TRFReport potentialReport = traverseSubReports(token, (TRFReport) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.TRFBuildAction_DisplayName();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new TRFProjectAction(isProjectLevel()));
    }
}
