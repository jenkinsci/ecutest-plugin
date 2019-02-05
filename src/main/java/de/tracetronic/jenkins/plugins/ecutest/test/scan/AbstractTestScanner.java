/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
     * @param inputDir  the input directory
     * @param recursive specifies whether to scan recursively
     * @param launcher  the launcher
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
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public List<String> scanTestFiles() throws IOException, InterruptedException {
        return launcher.getChannel().call(new ScanTestCallable(inputDir, getFilePattern()));
    }

    /**
     * Gets the file pattern.
     *
     * @return the file pattern
     */
    protected String getFilePattern() {
        final String filePattern;
        if (isRecursive()) {
            filePattern = "**/**" + getFileExtension();
        } else {
            filePattern = "*" + getFileExtension();
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
        private final String filePattern;

        /**
         * Instantiates a new {@link ScanTestCallable}.
         *
         * @param inputDir    the input directory
         * @param filePattern the file pattern
         */
        ScanTestCallable(final String inputDir, final String filePattern) {
            this.inputDir = inputDir;
            this.filePattern = filePattern;
        }

        @Override
        public List<String> call() throws IOException {
            final List<String> includeFiles = new ArrayList<>();
            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(inputDir);
            scanner.setIncludes(new String[]{filePattern});
            scanner.scan();

            final String[] fileNames = scanner.getIncludedFiles();
            for (final String fileName : fileNames) {
                includeFiles.add(new File(inputDir, fileName).getAbsolutePath());
            }

            return includeFiles;
        }
    }
}
