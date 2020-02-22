/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExportPackageBuilder}.
 */
public class ExportPackageBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ExportPackageBuilder builder = new ExportPackageBuilder(null);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @Test
    public void testExportConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageConfig exportConfig = new ExportPackageConfig("test.pkg", "export", true, "cred", "600");
        exportConfigs.add(exportConfig);
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat(builder.getExportConfigs().get(0), sameInstance(exportConfig));
    }

    @Test
    public void testExportAttributeConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageAttributeConfig attributeConfig = new ExportPackageAttributeConfig("test.pkg", "cred", "600");
        exportConfigs.add(attributeConfig);
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat(builder.getExportConfigs().get(0), sameInstance(attributeConfig));
    }

    @Test
    public void testEmptyExportConfigs() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        exportConfigs.add(new ExportPackageConfig(" ", null, false, null, null));
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }
}
