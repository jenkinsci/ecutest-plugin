/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportAction;
import hudson.model.Job;
import hudson.model.Run;

/**
 * Common base class for {@link ATXBuildAction} and {@link ATXProjectAction}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractATXAction extends AbstractReportAction {

    /**
     * Instantiates a new {@link AbstractATXAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public AbstractATXAction(final boolean projectLevel) {
        super(projectLevel);
    }

    @Override
    public Run<?, ?> getLastReportBuild(final Job<?, ?> project) {
        for (Run<?, ?> build = project.getLastBuild(); build != null; build = build.getPreviousBuild()) {
            if (build.getAction(ATXBuildAction.class) != null) {
                return build;
            }
        }
        return null;
    }

    @Override
    public String getUrlName() {
        return ATXPublisher.URL_NAME;
    }

    @Override
    public String getIconClassName() {
        return "icon-ecutest-test-guide";
    }

    /**
     * @return the report icon class name
     */
    public String getReportIconClassName() {
        return "icon-ecutest-atx-report";
    }

    /**
     * @return the trend icon class name
     */
    public String getTrendIconClassName() {
        return "icon-ecutest-atx-trend";
    }
}
