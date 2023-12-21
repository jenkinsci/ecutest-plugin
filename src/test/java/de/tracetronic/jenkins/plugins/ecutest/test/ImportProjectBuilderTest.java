/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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

    @Test
    public void testSetImportConfigs() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        importConfigs.add(new ImportProjectConfig("tmsPath", "importPath", false, "credentialsId", "timeout"));
        final ImportProjectBuilder builder = new ImportProjectBuilder(importConfigs);
        assertThat(builder.getImportConfigs(), hasSize(1));

        builder.setImportConfigs(importConfigs);
        assertThat(builder.getImportConfigs(), hasSize(1));

        builder.setImportConfigs(null);
        assertThat(builder.getImportConfigs(), hasSize(1));

        // Check: If fields of importConfigs and importConfigs2 are the same, they are treated as equal.
        final List<TMSConfig> importConfigs2 = new ArrayList<TMSConfig>();
        importConfigs2.add(new ImportProjectConfig("tmsPath", "importPath", false, "credentialsId", "timeout"));
        builder.setImportConfigs(importConfigs2);
        assertThat(builder.getImportConfigs(), hasSize(1));

        // Check: If fields of importConfigs and importConfigs2 are not the same, they are not treated as equal.
        final List<TMSConfig> importConfigs3 = new ArrayList<TMSConfig>();
        importConfigs3.add(new ImportProjectConfig("tmsPath", "importPath", true, "credentialsId", "timeout"));
        builder.setImportConfigs(importConfigs3);
        assertThat(builder.getImportConfigs(), hasSize(2));
    }
}
