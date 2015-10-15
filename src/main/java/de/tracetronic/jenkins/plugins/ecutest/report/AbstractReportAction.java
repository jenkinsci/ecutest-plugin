/**
 * Copyright (c) 2015 TraceTronic GmbH
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

import org.kohsuke.stapler.StaplerRequest;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.AbstractATXAction;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.AbstractTRFAction;

/**
 * Common base class for {@link AbstractATXAction} and {@link AbstractTRFAction}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractReportAction implements Action {

    private static final Logger LOGGER = Logger.getLogger(AbstractReportAction.class.getName());

    /**
     * Gets the owner of this action.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this action
     * @return the {@link AbstractProject} or {@link AbstractBuild} or null if no proper owner exists
     */
    @CheckForNull
    public Object getOwner(final StaplerRequest req) {
        final AbstractBuild<?, ?> build = req.findAncestorObject(AbstractBuild.class);
        if (build != null) {
            return build;
        }

        final AbstractProject<?, ?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return project;
        }

        return null;
    }

    /**
     * Gets the project of this action.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this action
     * @return the project containing this action or null if no proper project exists
     */
    @CheckForNull
    public AbstractProject<?, ?> getProject(final StaplerRequest req) {
        final AbstractProject<?, ?> project = req.findAncestorObject(AbstractProject.class);
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
     * @return the build with report artifacts to handle or null if no proper build exists
     */
    @CheckForNull
    public AbstractBuild<?, ?> getBuild(final StaplerRequest req) {
        final AbstractBuild<?, ?> build = req.findAncestorObject(AbstractBuild.class);
        if (build != null) {
            return build;
        }

        final AbstractProject<?, ?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return getLastReportBuild(project);
        }

        return null;
    }

    /**
     * Gets the last build with report artifacts in a project.
     *
     * @param project
     *            the project
     * @return the last build with report artifacts or null if no proper build exists
     */
    @CheckForNull
    protected abstract AbstractBuild<?, ?> getLastReportBuild(final AbstractProject<?, ?> project);

    /**
     * Gets the icon path inside of this plugin.
     *
     * @param icon
     *            the icon to search
     * @return the full icon path or null if the icon does not exist
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
