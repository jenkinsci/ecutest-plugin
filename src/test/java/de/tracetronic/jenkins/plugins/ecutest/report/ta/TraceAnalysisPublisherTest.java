/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TraceAnalysisPublisher}.
 */
public class TraceAnalysisPublisherTest {

    @Test
    public void testDefaultStep() throws IOException {
        final TraceAnalysisPublisher publisher = new TraceAnalysisPublisher("");
        assertPublisher(publisher);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final TraceAnalysisPublisher publisher = new TraceAnalysisPublisher(null);
        publisher.setTimeout(null);
        publisher.setAllowMissing(false);
        publisher.setRunOnFailed(false);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        publisher.setMergeReports(true);
        publisher.setCreateReportDir(false);
        assertPublisher(publisher);
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher the publisher
     */
    private void assertPublisher(final TraceAnalysisPublisher publisher) {
        assertNotNull(publisher);
        assertNotNull(publisher.getToolName());
        assertNotNull(publisher.getTimeout());
        assertEquals(String.valueOf(TraceAnalysisPublisher.getDefaultTimeout()), publisher.getTimeout());
        assertFalse(publisher.isAllowMissing());
        assertFalse(publisher.isRunOnFailed());
        assertTrue(publisher.isArchiving());
        assertTrue(publisher.isKeepAll());
        assertTrue(publisher.isMergeReports());
        assertFalse(publisher.isCreateReportDir());
    }
}
