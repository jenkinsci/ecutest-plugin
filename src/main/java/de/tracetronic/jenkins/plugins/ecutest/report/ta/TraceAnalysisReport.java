/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractArchiveFileReport;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Holds a link to the trace analysis report. The build that holds the artifact is resolved by
 * {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 */
public class TraceAnalysisReport extends AbstractArchiveFileReport {

    /**
     * Instantiates a new {@link TraceAnalysisReport}.
     *
     * @param id       the id used in the report URL
     * @param title    the report title
     * @param fileName the file name
     * @param fileSize the report file size
     */
    public TraceAnalysisReport(final String id, final String title, final String fileName, final long fileSize) {
        super(id, title, fileName, fileSize);
    }

    @Override
    public String getArchiveDir() {
        return TraceAnalysisPublisher.URL_NAME;
    }
}
