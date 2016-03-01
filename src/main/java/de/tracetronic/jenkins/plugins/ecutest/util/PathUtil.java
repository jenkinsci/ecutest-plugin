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
package de.tracetronic.jenkins.plugins.ecutest.util;

import hudson.FilePath;

import java.io.File;
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
     * @param path
     *            the path to make absolute
     * @param relativeTo
     *            the relative path
     * @return the absolute path
     */
    public static String makeAbsolutePath(final String path, final FilePath relativeTo) {
        final File wsFile = new File(path);
        if (!wsFile.isAbsolute()) {
            final FilePath wsDir = new FilePath(relativeTo, path);
            return wsDir.getRemote();
        }
        return path;
    }

    /**
     * Copies a file from source to destination which can be on remote.
     *
     * @param src
     *            the source file
     * @param dest
     *            the destination file
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    public static boolean copyRemoteFile(final FilePath src, final FilePath dest) throws IOException,
            InterruptedException {
        if (dest != null && !dest.exists()) {
            if (src != null && src.exists()) {
                try {
                    LOGGER.log(Level.INFO, String.format("Copy %s to %s", src.getRemote(), dest.getRemote()));
                    src.copyTo(dest);
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    return false;
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    return false;
                }
            } else {
                LOGGER.log(Level.SEVERE, String.format("Source %s does not exist!", src.getRemote()));
                return false;
            }
        }
        return true;
    }
}
