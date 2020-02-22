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
 * Unit tests for {@link ExportProjectAttributeConfig}.
 */
public class ExportProjectAttributeConfigTest {

    @Test
    public void testNullConstructor() {
        final ExportProjectAttributeConfig config = new ExportProjectAttributeConfig(null, null, null);
        assertThat(config.getFilePath(), is(""));
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ExportProjectAttributeConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ExportProjectAttributeConfig config = new ExportProjectAttributeConfig("${FILE_PATH}",
            "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("FILE_PATH", "test.prj");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ExportProjectAttributeConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getFilePath(), is("test.prj"));
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ExportProjectAttributeConfig.class).verify();
    }
}
