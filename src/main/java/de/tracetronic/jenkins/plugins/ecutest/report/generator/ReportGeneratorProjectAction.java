/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.model.Run;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;

/**
 * Action to show a link to {@link GeneratorReport}s in side menu of projects.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
