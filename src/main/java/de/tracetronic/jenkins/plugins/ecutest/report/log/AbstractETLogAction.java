/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportAction;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Common base class for {@link ETLogBuildAction} and {@link ETLogProjectAction}.
 */
public abstract class AbstractETLogAction extends AbstractReportAction {

    private static final int MAX_LOG_SIZE = 10;

    /**
     * Instantiates a new {@link AbstractETLogAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public AbstractETLogAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the maximum size of logs to show.
     *
     * @return the max log size
     */
    public static int getMaxLogSize() {
        return MAX_LOG_SIZE;
    }

    @Override
    public Run<?, ?> getLastReportBuild(final Job<?, ?> project) {
        for (Run<?, ?> build = project.getLastBuild(); build != null; build = build.getPreviousBuild()) {
            if (build.getAction(ETLogBuildAction.class) != null) {
                return build;
            }
        }
        return null;
    }

    @Override
    public String getUrlName() {
        return ETLogPublisher.URL_NAME;
    }

    @Override
    public String getIconClassName() {
        return "icon-ecutest-ecu-test";
    }
}
