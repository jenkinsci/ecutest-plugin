/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ImportProjectArchiveConfig}.
 */
public class ImportProjectArchiveConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportProjectArchiveConfig config = new ImportProjectArchiveConfig(null, null, null, false);
        assertThat(config.getTmsPath(), is(""));
        assertThat(config.getImportPath(), is(""));
        assertThat(config.getImportConfigPath(), is(""));
        assertFalse(config.isReplaceFiles());
    }

    @Test
    public void testExpand() {
        final ImportProjectArchiveConfig config = new ImportProjectArchiveConfig("${ARCHIVE_PATH}", "${IMPORT_PATH}",
            "${IMPORT_CONFIG_PATH}", true);
        final EnvVars envVars = new EnvVars();
        envVars.put("ARCHIVE_PATH", "test.prz");
        envVars.put("IMPORT_PATH", "import");
        envVars.put("IMPORT_CONFIG_PATH", "import");
        final ImportProjectArchiveConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTmsPath(), is("test.prz"));
        assertThat(expConfig.getImportPath(), is("import"));
        assertThat(expConfig.getImportConfigPath(), is("import"));
        assertTrue(config.isReplaceFiles());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportProjectArchiveConfig.class).withRedefinedSuperclass().verify();
    }
}
