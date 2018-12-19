/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExportProjectBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportProjectBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ExportProjectBuilder builder = new ExportProjectBuilder(null);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @Test
    public void testExportConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectConfig exportConfig = new ExportProjectConfig("test.prj", "export", true, "cred", "600");
        exportConfigs.add(exportConfig);
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat((ExportProjectConfig) builder.getExportConfigs().get(0), sameInstance(exportConfig));
    }

    @Test
    public void testExportAttributeConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportProjectAttributeConfig attributeConfig = new ExportProjectAttributeConfig("test.prj", "cred", "600");
        exportConfigs.add(attributeConfig);
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat((ExportProjectAttributeConfig) builder.getExportConfigs().get(0), sameInstance(attributeConfig));
    }

    @Test
    public void testEmptyExportConfigs() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        exportConfigs.add(new ExportProjectConfig(" ", null, false, null, null));
        final ExportProjectBuilder builder = new ExportProjectBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }
}
