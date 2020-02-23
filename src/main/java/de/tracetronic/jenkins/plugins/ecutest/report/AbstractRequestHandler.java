/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.util.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Common base class providing shared methods to handle {@link StaplerRequest}s.
 */
public abstract class AbstractRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(AbstractRequestHandler.class.getName());

    /**
     * Gets the owner of this action.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the {@link AbstractProject} or {@link AbstractBuild} or {@code null} if no proper owner exists
     */
    @CheckForNull
    public Object getOwner(final StaplerRequest req) {
        final Run<?, ?> build = getAnchestorBuild(req);
        if (build != null) {
            return build;
        }
        return getAnchestorProject(req);
    }

    /**
     * Gets the build that have report artifacts this action handles.
     *
     * <p>If called in a project context, returns the last build that contains report artifacts.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the build with report artifacts to handle or {@code null} if no proper build exists
     */
    @CheckForNull
    public abstract Run<?, ?> getBuild(StaplerRequest req);

    /**
     * Gets the archive target directory for use in {@link #doZipDownload}.
     *
     * @param rootDir the root directory
     * @return the archive target directory
     */
    protected abstract VirtualFile getArchiveTargetDir(File rootDir);

    /**
     * Resolves the build action containing the report artifacts by {@link StaplerRequest#findAncestorObject(Class)}.
     *
     * @param req the {@link StaplerRequest} used for access this report
     * @return the build action with report artifacts to handle or {@code null} if no proper build action exists
     */
    @CheckForNull
    protected AbstractReportAction getBuildAction(final StaplerRequest req) {
        return req.findAncestorObject(AbstractReportAction.class);
    }

    /**
     * Gets the build of this action.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the build containing this action or {@code null} if no proper project exists
     */
    @CheckForNull
    protected Run<?, ?> getAnchestorBuild(final StaplerRequest req) {
        return req.findAncestorObject(Run.class);
    }

    /**
     * Gets the project of this action.
     *
     * @param req the {@link StaplerRequest} used for access this action
     * @return the project containing this action or {@code null} if no proper project exists
     */
    @CheckForNull
    protected Job<?, ?> getAnchestorProject(final StaplerRequest req) {
        return req.findAncestorObject(Job.class);
    }

    /**
     * Serves the compressed contents of the archive directory that is requested via HTTP.
     *
     * @param req the {@link StaplerRequest} used for access this report
     * @param rsp the {@link StaplerResponse} used for serving the file
     * @throws IOException      signals that an I/O exception has occurred
     */
    public void doZipDownload(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        final Run<?, ?> build = getBuild(req);
        final AbstractReportAction action = getBuildAction(req);
        if (build == null || action == null) {
            LOGGER.warning(String.format("No build or related action found for url %s", req.getRequestURI()));
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final boolean isProjectLevel = action.isProjectLevel();
        final File rootDir = isProjectLevel ? build.getParent().getRootDir() : build.getRootDir();
        final VirtualFile archiveDir = getArchiveTargetDir(rootDir);
        if (!archiveDir.exists()) {
            LOGGER.warning(String.format("Archive directory does not exists: %s for %s", archiveDir.getName(),
                build.getFullDisplayName()));
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!archiveDir.isDirectory()) {
            LOGGER.warning(String.format("Archive is not a directory: %s for %s", archiveDir.getName(),
                build.getFullDisplayName()));
            rsp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (req.getDateHeader("If-Modified-Since") >= 0
            && req.getDateHeader("If-Modified-Since") >= archiveDir.lastModified()) {
            rsp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // Compress and download the archive directory
        final String zipFileName = String.format("%s_%s#%d", archiveDir.getName(), build.getParent().getName(),
            build.getNumber());
        rsp.setHeader("Content-Disposition", "attachment;filename=\"" + zipFileName + "\"");
        rsp.setContentType("application/zip");
        zip(rsp.getOutputStream(), archiveDir);
    }

    /**
     * Compresses the given archive directory and serves as download.
     *
     * @param outputStream the output stream
     * @param archiveDir   the archive directory
     * @throws IOException signals that an I/O exception has occurred
     */
    private void zip(final OutputStream outputStream, final VirtualFile archiveDir) throws IOException {
        final ZipOutputStream zos = new ZipOutputStream(outputStream);
        zos.setEncoding(System.getProperty("file.encoding"));

        for (final String archiveFile : archiveDir.list("**/**")) {
            // Convert all backslashes to forward slashes
            final ZipEntry entry = new ZipEntry(archiveFile.replace('\\', '/'));
            final VirtualFile file = archiveDir.child(archiveFile);
            entry.setTime(file.lastModified());
            zos.putNextEntry(entry);

            final InputStream in = file.open();
            try {
                IOUtils.copy(in, zos);
            } finally {
                IOUtils.closeQuietly(in);
            }
            zos.closeEntry();
        }
        zos.close();
    }
}
