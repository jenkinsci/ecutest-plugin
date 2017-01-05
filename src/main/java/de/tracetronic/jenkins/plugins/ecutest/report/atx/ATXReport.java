/**
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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import hudson.model.Run;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import jenkins.util.VirtualFile;

import org.apache.commons.lang.NotImplementedException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;

/**
 * Holds a link to ATX report. The build that holds the report artifact is resolved by
 * {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXReport extends AbstractTestReport {

    private static final Logger LOGGER = Logger.getLogger(ATXReport.class.getName());

    private String reportUrl;
    private final boolean hasTrendReport;

    /**
     * Instantiates a new {@link ATXReport}.
     *
     * @param id
     *            the id used in the report URL
     * @param title
     *            the report title
     * @param reportUrl
     *            the report URL
     */
    public ATXReport(final String id, final String title, final String reportUrl) {
        super(id, title);
        this.reportUrl = reportUrl;
        hasTrendReport = false;
    }

    /**
     * Instantiates a new {@link ATXReport}.
     *
     * @param id
     *            the id used in report URL
     * @param title
     *            the report title
     * @param reportUrl
     *            the report URL
     * @param hasTrendReport
     *            specifies whether the report has an additional trend report
     */
    public ATXReport(final String id, final String title, final String reportUrl, final boolean hasTrendReport) {
        super(id, title);
        this.reportUrl = reportUrl;
        this.hasTrendReport = hasTrendReport;
    }

    /**
     * @return the report URL
     */
    public String getReportUrl() {
        return reportUrl;
    }

    /**
     * Sets the report URL.
     *
     * @param reportUrl
     *            the new report URL
     */
    public void setReportUrl(final String reportUrl) {
        this.reportUrl = reportUrl;
    }

    /**
     * Currently only packages can contain an additional trend report.
     *
     * @return {@code true} if has a trend report, {@code false} otherwise
     */
    public boolean hasTrendReport() {
        return hasTrendReport;
    }

    /**
     * Redirects to URL that is requested via HTTP.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this report
     * @param rsp
     *            the {@link StaplerResponse} used for redirecting to the report
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    public void doDynamic(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        final Run<?, ?> build = getBuild(req);
        if (build == null) {
            LOGGER.warning(String.format("No build found for url %s", req.getRequestURI()));
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Redirect to ATX URL
        final URL url = new URL(getReportUrl());
        rsp.sendRedirect(url.toString());
    }

    @Override
    protected VirtualFile getArchiveTargetDir(final File rootDir) {
        throw new NotImplementedException();
    }
}
