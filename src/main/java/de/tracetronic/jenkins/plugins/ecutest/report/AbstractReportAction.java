/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.AbstractATXAction;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.AbstractTRFAction;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.util.VirtualFile;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import java.io.File;

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
     * @param projectLevel specifies whether archiving is restricted to project level only
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
        final String urlName = getUrlName();
        if (urlName != null) {
            return VirtualFile.forFile(new File(rootDir, urlName));
        } else {
            return VirtualFile.forFile(rootDir);
        }
    }

    /**
     * Gets the last build with report artifacts in a project.
     *
     * @param project the project
     * @return the last build with report artifacts or {@code null} if no proper build exists
     */
    @CheckForNull
    protected abstract Run<?, ?> getLastReportBuild(Job<?, ?> project);

    @Override
    public String getIconFileName() {
        return ETPlugin.getIconFileName(getIconClassName(), "icon-xlg");
    }
}
