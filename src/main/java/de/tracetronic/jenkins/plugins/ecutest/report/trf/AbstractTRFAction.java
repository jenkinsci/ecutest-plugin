/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportAction;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Common base class for {@link TRFBuildAction} and {@link TRFProjectAction}.
 */
public abstract class AbstractTRFAction extends AbstractReportAction {

    /**
     * Instantiates a new {@link AbstractTRFAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public AbstractTRFAction(final boolean projectLevel) {
        super(projectLevel);
    }

    @Override
    public Run<?, ?> getLastReportBuild(final Job<?, ?> project) {
        for (Run<?, ?> build = project.getLastBuild(); build != null; build = build.getPreviousBuild()) {
            final TRFBuildAction buildAction = build.getAction(TRFBuildAction.class);
            if (buildAction != null) {
                return build;
            }
        }
        return null;
    }

    @Override
    public String getUrlName() {
        return TRFPublisher.URL_NAME;
    }

    @Override
    public String getIconClassName() {
        return "icon-ecutest-trf-report";
    }
}
