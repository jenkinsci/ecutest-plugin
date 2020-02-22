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
 * Unit tests for {@link PackageParameter}.
 */
public class PackageParameterTest {

    @Test
    public void testNullConstructor() {
        final PackageParameter parameter = new PackageParameter(null, null);
        assertThat(parameter.getName(), is(""));
        assertThat(parameter.getValue(), is(""));
    }

    @Test
    public void testExpand() {
        final PackageParameter parameter = new PackageParameter("${NAME}", "${VALUE}");
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final PackageParameter expParameter = parameter.expand(envVars);
        assertThat(expParameter.getName(), is("name"));
        assertThat(expParameter.getValue(), is("value"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PackageParameter.class).verify();
    }
}
