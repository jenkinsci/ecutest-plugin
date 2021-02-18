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
 * Unit tests for {@link ExportProjectConfig}.
 */
public class ExportProjectConfigTest {

    @Test
    public void testNullConstructor() {
        final ExportProjectConfig config = new ExportProjectConfig(null, null, false, null, null);
        assertThat(config.getFilePath(), is(""));
        assertThat(config.getExportPath(), is(""));
        assertFalse(config.isCreateNewPath());
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ExportProjectConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ExportProjectConfig config = new ExportProjectConfig("${FILE_PATH}", "${EXPORT_PATH}",
            true, "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("FILE_PATH", "test.prj");
        envVars.put("EXPORT_PATH", "export");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ExportProjectConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getFilePath(), is("test.prj"));
        assertThat(expConfig.getExportPath(), is("export"));
        assertTrue(config.isCreateNewPath());
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ExportProjectConfig.class).verify();
    }
}
