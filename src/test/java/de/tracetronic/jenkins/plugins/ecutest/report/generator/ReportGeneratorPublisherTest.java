/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ReportGeneratorPublisher}.
 */
public class ReportGeneratorPublisherTest {

    @Test
    public void testDefaultStep() {
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("");
        assertPublisher(publisher);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher(null);
        publisher.setGenerators(null);
        publisher.setCustomGenerators(null);
        publisher.setAllowMissing(false);
        publisher.setRunOnFailed(false);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        assertPublisher(publisher);
    }

    @Test
    public void testConstructor() {
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig("HTML", null, true));
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig("Custom", null, false));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setGenerators(generators);
        publisher.setCustomGenerators(customGenerators);
        assertPublisher(publisher);
        assertEquals("ECU-TEST", publisher.getToolName());
        assertThat(publisher.getGenerators(), hasSize(1));
        assertThat(publisher.getCustomGenerators(), hasSize(1));
        assertThat(publisher.getGenerators().get(0).getName(), is("HTML"));
        assertTrue(publisher.getGenerators().get(0).getSettings().isEmpty());
        assertTrue(publisher.getGenerators().get(0).isUsePersistedSettings());
        assertThat(publisher.getCustomGenerators().get(0).getName(), is("Custom"));
        assertTrue(publisher.getCustomGenerators().get(0).getSettings().isEmpty());
    }

    @Test
    public void testEmptyGenerators() {
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig(" ", null, false));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setGenerators(generators);
        assertTrue(publisher.getGenerators().isEmpty());
    }

    @Test
    public void testEmptyCustomGenerators() {
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig(" ", null, false));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setCustomGenerators(customGenerators);
        assertTrue(publisher.getCustomGenerators().isEmpty());
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher the publisher
     */
    private void assertPublisher(final ReportGeneratorPublisher publisher) {
        assertNotNull(publisher);
        assertNotNull(publisher.getToolName());
        assertNotNull(publisher.getGenerators());
        assertNotNull(publisher.getCustomGenerators());
        assertFalse(publisher.isAllowMissing());
        assertFalse(publisher.isRunOnFailed());
        assertTrue(publisher.isArchiving());
        assertTrue(publisher.isKeepAll());
    }
}
