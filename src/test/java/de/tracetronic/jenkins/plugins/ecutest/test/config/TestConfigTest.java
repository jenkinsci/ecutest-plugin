/**
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hudson.EnvVars;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

/**
 * Unit tests for {@link TestConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestConfigTest {

    @Test
    public void testNullConstructor() {
        final TestConfig config = new TestConfig(null, null, false, false, null);
        assertThat(config.getTbcFile(), is(""));
        assertThat(config.getTcfFile(), is(""));
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
        assertNotNull(config.getConstants());
    }

    @Test
    public void testEmptyConstructor() {
        final TestConfig config = new TestConfig(null, null, false, false);
        assertThat(config.getTbcFile(), is(""));
        assertThat(config.getTcfFile(), is(""));
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
        assertTrue(config.getConstants().isEmpty());
    }

    @Test
    public void testEmptyConstants() {
        final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();
        constants.add(new GlobalConstant(" ", " "));
        final TestConfig config = new TestConfig(null, null, false, false, constants);
        assertTrue(config.getConstants().isEmpty());
    }

    @Test
    public void testExpand() {
        final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();
        constants.add(new GlobalConstant("${NAME}", "${VALUE}"));
        final TestConfig config = new TestConfig("${TBC}", "${TCF}", false, false, constants);
        final EnvVars envVars = new EnvVars();
        envVars.put("TBC", "test.tbc");
        envVars.put("TCF", "test.tcf");
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final TestConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getTbcFile(), is("test.tbc"));
        assertThat(expConfig.getTcfFile(), is("test.tcf"));
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
        assertThat(expConfig.getConstants().get(0).getName(), is("name"));
        assertThat(expConfig.getConstants().get(0).getValue(), is("value"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCompatibility() {
        final TestConfig config = new TestConfig(null, null, null);
        assertFalse(config.isForceReload());
        assertFalse(config.isLoadOnly());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(TestConfig.class).verify();
    }
}
