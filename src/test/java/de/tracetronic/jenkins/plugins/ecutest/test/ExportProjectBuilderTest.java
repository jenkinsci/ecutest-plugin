/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExportProjectBuilder}.
 */
public class ExportProjectBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ExportProjectBuilder builder = new ExportProjectBuilder(null);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @Test
    public void testExportConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectConfig exportConfig = new ExportProjectConfig("test.prj", "export", true, "cred", "600");
        exportConfigs.add(exportConfig);
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat(builder.getExportConfigs().get(0), sameInstance(exportConfig));
    }

    @Test
    public void testExportAttributeConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectAttributeConfig attributeConfig = new ExportProjectAttributeConfig("test.prj", "cred", "600");
        exportConfigs.add(attributeConfig);
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat(builder.getExportConfigs().get(0), sameInstance(attributeConfig));
    }

    @Test
    public void testEmptyExportConfigs() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        exportConfigs.add(new ExportProjectConfig(" ", null, false, null, null));
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }
}
