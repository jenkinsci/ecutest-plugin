/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action to show a link to {@link ATXReport}s or {@link ATXZipReport}s at the build page.
 *
 * @param <T> the report type, either {@link ATXReport} or {@link ATXZipReport}
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXBuildAction<T extends AbstractTestReport> extends AbstractATXAction implements
    SimpleBuildStep.LastBuildAction {

    private final List<T> atxReports = new ArrayList<>();

    /**
     * Instantiates a new {@link ATXBuildAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public ATXBuildAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the ATX reports.
     *
     * @return the ATX reports
     */
    public List<T> getATXReports() {
        return atxReports;
    }

    /**
     * Adds a ATX report.
     *
     * @param report the ATX report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final T report) {
        return getATXReports().add(report);
    }

    /**
     * Adds a bundle of ATX reports.
     *
     * @param reports the collection of ATX reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<T> reports) {
        return getATXReports().addAll(reports);
    }

    /**
     * Returns {@link ATXReport} specified by the URL.
     *
     * @param token the URL token
     * @return the {@link ATXReport} or {@code null} if no proper report exists
     */
    public T getDynamic(final String token) {
        for (final T report : getATXReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final T potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link ATXReport} matching the given token id.
     *
     * @param token  the token id
     * @param report the report
     * @return the {@link ATXReport} or {@code null} if no proper report exists
     */
    @SuppressWarnings("unchecked")
    private T traverseSubReports(final String token, final T report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (T) subReport;
            } else {
                final T potentialReport = traverseSubReports(token, (T) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Specifies whether this action holds archived {@link ATXZipReport}s.
     *
     * @return {@code true} if has archived reports, {@code false} otherwise
     */
    public boolean hasArchivedReports() {
        return !getATXReports().isEmpty() && getATXReports().get(0) instanceof ATXZipReport;
    }

    @Override
    public String getDisplayName() {
        return Messages.ATXBuildAction_DisplayName();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new ATXProjectAction(isProjectLevel()));
    }
}
