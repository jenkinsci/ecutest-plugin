/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractArchiveFileReport;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Holds a link to the zipped ATX report if ATX upload is disabled.
 * The build that holds the report artifact is resolved by {@link StaplerRequest#findAncestorObject(Class)} at runtime.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXZipReport extends AbstractArchiveFileReport {

    /**
     * Instantiates a new {@link ATXZipReport}.
     *
     * @param id       the id used in the report URL
     * @param title    the report title
     * @param fileName the file name
     * @param fileSize the report file size
     */
    public ATXZipReport(final String id, final String title, final String fileName, final long fileSize) {
        super(id, title, fileName, fileSize);
    }

    @Override
    public String getArchiveDir() {
        return ATXPublisher.URL_NAME;
    }
}
