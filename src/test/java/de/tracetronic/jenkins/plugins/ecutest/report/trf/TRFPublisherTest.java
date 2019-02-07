/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TRFPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TRFPublisherTest {

    @Test
    public void testConstructorStep() {
        final TRFPublisher publisher = new TRFPublisher();
        publisher.setAllowMissing(true);
        publisher.setRunOnFailed(true);
        publisher.setArchiving(false);
        publisher.setKeepAll(false);
        assertPublisher(publisher);
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher the publisher
     */
    private void assertPublisher(final TRFPublisher publisher) {
        assertNotNull(publisher);
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
        assertFalse(publisher.isArchiving());
        assertFalse(publisher.isKeepAll());
    }
}
