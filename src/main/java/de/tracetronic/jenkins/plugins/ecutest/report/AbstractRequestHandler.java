/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Common base class providing shared methods to handle {@link StaplerRequest}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractRequestHandler {

    /**
     * Gets the owner of this action.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this action
     * @return the {@link AbstractProject} or {@link AbstractBuild} or {@code null} if no proper owner exists
     */
    @CheckForNull
    public Object getOwner(final StaplerRequest req) {
        final AbstractBuild<?, ?> build = getAnchestorBuild(req);
        if (build != null) {
            return build;
        }

        final AbstractProject<?, ?> project = getAnchestorProject(req);
        if (project != null) {
            return project;
        }

        return null;
    }

    /**
     * Gets the build that have report artifacts this action handles.
     * <p>
     * If called in a project context, returns the last build that contains report artifacts.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this action
     * @return the build with report artifacts to handle or {@code null} if no proper build exists
     */
    @CheckForNull
    public abstract AbstractBuild<?, ?> getBuild(final StaplerRequest req);

    /**
     * Resolves the build action containing the report artifacts by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this report
     * @return the build action with report artifacts to handle or {@code null} if no proper build action exists
     */
    @CheckForNull
    protected AbstractReportAction getBuildAction(final StaplerRequest req) {
        return req.findAncestorObject(AbstractReportAction.class);
    }

    /**
     * Gets the build of this action.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this action
     * @return the build containing this action or {@code null} if no proper project exists
     */
    @CheckForNull
    protected AbstractBuild<?, ?> getAnchestorBuild(final StaplerRequest req) {
        return req.findAncestorObject(AbstractBuild.class);
    }

    /**
     * Gets the project of this action.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this action
     * @return the project containing this action or {@code null} if no proper project exists
     */
    @CheckForNull
    protected AbstractProject<?, ?> getAnchestorProject(final StaplerRequest req) {
        return req.findAncestorObject(AbstractProject.class);
    }
}
