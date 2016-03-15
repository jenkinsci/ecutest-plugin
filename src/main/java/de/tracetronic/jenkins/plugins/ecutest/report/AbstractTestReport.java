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

import hudson.Util;
import hudson.model.ModelObject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Common base class for several test reports created by {@link AbstractReportPublisher}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractTestReport implements ModelObject {

    private final String id;
    private final String title;
    private final List<AbstractTestReport> subReports;

    /**
     * Instantiates a new {@link AbstractTestReport}.
     *
     * @param id
     *            the id used in report URL
     * @param title
     *            the report title
     */
    public AbstractTestReport(final String id, final String title) {
        super();
        this.id = id;
        this.title = title;
        subReports = new ArrayList<AbstractTestReport>();
    }

    /**
     * @return the report identifier used in URL
     */
    public String getId() {
        return id;
    }

    /**
     * @return the report title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the sub-reports.
     *
     * @return the subReports
     */
    public List<AbstractTestReport> getSubReports() {
        return subReports;
    }

    /**
     * Adds a sub-report.
     *
     * @param subReport
     *            the subReport to add
     */
    public void addSubReport(final AbstractTestReport subReport) {
        subReports.add(subReport);
    }

    /**
     * Adds a bundle of sub-reports.
     *
     * @param subReports
     *            the subReports to add
     */
    public void addSubReports(final List<AbstractTestReport> subReports) {
        this.subReports.addAll(subReports);
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    /**
     * @return the URL for the report path
     */
    public String getUrl() {
        return Util.rawEncode(getId());
    }

    /**
     * Resolves the build containing the report artifacts by {@link StaplerRequest#findAncestorObject(Class)}.
     * <p>
     * If called in a project context, returns the last build that contains report artifacts.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this report
     * @return the build with report artifacts to handle or {@code null} if no proper build exists
     */
    @CheckForNull
    protected AbstractBuild<?, ?> getBuild(final StaplerRequest req) {
        final AbstractBuild<?, ?> build = req.findAncestorObject(AbstractBuild.class);
        if (build != null) {
            return build;
        }

        final AbstractProject<?, ?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return project.getLastSuccessfulBuild();
        }

        return null;
    }
}
