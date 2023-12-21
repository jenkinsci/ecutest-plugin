/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractArchiveFileReport;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Holds a link to the generated report. The build that holds the artifact is resolved by
 * {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 */
public class GeneratorReport extends AbstractArchiveFileReport {

    /**
     * Instantiates a new {@link GeneratorReport}.
     *
     * @param id       the id used in the report URL
     * @param title    the report title
     * @param fileName the file name
     * @param fileSize the report file size
     */
    public GeneratorReport(final String id, final String title, final String fileName, final long fileSize) {
        super(id, title, fileName, fileSize);
    }

    @Override
    public String getArchiveDir() {
        return ReportGeneratorPublisher.URL_NAME;
    }
}
