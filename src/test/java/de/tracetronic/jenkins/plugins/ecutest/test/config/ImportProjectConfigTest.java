/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ImportProjectConfig}.
 */
public class ImportProjectConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportProjectConfig config = new ImportProjectConfig(null, null, false, null, null, null);
        assertThat(config.getTmsPath(), is(""));
        assertThat(config.getImportPath(), is(""));
        assertFalse(config.isImportMissingPackages());
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ImportProjectConfig.getDefaultTimeout())));
        assertThat(config.getTmProjectId(), is(""));
    }

    @Test
    public void testExpand() {
        final ImportProjectConfig config = new ImportProjectConfig("${PROJECT_PATH}", "${IMPORT_PATH}",
            true, "${CREDENTIALS_ID}", "${TIMEOUT}", "tmProjectId");
        final EnvVars envVars = new EnvVars();
        envVars.put("PROJECT_PATH", "project");
        envVars.put("IMPORT_PATH", "import");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ImportProjectConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTmsPath(), is("project"));
        assertThat(expConfig.getImportPath(), is("import"));
        assertTrue(config.isImportMissingPackages());
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
        assertThat(config.getTmProjectId(), is("tmProjectId"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportProjectConfig.class).withRedefinedSuperclass().verify();
    }
}
