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
 * Unit tests for {@link ImportPackageAttributeConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportPackageAttributeConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportPackageAttributeConfig config = new ImportPackageAttributeConfig(null, null, null);
        assertThat(config.getFilePath(), is(""));
        assertThat(config.getCredentialsId(), is(""));
        assertThat(config.getTimeout(), is(String.valueOf(ImportPackageAttributeConfig.getDefaultTimeout())));
    }

    @Test
    public void testExpand() {
        final ImportPackageAttributeConfig config = new ImportPackageAttributeConfig("${FILE_PATH}",
            "${CREDENTIALS_ID}", "${TIMEOUT}");
        final EnvVars envVars = new EnvVars();
        envVars.put("FILE_PATH", "test.pkg");
        envVars.put("CREDENTIALS_ID", "credentialsId");
        envVars.put("TIMEOUT", "600");
        final ImportPackageAttributeConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getFilePath(), is("test.pkg"));
        assertThat(expConfig.getCredentialsId(), is("credentialsId"));
        assertThat(expConfig.getTimeout(), is("600"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportPackageAttributeConfig.class).verify();
    }
}
