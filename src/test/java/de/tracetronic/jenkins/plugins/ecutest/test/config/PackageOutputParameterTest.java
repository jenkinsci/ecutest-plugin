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
 * Unit tests for {@link PackageOutputParameter}.
 */
public class PackageOutputParameterTest {

    @Test
    public void testNullConstructor() {
        final PackageOutputParameter outputParameter = new PackageOutputParameter(null);
        assertThat(outputParameter.getName(), is(""));
    }

    @Test
    public void testExpand() {
        final PackageOutputParameter outputParameter = new PackageOutputParameter("${NAME}");
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        final PackageOutputParameter expOutputParameter = outputParameter.expand(envVars);
        assertThat(expOutputParameter.getName(), is("name"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PackageOutputParameter.class).verify();
    }
}
