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
 * Unit tests for {@link ExportPackageConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportPackageConfigTest {

    @Test
    public void testNullConstructor() {
        final ExportPackageConfig config = new ExportPackageConfig(null, null, false, null, null);
        assertThat(config.getFilePath(), is(""));
        assertThat(config.getExportPath(), is(""));
        assertFalse(config.isCreateNewPath());
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ExportPackageConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ExportPackageConfig config = new ExportPackageConfig("${FILE_PATH}", "${EXPORT_PATH}",
            true, "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("FILE_PATH", "test.pkg");
        envVars.put("EXPORT_PATH", "export");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ExportPackageConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getFilePath(), is("test.pkg"));
        assertThat(expConfig.getExportPath(), is("export"));
        assertTrue(config.isCreateNewPath());
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ExportPackageConfig.class).verify();
    }
}
