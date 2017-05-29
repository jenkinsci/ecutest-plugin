/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for {@link ExportPackageBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ExportPackageBuilderTest {

    @Test
    public void testDefaultStep() throws IOException {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ExportPackageBuilder builder = new ExportPackageBuilder(null);
        assertTrue(builder.getExportConfigs().isEmpty());
    }

    @Test
    public void testExportConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageConfig exportConfig = new ExportPackageConfig("test.pkg", "export", true, "cred", "600");
        exportConfigs.add(exportConfig);
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat((ExportPackageConfig) builder.getExportConfigs().get(0), sameInstance(exportConfig));
    }

    @Test
    public void testExportAttributeConfig() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        final ExportPackageAttributeConfig attributeConfig = new ExportPackageAttributeConfig("test.pkg", "cred", "600");
        exportConfigs.add(attributeConfig);
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertThat(builder.getExportConfigs(), hasSize(1));
        assertThat((ExportPackageAttributeConfig) builder.getExportConfigs().get(0), sameInstance(attributeConfig));
    }

    @Test
    public void testEmptyExportConfigs() {
        final List<TMSConfig> exportConfigs = new ArrayList<TMSConfig>();
        exportConfigs.add(new ExportPackageConfig(" ", null, false, null, null));
        final ExportPackageBuilder builder = new ExportPackageBuilder(exportConfigs);
        assertTrue(builder.getExportConfigs().isEmpty());
    }
}
