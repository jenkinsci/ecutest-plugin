/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
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
        assertNotNull(config.getOutputParameters());
    }

    @Test
    public void testEmptyConstructor() {
        final PackageConfig config = new PackageConfig(true, true);
        assertTrue(config.isRunTest());
        assertTrue(config.isRunTraceAnalysis());
        assertTrue(config.getParameters().isEmpty());
        assertTrue(config.getOutputParameters().isEmpty());
    }

    @Test
    public void testEmptyParameters() {
        final List<PackageParameter> parameters = new ArrayList<PackageParameter>();
        parameters.add(new PackageParameter(" ", " "));
        final PackageConfig config = new PackageConfig(true, true, parameters, null);
        assertTrue(config.getParameters().isEmpty());
        assertNotNull(config.getOutputParameters());
    }

    @Test
    public void testEmptyOutputParameters() {
        final List<PackageOutputParameter> outputParameters = new ArrayList<PackageOutputParameter>();
        outputParameters.add(new PackageOutputParameter(" "));
        final PackageConfig config = new PackageConfig(true, true, null, outputParameters);
        assertNotNull(config.getParameters());
        assertTrue(config.getOutputParameters().isEmpty());
    }

    @Test
    public void testExpand() {
        final List<PackageParameter> parameters = new ArrayList<PackageParameter>();
        parameters.add(new PackageParameter("${NAME}", "${VALUE}"));
        final List<PackageOutputParameter> outputParameters = new ArrayList<PackageOutputParameter>();
        outputParameters.add(new PackageOutputParameter("${OUTNAME}"));
        final PackageConfig config = new PackageConfig(true, true, parameters, outputParameters);
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        envVars.put("OUTNAME", "outName");
        final PackageConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getParameters().get(0).getName(), is("name"));
        assertThat(expConfig.getParameters().get(0).getValue(), is("value"));
        assertThat(expConfig.getOutputParameters().get(0).getName(), is("outName"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PackageConfig.class).verify();
    }
}
