/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.scan;

import hudson.Launcher;

/**
 * Directory scanner searching for ecu.test packages.
 */
public class TestPackageScanner extends AbstractTestScanner {

    /**
     * Defines the package file extension.
     */
    private static final String FILE_EXTENSION = ".pkg";

    /**
     * Instantiates a new {@link TestPackageScanner}.
     *
     * @param inputDir  the input directory
     * @param recursive specifies whether to scan recursively
     * @param launcher  the launcher
     */
    public TestPackageScanner(final String inputDir, final boolean recursive, final Launcher launcher) {
        super(inputDir, recursive, launcher);
    }

    @Override
    protected String getFileExtension() {
        return FILE_EXTENSION;
    }
}
