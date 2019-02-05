/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportAction;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Common base class for {@link ReportGeneratorBuildAction} and {@link ReportGeneratorProjectAction}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractReportGeneratorAction extends AbstractReportAction {

    /**
     * Instantiates a new {@link AbstractReportGeneratorAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public AbstractReportGeneratorAction(final boolean projectLevel) {
        super(projectLevel);
    }

    @Override
    public Run<?, ?> getLastReportBuild(final Job<?, ?> project) {
        for (Run<?, ?> build = project.getLastBuild(); build != null; build = build.getPreviousBuild()) {
            if (build.getAction(ReportGeneratorBuildAction.class) != null) {
                return build;
            }
        }
        return null;
    }

    @Override
    public String getUrlName() {
        return ReportGeneratorPublisher.URL_NAME;
    }

    @Override
    public String getIconClassName() {
        return "icon-ecutest-report-generator";
    }
}
