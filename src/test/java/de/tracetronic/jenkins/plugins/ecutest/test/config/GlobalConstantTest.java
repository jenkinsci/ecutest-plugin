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
 * Unit tests for {@link GlobalConstant}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
