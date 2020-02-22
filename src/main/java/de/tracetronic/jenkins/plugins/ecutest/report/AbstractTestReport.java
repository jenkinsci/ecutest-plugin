/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import hudson.Util;
import hudson.model.ModelObject;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Common base class for several test reports created by {@link AbstractReportPublisher}s.
 */
public abstract class AbstractTestReport extends AbstractRequestHandler implements ModelObject {

    private final String id;
    private final String title;
    private final List<AbstractTestReport> subReports;

    /**
     * Instantiates a new {@link AbstractTestReport}.
     *
     * @param id    the id used in report URL
     * @param title the report title
     */
    public AbstractTestReport(final String id, final String title) {
        super();
        this.id = id;
        this.title = title;
        subReports = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Gets the sub-reports.
     *
     * @return the subReports
     */
    public List<AbstractTestReport> getSubReports() {
        return subReports;
    }

    /**
     * Adds a sub-report.
     *
     * @param subReport the subReport to add
     */
    public void addSubReport(final AbstractTestReport subReport) {
        subReports.add(subReport);
    }

    /**
     * Adds a bundle of sub-reports.
     *
     * @param subReports the subReports to add
     */
    public void addSubReports(final List<AbstractTestReport> subReports) {
        this.subReports.addAll(subReports);
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    public String getUrl() {
        return Util.rawEncode(getId());
    }

    @Override
    public Run<?, ?> getBuild(final StaplerRequest req) {
        return getAnchestorBuild(req);
    }
}
