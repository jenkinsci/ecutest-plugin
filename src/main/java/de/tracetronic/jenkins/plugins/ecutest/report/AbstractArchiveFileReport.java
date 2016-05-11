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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jenkins.util.VirtualFile;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogReport;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFReport;

/**
 * Common base class for {@link TRFReport} and {@link ETLogReport} holding the archive file information.
 * The build that holds the artifact is resolved by {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractArchiveFileReport extends AbstractTestReport {

    private static final Logger LOGGER = Logger.getLogger(AbstractArchiveFileReport.class.getName());

    private final String fileName;
    private final long fileSize;

    /**
     * Instantiates a new {@link AbstractArchiveFileReport}.
     *
     * @param id
     *            the id used in the report URL
     * @param title
     *            the report title
     * @param fileName
     *            the log file name
     * @param fileSize
     *            the log file size
     */
    public AbstractArchiveFileReport(final String id, final String title, final String fileName,
            final long fileSize) {
        super(id, title);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    /**
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the file size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the archive directory containing the stored files.
     *
     * @return the archive directory
     */
    public abstract String getArchiveDir();

    @Override
    protected VirtualFile getArchiveTargetDir(final File rootDir) {
        return VirtualFile.forFile(new File(new File(rootDir, getArchiveDir()), getFileName()));
    }

    /**
     * Send contents of the archive file that is requested via HTTP.
     *
     * @param req
     *            the {@link StaplerRequest} used for access this report
     * @param rsp
     *            the {@link StaplerResponse} used for serving the file
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws ServletException
     *             if serving the file failed
     */
    public void doDynamic(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        final AbstractBuild<?, ?> build = getBuild(req);
        final AbstractReportAction action = getBuildAction(req);
        if (build == null || action == null) {
            LOGGER.warning(String.format("No build or related action found for url %s", req.getRequestURI()));
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final boolean isProjectLevel = action.isProjectLevel();
        final File rootDir = isProjectLevel ? build.getProject().getRootDir() : build.getRootDir();
        final File archiveFile = new File(new File(rootDir, getArchiveDir()), getFileName());
        if (!archiveFile.exists()) {
            LOGGER.warning(String.format("Archive file does not exists: %s for %s", getFileName(),
                    build.getFullDisplayName()));
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!archiveFile.isFile()) {
            LOGGER.warning(String.format("Archive file is not a file: %s for %s", getFileName(),
                    build.getFullDisplayName()));
            rsp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (req.getDateHeader("If-Modified-Since") >= 0
                && req.getDateHeader("If-Modified-Since") >= archiveFile.lastModified()) {
            rsp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // Download the archive file
        rsp.setHeader("Content-Disposition", "attachment;filename=\"" + archiveFile.getName() + "\"");
        rsp.serveFile(req, archiveFile.toURI().toURL());
    }
}
