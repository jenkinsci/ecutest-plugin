/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import hudson.model.Run;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;

/**
 * Action to show a link to {@link ETLogReport}s in side menu of projects.
 */
public class ETLogProjectAction extends AbstractETLogAction {

    /**
     * Instantiates a new {@link ETLogProjectAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public ETLogProjectAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Returns the {@link ETLogBuildAction} in the last build that have artifact documents.
     *
     * <p>Resolves the owner by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the last {@link ETLogBuildAction} or {@code null} if no proper build exists
     */
    @CheckForNull
    public ETLogBuildAction getLastBuildAction(final StaplerRequest req) {
        final Run<?, ?> build = getBuild(req);
        return build != null ? build.getAction(ETLogBuildAction.class) : null;
    }

    /**
     * Returns {@link ETLogReport} specified by the URL.
     *
     * <p>Delegates to the last {@link ETLogBuildAction}.
     *
     * @param token the URL token
     * @param req   the {@link StaplerRequest} used for access this action
     * @return the requested {@link ETLogReport} or {@code null} if no proper report exists
     */
    public ETLogReport getDynamic(final String token, final StaplerRequest req) {
        final ETLogBuildAction buildAction = getLastBuildAction(req);
        return buildAction != null ? buildAction.getDynamic(token) : null;
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
        return Messages.ETLogProjectAction_DisplayName();
    }
}
