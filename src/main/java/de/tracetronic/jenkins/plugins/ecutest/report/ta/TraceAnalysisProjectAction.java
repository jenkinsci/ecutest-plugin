/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import hudson.model.Run;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;

/**
 * Action to show a link to {@link TraceAnalysisReport}s in side menu of projects.
 */
public class TraceAnalysisProjectAction extends AbstractTraceAnalysisAction {

    /**
     * Instantiates a new {@link TraceAnalysisProjectAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public TraceAnalysisProjectAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Returns the {@link TraceAnalysisBuildAction} in the last build that have artifact documents.
     *
     * <p>Resolves the owner by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the last {@link TraceAnalysisBuildAction} or {@code null} if no proper build exists
     */
    @CheckForNull
    public TraceAnalysisBuildAction getLastBuildAction(final StaplerRequest req) {
        final Run<?, ?> build = getBuild(req);
        return build != null ? build.getAction(TraceAnalysisBuildAction.class) : null;
    }

    /**
     * Returns {@link TraceAnalysisReport} specified by the URL.
     *
     * <p>Delegates to the last {@link TraceAnalysisBuildAction}.
     *
     * @param token the URL token
     * @param req   the {@link StaplerRequest} used for access this action
     * @return the requested {@link TraceAnalysisReport} or {@code null} if no proper report exists
     */
    public TraceAnalysisReport getDynamic(final String token, final StaplerRequest req) {
        final TraceAnalysisBuildAction buildAction = getLastBuildAction(req);
        return (TraceAnalysisReport) (buildAction != null ? buildAction.getDynamic(token) : null);
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
        return Messages.TraceAnalysisProjectAction_DisplayName();
    }
}
