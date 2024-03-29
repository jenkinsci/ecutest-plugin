/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        assertPublisher(publisher, true);
        assertEquals("test.guide", publisher.getAtxName());
    }

    @Test
    public void testNonDefaultStep() {
        final ATXPublisher publisher = new ATXPublisher("test.guide");
        publisher.setAtxInstallation(new ATXInstallation("test.guide", "ecu.test", new ATXConfig()));
        publisher.setFailOnOffline(true);
        publisher.setUsePersistedSettings(true);
        publisher.setInjectBuildVars(true);
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
        assertEquals(!isDefault, publisher.isInjectBuildVars());
        assertEquals(!isDefault, publisher.isAllowMissing());
        assertEquals(!isDefault, publisher.isRunOnFailed());
        assertEquals(isDefault, publisher.isArchiving());
        assertEquals(isDefault, publisher.isKeepAll());
    }
}
