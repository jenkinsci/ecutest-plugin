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
 * Unit tests for {@link ExportPackageAttributeConfig}.
 */
public class ExportPackageAttributeConfigTest {

    @Test
    public void testNullConstructor() {
        final ExportPackageAttributeConfig config = new ExportPackageAttributeConfig(null, null, null);
        assertThat(config.getFilePath(), is(""));
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ExportPackageAttributeConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ExportPackageAttributeConfig config = new ExportPackageAttributeConfig("${FILE_PATH}",
            "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("FILE_PATH", "test.pkg");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ExportPackageAttributeConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getFilePath(), is("test.pkg"));
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ExportPackageAttributeConfig.class).verify();
    }
}
