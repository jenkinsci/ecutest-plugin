/*
 * Copyright (c) 2015 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hudson.EnvVars;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

/**
 * Unit tests for {@link PackageConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class PackageConfigTest {

    @Test
    public void testNullConstructor() {
        final PackageConfig config = new PackageConfig(true, true, null);
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
        final PackageConfig config = new PackageConfig(true, true, parameters);
        assertTrue(config.getParameters().isEmpty());
    }

    @Test
    public void testExpand() {
        final List<PackageParameter> params = new ArrayList<PackageParameter>();
        params.add(new PackageParameter("${NAME}", "${VALUE}"));
        final PackageConfig config = new PackageConfig(true, true, params);
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
