/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.model.Run;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;

/**
 * Action to show a link to {@link GeneratorReport}s in side menu of projects.
 */
public class ReportGeneratorProjectAction extends AbstractReportGeneratorAction {

    /**
     * Instantiates a new {@link ReportGeneratorProjectAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public ReportGeneratorProjectAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Returns the {@link ReportGeneratorBuildAction} in the last build that have artifact documents.
     * <p>
     * Resolves the owner by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the last {@link ReportGeneratorBuildAction} or {@code null} if no proper build exists
     */
    @CheckForNull
    public ReportGeneratorBuildAction getLastBuildAction(final StaplerRequest req) {
        final Run<?, ?> build = getBuild(req);
        return build != null ? build.getAction(ReportGeneratorBuildAction.class) : null;
    }

    /**
     * Returns {@link GeneratorReport} specified by the URL.
     * <p>
     * Delegates to the last {@link ReportGeneratorBuildAction}.
     *
     * @param token the URL token
     * @param req   the {@link StaplerRequest} used for access this action
     * @return the requested {@link GeneratorReport} or {@code null} if no proper report exists
     */
    public GeneratorReport getDynamic(final String token, final StaplerRequest req) {
        final ReportGeneratorBuildAction buildAction = getLastBuildAction(req);
        return (GeneratorReport) (buildAction != null ? buildAction.getDynamic(token) : null);
    }

    @Override
    public String getIconFileName() {
        if (getBuild(Stapler.getCurrentRequest()) == null) {
            return null;
        }
        return super.getIconFileName();
    }

    @Override
    public String getDisplayName() {
        return Messages.ReportGeneratorProjectAction_DisplayName();
    }
}
