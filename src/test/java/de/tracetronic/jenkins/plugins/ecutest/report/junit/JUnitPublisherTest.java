/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link JUnitPublisher}.
 */
public class JUnitPublisherTest {

    @Test
    public void testDefaultStep() throws IOException {
        final JUnitPublisher publisher = new JUnitPublisher("");
        assertPublisher(publisher);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final JUnitPublisher publisher = new JUnitPublisher(null);
        publisher.setUnstableThreshold(0);
        publisher.setFailedThreshold(0);
        publisher.setAllowMissing(false);
        publisher.setRunOnFailed(false);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        assertPublisher(publisher);
    }

    @Test
    public void testNegativeThresholds() {
        final JUnitPublisher publisher = new JUnitPublisher("");
        publisher.setUnstableThreshold(-1);
        publisher.setFailedThreshold(-1);
        assertEquals(0, Double.compare(0, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(0, publisher.getFailedThreshold()));
    }

    @Test
    public void testInvalidThresholds() {
        final JUnitPublisher publisher = new JUnitPublisher("");
        publisher.setUnstableThreshold(101);
        publisher.setFailedThreshold(101);
        assertEquals(0, Double.compare(100, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(100, publisher.getFailedThreshold()));
    }

    @Test
    public void testFailedPercentage() {
        assertEquals(0, Double.compare(0, JUnitPublisher.getFailedPercentage(0, 0)));
        assertEquals(0, Double.compare(0, JUnitPublisher.getFailedPercentage(0, 100)));
        assertEquals(0, Double.compare(50.0, JUnitPublisher.getFailedPercentage(50, 100)));
        assertEquals(0, Double.compare(100, JUnitPublisher.getFailedPercentage(100, 100)));
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher the publisher
     */
    private void assertPublisher(final JUnitPublisher publisher) {
        assertNotNull(publisher);
        assertNotNull(publisher.getToolName());
        assertEquals(0, Double.compare(0, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(0, publisher.getFailedThreshold()));
        assertFalse(publisher.isAllowMissing());
        assertFalse(publisher.isRunOnFailed());
        assertTrue(publisher.isArchiving());
        assertTrue(publisher.isKeepAll());
    }
}
