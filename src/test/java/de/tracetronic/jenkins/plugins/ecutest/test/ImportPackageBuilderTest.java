/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
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
 * Unit tests for {@link ImportPackageBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportPackageBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportPackageBuilder builder = new ImportPackageBuilder(importConfigs);
        assertTrue(builder.getImportConfigs().isEmpty());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ImportPackageBuilder builder = new ImportPackageBuilder(null);
        assertTrue(builder.getImportConfigs().isEmpty());
    }

    @Test
    public void testImportConfig() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportPackageConfig importConfig = new ImportPackageConfig("import", "import", "cred", "600");
        importConfigs.add(importConfig);
        final ImportPackageBuilder builder = new ImportPackageBuilder(importConfigs);
        assertThat(builder.getImportConfigs(), hasSize(1));
        assertThat(builder.getImportConfigs().get(0), sameInstance(importConfig));
    }

    @Test
    public void testImportAttributeConfig() {
        final List<TMSConfig> importConfigs = new ArrayList<TMSConfig>();
        final ImportPackageAttributeConfig attributeConfig = new ImportPackageAttributeConfig("test.pkg", "cred", "600");
        importConfigs.add(attributeConfig);
        final ImportPackageBuilder builder = new ImportPackageBuilder(importConfigs);
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
