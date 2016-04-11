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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportAction;

/**
 * Common base class for {@link ATXBuildAction} and {@link ATXProjectAction}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractATXAction extends AbstractReportAction {

    /**
     * Instantiates a new {@link AbstractATXAction}.
     *
     * @param projectLevel
     *            specifies whether archiving is restricted to project level only
     */
    public AbstractATXAction(final boolean projectLevel) {
        super(projectLevel);
    }

    @Override
    public AbstractBuild<?, ?> getLastReportBuild(final AbstractProject<?, ?> project) {
        for (AbstractBuild<?, ?> build = project.getLastBuild(); build != null; build = build
                .getPreviousBuild()) {
            if (build.getAction(ATXBuildAction.class) != null) {
                return build;
            }
        }
        return null;
    }

    @Override
    public String getUrlName() {
        return ATXPublisher.URL_NAME;
    }

    @Override
    public String getIconFileName() {
        return getIconPath("48x48/test-guide.png");
    }

    /**
     * @return the report icon file name
     */
    public String getReportIconFileName() {
        return getIconPath("24x24/atx.png");
    }

    /**
     * @return the trend icon file name
     */
    public String getTrendIconFileName() {
        return getIconPath("24x24/atx-trend.png");
    }
}
