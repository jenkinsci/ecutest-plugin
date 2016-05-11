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

import hudson.PluginWrapper;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;
import jenkins.util.VirtualFile;

import org.kohsuke.stapler.StaplerRequest;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.AbstractATXAction;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.AbstractTRFAction;

/**
 * Common base class for {@link AbstractATXAction} and {@link AbstractTRFAction}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractReportAction extends AbstractRequestHandler implements Action {

    private static final Logger LOGGER = Logger.getLogger(AbstractReportAction.class.getName());

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
    public AbstractBuild<?, ?> getBuild(final StaplerRequest req) {
        final AbstractBuild<?, ?> build = getAnchestorBuild(req);
        if (build != null) {
            return build;
        }

        final AbstractProject<?, ?> project = getAnchestorProject(req);
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
    protected abstract AbstractBuild<?, ?> getLastReportBuild(final AbstractProject<?, ?> project);

    /**
     * Gets the icon path inside of this plugin.
     *
     * @param icon
     *            the icon to search
     * @return the full icon path or {@code null} if the icon does not exist
     */
    @CheckForNull
    protected String getIconPath(final String icon) {
        String iconPath = null;
        if (icon == null) {
            return iconPath;
        }
        // Try plugin icons dir, fallback to Jenkins image
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            final PluginWrapper wrapper = instance.getPluginManager().getPlugin(ETPlugin.class);
            boolean pluginIconExists = false;
            try {
                pluginIconExists = wrapper != null
                        && new File(wrapper.baseResourceURL.toURI().getPath() + "/icons/" + icon).exists();
            } catch (final URISyntaxException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
            if (pluginIconExists) {
                iconPath = "/plugin/" + wrapper.getShortName() + "/icons/" + icon;
            } else {
                iconPath = "/images/48x48/document.png";
            }
        }
        return iconPath;
    }
}
