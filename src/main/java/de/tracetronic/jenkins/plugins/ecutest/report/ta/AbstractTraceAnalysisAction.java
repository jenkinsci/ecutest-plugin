/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportAction;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Common base class for {@link TraceAnalysisBuildAction} and {@link TraceAnalysisProjectAction}.
 */
public abstract class AbstractTraceAnalysisAction extends AbstractReportAction {

    /**
     * Instantiates a new {@link AbstractTraceAnalysisAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public AbstractTraceAnalysisAction(final boolean projectLevel) {
        super(projectLevel);
    }

    @Override
    public Run<?, ?> getLastReportBuild(final Job<?, ?> project) {
        for (Run<?, ?> build = project.getLastBuild(); build != null; build = build.getPreviousBuild()) {
            if (build.getAction(TraceAnalysisBuildAction.class) != null) {
                return build;
            }
        }
        return null;
    }

    @Override
    public String getUrlName() {
        return TraceAnalysisPublisher.URL_NAME;
    }

    @Override
    public String getIconClassName() {
        return "icon-ecutest-trace-check";
    }

    /**
     * @return the report icon class name
     */
    public String getReportIconClassName() {
        return "icon-ecutest-trace-report";
    }
}
