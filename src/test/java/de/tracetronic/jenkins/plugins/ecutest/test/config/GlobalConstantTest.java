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
 * Unit tests for {@link GlobalConstant}.
 */
public class GlobalConstantTest {

    @Test
    public void testNullConstructor() {
        final GlobalConstant constant = new GlobalConstant(null, null);
        assertThat(constant.getName(), is(""));
        assertThat(constant.getValue(), is(""));
    }

    @Test
    public void testExpand() {
        final GlobalConstant constant = new GlobalConstant("${NAME}", "${VALUE}");
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final GlobalConstant expParameter = constant.expand(envVars);
        assertThat(expParameter.getName(), is("name"));
        assertThat(expParameter.getValue(), is("value"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(PackageParameter.class).verify();
    }
}
