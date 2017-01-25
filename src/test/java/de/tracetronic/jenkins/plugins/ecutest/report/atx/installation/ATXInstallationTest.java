/*
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link ATXInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@SuppressWarnings("rawtypes")
public class ATXInstallationTest {

    @Test
    public void testNullConfig() {
        final ATXInstallation inst = new ATXInstallation("TG", "ET", null);
        assertThat(inst.getName(), is("TG"));
        assertThat(inst.getToolName(), is("ET"));
        assertNotNull(inst.getConfig());
    }

    @Test
    public void testEmptyConfigMap() {
        final ATXConfig config = new ATXConfig(Collections.<String, List<ATXSetting>> emptyMap(),
                Collections.<ATXCustomSetting> emptyList());
        final ATXInstallation inst = new ATXInstallation("TG", "ET", config);
        assertNotNull(inst.getConfig());
        assertTrue(inst.getConfig().getConfigMap().isEmpty());
    }

    @Test
    public void testEmptyCustomSettings() {
        final ATXConfig config = new ATXConfig(Collections.<String, List<ATXSetting>> emptyMap(),
                Collections.<ATXCustomSetting> emptyList());
        final ATXInstallation inst = new ATXInstallation("TG", "ET", config);
        assertNotNull(inst.getConfig());
        assertTrue(inst.getConfig().getCustomSettings().isEmpty());
    }

    @Test
    public void testGetAllInstallations() {
        assertThat(ATXInstallation.all(), emptyArray());
    }

    @Test
    public void testGetUndefinedInstallation() {
        assertNull(ATXInstallation.get("undefined"));
    }
}
