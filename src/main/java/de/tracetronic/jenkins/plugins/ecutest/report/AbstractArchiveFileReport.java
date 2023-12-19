/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogReport;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFReport;
import hudson.model.Run;
import jenkins.util.VirtualFile;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Common base class for {@link TRFReport} and {@link ETLogReport} holding the archive file information.
 * The build that holds the artifact is resolved by {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 */
public abstract class AbstractArchiveFileReport extends AbstractTestReport {

    private static final Logger LOGGER = Logger.getLogger(AbstractArchiveFileReport.class.getName());

    private final String fileName;
    private final long fileSize;

    /**
     * Instantiates a new {@link AbstractArchiveFileReport}.
     *
     * @param id       the id used in the report URL
     * @param title    the report title
     * @param fileName the log file name
     * @param fileSize the log file size
     */
    public AbstractArchiveFileReport(final String id, final String title, final String fileName,
                                     final long fileSize) {
        super(id, title);
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

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
     * @param req the {@link StaplerRequest} used for access this report
     * @param rsp the {@link StaplerResponse} used for serving the file
     * @throws IOException      signals that an I/O exception has occurred
     * @throws ServletException if serving the file failed
     */
    public void doDynamic(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        final Run<?, ?> build = getBuild(req);
        final AbstractReportAction action = getBuildAction(req);
        if (build == null || action == null) {
            LOGGER.warning(String.format("No build or related action found for url %s", req.getRequestURI()));
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final boolean isProjectLevel = action.isProjectLevel();
        final File rootDir = isProjectLevel ? build.getParent().getRootDir() : build.getRootDir();
        final File archiveFile = new File(new File(rootDir, getArchiveDir()), getFileName());
        if (!archiveFile.exists()) {
            LOGGER.warning(String.format("Archive file does not exist: %s for %s", getFileName(),
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
