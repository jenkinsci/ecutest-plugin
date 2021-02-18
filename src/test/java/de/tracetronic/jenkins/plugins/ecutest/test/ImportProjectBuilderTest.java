/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
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
 * Unit tests for {@link ImportProjectBuilder}.
 */
public class ImportProjectBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectBuilder builder = new ImportProjectBuilder(importConfigs);
        assertTrue(builder.getImportConfigs().isEmpty());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ImportProjectBuilder builder = new ImportProjectBuilder(null);
        assertTrue(builder.getImportConfigs().isEmpty());
    }

    @Test
    public void testImportConfig() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectConfig importConfig = new ImportProjectConfig("import", "import", true, "cred", "600");
        importConfigs.add(importConfig);
        final ImportProjectBuilder builder = new ImportProjectBuilder(importConfigs);
        assertThat(builder.getImportConfigs(), hasSize(1));
        assertThat(builder.getImportConfigs().get(0), sameInstance(importConfig));
    }

    @Test
    public void testImportArchiveConfig() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectArchiveConfig archiveConfig = new ImportProjectArchiveConfig("test.prz", "import", "import",
            true);
        importConfigs.add(archiveConfig);
        final ImportProjectBuilder builder = new ImportProjectBuilder(importConfigs);
        assertThat(builder.getImportConfigs(), hasSize(1));
        assertThat(builder.getImportConfigs().get(0), sameInstance(archiveConfig));
    }

    @Test
    public void testImportAttributeConfig() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportProjectAttributeConfig attributeConfig = new ImportProjectAttributeConfig("test.prj", "cred", "600");
        importConfigs.add(attributeConfig);
        final ImportProjectBuilder builder = new ImportProjectBuilder(importConfigs);
        assertThat(builder.getImportConfigs(), hasSize(1));
        assertThat(builder.getImportConfigs().get(0), sameInstance(attributeConfig));
    }

    @Test
    public void testEmptyImportConfigs() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        importConfigs.add(new ImportProjectArchiveConfig(" ", null, null, false));
        final ImportProjectBuilder builder = new ImportProjectBuilder(importConfigs);
        assertTrue(builder.getImportConfigs().isEmpty());
    }
}
