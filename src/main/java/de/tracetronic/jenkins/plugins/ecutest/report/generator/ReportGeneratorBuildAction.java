/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action to show a link to {@link GeneratorReport}s at the build page.
 */
public class ReportGeneratorBuildAction extends AbstractReportGeneratorAction implements
    SimpleBuildStep.LastBuildAction {

    private final List<GeneratorReport> generatorReports = new ArrayList<>();

    /**
     * Instantiates a new {@link ReportGeneratorBuildAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public ReportGeneratorBuildAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the generator reports.
     *
     * @return the generator reports
     */
    public List<GeneratorReport> getGeneratorReports() {
        return generatorReports;
    }

    /**
     * Adds a generator report.
     *
     * @param report the generator report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final GeneratorReport report) {
        return getGeneratorReports().add(report);
    }

    /**
     * Adds a bundle of generator reports.
     *
     * @param reports the collection of generator reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<GeneratorReport> reports) {
        return getGeneratorReports().addAll(reports);
    }

    /**
     * Returns {@link GeneratorReport} specified by the URL.
     *
     * @param token the URL token
     * @return the {@link GeneratorReport} or {@code null} if no proper report exists
     */
    public AbstractTestReport getDynamic(final String token) {
        for (final GeneratorReport report : getGeneratorReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final GeneratorReport potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link GeneratorReport} matching the given token id.
     *
     * @param token  the token id
     * @param report the report
     * @return the {@link GeneratorReport} or {@code null} if no proper report exists
     */
    private GeneratorReport traverseSubReports(final String token, final GeneratorReport report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (GeneratorReport) subReport;
            } else {
                final GeneratorReport potentialReport = traverseSubReports(token, (GeneratorReport) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.ReportGeneratorBuildAction_DisplayName();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new ReportGeneratorProjectAction(isProjectLevel()));
    }
}
