/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PackageConfig}.
 */
public class PackageConfigTest {

    @Test
    public void testNullConstructor() {
        final PackageConfig config = new PackageConfig(true, true, null, null);
        assertTrue(config.isRunTest());
        assertTrue(config.isRunTraceAnalysis());
        assertNotNull(config.getParameters());
    }

    @Test
    public void testEmptyConstructor() {
        final PackageConfig config = new PackageConfig(true, true);
        assertTrue(config.isRunTest());
        assertTrue(config.isRunTraceAnalysis());
        assertTrue(config.getParameters().isEmpty());
    }

    @Test
    public void testEmptyParameters() {
        final List<PackageParameter> parameters = new ArrayList<PackageParameter>();
        parameters.add(new PackageParameter(" ", " "));
        final PackageConfig config = new PackageConfig(true, true, parameters, null);
        assertTrue(config.getParameters().isEmpty());
    }

    @Test
    public void testExpand() {
        final List<PackageParameter> params = new ArrayList<PackageParameter>();
        params.add(new PackageParameter("${NAME}", "${VALUE}"));
        final List<PackageOutputParameter> outParams = new ArrayList<PackageOutputParameter>();
        outParams.add(new PackageOutputParameter("${VARIABLE}"));
        final PackageConfig config = new PackageConfig(true, true, params, outParams);
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final PackageConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getParameters().get(0).getName(), is("name"));
        assertThat(expConfig.getParameters().get(0).getValue(), is("value"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PackageConfig.class).verify();
    }
}
