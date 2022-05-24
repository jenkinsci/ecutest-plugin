/*
 * Copyright (c) 2015-2022 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ImportProjectClientTest {

    @Test
    public void testBlankConstructor() {
        TMSConfig config = new ImportProjectConfig("", "", false, "", "");
        ImportProjectClient client = new ImportProjectClient(config);
        assertFalse(((ImportProjectConfig)client.getImportConfig()).isImportMissingPackages());
        assertEquals("",((ImportProjectConfig)client.getImportConfig()).getTmProjectId());
    }

    @Test
    public void testNullConstructor() {
        TMSConfig config = new ImportProjectConfig(null, null, false, null, null);
        ImportProjectClient client = new ImportProjectClient(config);
        assertFalse(((ImportProjectConfig)client.getImportConfig()).isImportMissingPackages());
        assertEquals("",((ImportProjectConfig)client.getImportConfig()).getTmProjectId());
    }
}
