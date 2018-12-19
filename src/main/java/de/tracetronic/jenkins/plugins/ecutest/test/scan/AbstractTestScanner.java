/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.test.scan;

import hudson.Launcher;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Common base class for the {@link TestPackageScanner} and {@link TestProjectScanner}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractTestScanner {

    private final String inputDir;
    private final boolean recursive;
    private final transient Launcher launcher;

    /**
     * Instantiates a {@link AbstractTestScanner}.
     *
     * @param inputDir
     *            the input directory
     * @param recursive
     *            specifies whether to scan recursively
     * @param launcher
     *            the launcher
     */
    public AbstractTestScanner(final String inputDir, final boolean recursive, final Launcher launcher) {
        super();
        this.inputDir = inputDir;
        this.recursive = recursive;
        this.launcher = launcher;
    }

    /**
     * @return the input directory to scan
     */
    public String getInputDir() {
        return inputDir;
    }

    /**
     * @return {@code true} if recursive scan is enabled, {@code false} otherwise.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Scans the test files.
     *
     * @return the test files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    public List<String> scanTestFiles() throws IOException, InterruptedException {
        return launcher.getChannel().call(new ScanTestCallable(inputDir, getFilePattern()));
    }

    /**
     * Gets the file pattern.
     *
     * @return the file pattern
     */
    protected String[] getFilePattern() {
        final String[] filePattern;
        if (isRecursive()) {
            filePattern = new String[] { "**/**" + getFileExtension() };
        } else {
            filePattern = new String[] { "*" + getFileExtension() };
        }
        return filePattern;
    }

    /**
     * Gets the file extension.
     *
     * @return the file extension
     */
    protected abstract String getFileExtension();

    /**
     * {@link Callable} providing remote access to scan a directory with a include file pattern.
     */
    private static final class ScanTestCallable extends MasterToSlaveCallable<List<String>, IOException> {

        private static final long serialVersionUID = 1L;

        private final String inputDir;
        private final String[] filePattern;

        /**
         * Instantiates a new {@link ScanTestCallable}.
         *
         * @param inputDir
         *            the input directory
         * @param filePattern
         *            the file pattern
         */
        ScanTestCallable(final String inputDir, final String[] filePattern) {
            this.inputDir = inputDir;
            this.filePattern = filePattern;
        }

        @Override
        public List<String> call() throws IOException {
            final List<String> includeFiles = new ArrayList<>();
            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(inputDir);
            scanner.setIncludes(filePattern);
            scanner.scan();

            final String[] fileNames = scanner.getIncludedFiles();
            for (final String fileName : fileNames) {
                includeFiles.add(new File(inputDir, fileName).getAbsolutePath());
            }

            return includeFiles;
        }
    }
}
