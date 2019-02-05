/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

import hudson.model.Run;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;

/**
 * Action to show a link to {@link TRFReport}s in side menu of projects.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TRFProjectAction extends AbstractTRFAction {

    /**
     * Instantiates a new {@link TRFProjectAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public TRFProjectAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Returns the {@link TRFBuildAction} in the last build that have artifact documents.
     * <p>
     * Resolves the owner by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the last {@link TRFBuildAction} or {@code null} if no proper build exists
     */
    @CheckForNull
    public TRFBuildAction getLastBuildAction(final StaplerRequest req) {
        final Run<?, ?> build = getBuild(req);
        return build != null ? build.getAction(TRFBuildAction.class) : null;
    }

    /**
     * Returns {@link TRFReport} specified by the URL.
     * <p>
     * Delegates to the last {@link TRFBuildAction}.
     *
     * @param token the URL token
     * @param req   the {@link StaplerRequest} used for access this action
     * @return the requested {@link TRFReport} or {@code null} if no proper report exists
     */
    public TRFReport getDynamic(final String token, final StaplerRequest req) {
        final TRFBuildAction buildAction = getLastBuildAction(req);
        return (TRFReport) (buildAction != null ? buildAction.getDynamic(token) : null);
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
        return Messages.TRFProjectAction_DisplayName();
    }
}
