/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
 * Unit tests for {@link ImportPackageDirConfig}.
 */
public class ImportPackageDirConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportPackageDirConfig config = new ImportPackageDirConfig(null, null, null, null);
        assertThat(config.getTmsPath(), is(""));
        assertThat(config.getImportPath(), is(""));
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ImportPackageDirConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ImportPackageDirConfig config = new ImportPackageDirConfig("${PACKAGE_DIR_PATH}", "${IMPORT_PATH}",
            "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("PACKAGE_DIR_PATH", "packageDir");
        envVars.put("IMPORT_PATH", "import");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ImportPackageConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTmsPath(), is("packageDir"));
        assertThat(expConfig.getImportPath(), is("import"));
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportPackageDirConfig.class).withRedefinedSuperclass().verify();
    }
}
