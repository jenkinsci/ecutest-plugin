/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link ATXPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXPublisherTest {

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ATXPublisher publisher = new ATXPublisher("");
        assertPublisher(publisher, true);
    }

    @Test
    public void testDefaultStep() {
        final ATXPublisher publisher = new ATXPublisher("TEST-GUIDE");
        assertPublisher(publisher, true);
        assertEquals("TEST-GUIDE", publisher.getAtxName());
    }

    @Test
    public void testNonDefaultStep() {
        final ATXPublisher publisher = new ATXPublisher("TEST-GUIDE");
        publisher.setAtxInstallation(new ATXInstallation("TEST-GUIDE", "ECU-TEST", new ATXConfig()));
        publisher.setFailOnOffline(true);
        publisher.setUsePersistedSettings(true);
        publisher.setAllowMissing(true);
        publisher.setRunOnFailed(true);
        publisher.setArchiving(false);
        publisher.setKeepAll(false);
        assertPublisher(publisher, false);
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher the publisher
     * @param isDefault specifies whether to check default values
     */
    private void assertPublisher(final ATXPublisher publisher, final boolean isDefault) {
        assertNotNull(publisher);
        assertNotNull(publisher.getAtxName());
        assertEquals(isDefault, publisher.getAtxInstallation() == null);
        assertEquals(!isDefault, publisher.isFailOnOffline());
        assertEquals(!isDefault, publisher.isUsePersistedSettings());
        assertEquals(!isDefault, publisher.isAllowMissing());
        assertEquals(!isDefault, publisher.isRunOnFailed());
        assertEquals(isDefault, publisher.isArchiving());
        assertEquals(isDefault, publisher.isKeepAll());
    }
}
