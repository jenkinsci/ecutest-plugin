/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import hudson.FilePath;
import hudson.util.IOUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing file path operations.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public final class PathUtil {

    private static final Logger LOGGER = Logger.getLogger(PathUtil.class.getName());

    /**
     * Instantiates a new {@link PathUtil}.
     */
    private PathUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Builds an absolute path by given relative path.
     *
     * @param path       the path to make absolute
     * @param relativeTo the relative path
     * @return the absolute path
     */
    public static String makeAbsolutePath(final String path, final FilePath relativeTo) {
        if (!IOUtils.isAbsolute(path)) {
            final FilePath wsDir = new FilePath(relativeTo, path);
            return wsDir.getRemote();
        }
        return path;
    }

    /**
     * Copies a file from source to destination which can be on remote.
     *
     * @param src  the source file
     * @param dest the destination file
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static boolean copyRemoteFile(final FilePath src, final FilePath dest) throws IOException,
        InterruptedException {
        if (dest != null && !dest.exists()) {
            if (src != null && src.exists()) {
                try {
                    LOGGER.log(Level.INFO, String.format("Copy %s to %s", src.getRemote(), dest.getRemote()));
                    src.copyTo(dest);
                } catch (final IOException | InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    return false;
                }
            } else {
                LOGGER.log(Level.SEVERE, String.format("Source %s does not exist!",
                    src != null ? src.getRemote() : ""));
                return false;
            }
        }
        return true;
    }
}
