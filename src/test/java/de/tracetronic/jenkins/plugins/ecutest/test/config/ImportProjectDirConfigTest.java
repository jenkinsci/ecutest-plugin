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

/**
 * Unit tests for {@link ImportProjectDirConfig}.
 */
public class ImportProjectDirConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportProjectDirConfig config = new ImportProjectDirConfig(null, null, null, null);
        assertThat(config.getTmsPath(), is(""));
        assertThat(config.getImportPath(), is(""));
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ImportProjectDirConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ImportProjectDirConfig config = new ImportProjectDirConfig("${PROJECT_DIR_PATH}", "${IMPORT_PATH}",
            "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("PROJECT_DIR_PATH", "projectDir");
        envVars.put("IMPORT_PATH", "import");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ImportProjectDirConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTmsPath(), is("projectDir"));
        assertThat(expConfig.getImportPath(), is("import"));
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportProjectDirConfig.class).verify();
    }
}
