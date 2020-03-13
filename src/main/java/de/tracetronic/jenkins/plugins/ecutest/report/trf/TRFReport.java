/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractArchiveFileReport;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Holds a link to the TRF report. The build that holds the artifact is resolved by
 * {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 */
public class TRFReport extends AbstractArchiveFileReport {

    /**
     * Instantiates a new {@link TRFReport}.
     *
     * @param id       the id used in the report URL
     * @param title    the report title
     * @param fileName the file name
     * @param fileSize the report file size
     */
    public TRFReport(final String id, final String title, final String fileName, final long fileSize) {
        super(id, title, fileName, fileSize);
    }

    @Override
    public String getArchiveDir() {
        return TRFPublisher.URL_NAME;
    }
}
