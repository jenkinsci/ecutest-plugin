/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Run;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;

/**
 * Action to show a link to {@link ATXReport}s in side menu of projects.
 *
 * @param <T> the report type, either {@link ATXReport} or {@link ATXZipReport}
*/
public class ATXProjectAction<T extends AbstractTestReport> extends AbstractATXAction {

    /**
     * Instantiates a new {@link ATXProjectAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public ATXProjectAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Returns the {@link ATXBuildAction} in the last build that have artifact documents.
     *
     * <p>
     * Resolves the owner by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the last {@link ATXBuildAction} or {@code null} if no proper build exists
     */
    @SuppressWarnings("unchecked")
    @CheckForNull
    public ATXBuildAction<T> getLastBuildAction(final StaplerRequest req) {
        final Run<?, ?> build = getBuild(req);
        return build != null ? build.getAction(ATXBuildAction.class) : null;
    }

    /**
     * Returns {@link ATXReport} specified by the URL.
     *
     * <p>Delegates to the last {@link ATXBuildAction}.
     *
     * @param token the URL token
     * @param req   the {@link StaplerRequest} used for access this action
     * @return the requested {@link ATXReport} or {@code null} if no proper report exists
     */
    public T getDynamic(final String token, final StaplerRequest req) {
        final ATXBuildAction<T> buildAction = getLastBuildAction(req);
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
        return Messages.ATXProjectAction_DisplayName();
    }
}
