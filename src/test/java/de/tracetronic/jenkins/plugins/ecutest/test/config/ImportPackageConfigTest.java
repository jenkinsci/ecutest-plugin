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
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ImportPackageConfig}.
 */
public class ImportPackageConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportPackageConfig config = new ImportPackageConfig(null, null, null, null);
        assertThat(config.getTmsPath(), is(""));
        assertThat(config.getImportPath(), is(""));
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ImportPackageConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ImportPackageConfig config = new ImportPackageConfig("${PACKAGE_PATH}", "${IMPORT_PATH}",
            "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("PACKAGE_PATH", "package");
        envVars.put("IMPORT_PATH", "import");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ImportPackageConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTmsPath(), is("package"));
        assertThat(expConfig.getImportPath(), is("import"));
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportPackageConfig.class).withRedefinedSuperclass().verify();
    }
}
