/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import java.io.File;

import javax.annotation.CheckForNull;

import jenkins.util.VirtualFile;

import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.stapler.StaplerRequest;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.AbstractATXAction;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.AbstractTRFAction;

/**
 * Common base class for {@link AbstractATXAction} and {@link AbstractTRFAction}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractReportAction extends AbstractRequestHandler implements Action, IconSpec {

    private final boolean projectLevel;

    /**
     * Instantiates a new {@link AbstractReportAction}.
     *
     * @param projectLevel
     *            specifies whether archiving is restricted to project level only
     */
    public AbstractReportAction(final boolean projectLevel) {
        super();
        this.projectLevel = projectLevel;
    }

    /**
     * Returns whether archiving is restricted to project level only.
     *
     * @return {@code true} if archiving is restricted to project level, {@code false} otherwise
     */
    public boolean isProjectLevel() {
        return projectLevel;
    }

    @Override
    public Run<?, ?> getBuild(final StaplerRequest req) {
        final Run<?, ?> build = getAnchestorBuild(req);
        if (build != null) {
            return build;
        }

        final Job<?, ?> project = getAnchestorProject(req);
        if (project != null) {
            return getLastReportBuild(project);
        }

        return null;
    }

    @Override
    protected VirtualFile getArchiveTargetDir(final File rootDir) {
        return VirtualFile.forFile(new File(rootDir, getUrlName()));
    }

    /**
     * Gets the last build with report artifacts in a project.
     *
     * @param project
     *            the project
     * @return the last build with report artifacts or {@code null} if no proper build exists
     */
    @CheckForNull
    protected abstract Run<?, ?> getLastReportBuild(Job<?, ?> project);

    @Override
    public String getIconFileName() {
        return ETPlugin.getIconFileName(getIconClassName(), "icon-xlg");
    }
}
