/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link DownStreamPublisher}.
 */
public class DownStreamPublisherTest {

    @Test
    public void testDefaultStep() {
        final DownStreamPublisher publisher = new DownStreamPublisher("", "");
        assertNullPublisher(publisher);
        assertEquals("", publisher.getWorkspace());
        assertEquals("", publisher.getReportDir());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final DownStreamPublisher publisher = new DownStreamPublisher(null, null);
        assertNullPublisher(publisher);
        assertDefaultPublisher(publisher);
    }

    /**
     * Asserts the publisher null properties.
     *
     * @param publisher the publisher
     */
    private void assertNullPublisher(final DownStreamPublisher publisher) {
        assertNotNull(publisher);
        assertNotNull(publisher.getPublishers());
        assertNotNull(publisher.getWorkspace());
        assertNotNull(publisher.getReportDir());
    }

    /**
     * Asserts the publisher default properties.
     *
     * @param publisher the publisher
     */
    private void assertDefaultPublisher(final DownStreamPublisher publisher) {
        assertEquals(DownStreamPublisher.getDefaultReportDir(), publisher.getReportDir());
        assertEquals("", publisher.getWorkspace());
    }
}
