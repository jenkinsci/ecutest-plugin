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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for {@link ReportGeneratorPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorPublisherTest {

    @Test
    public void testDefaultStep() throws IOException {
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("");
        assertPublisher(publisher);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher(null);
        publisher.setGenerators(null);
        publisher.setCustomGenerators(null);
        publisher.setAllowMissing(false);
        publisher.setRunOnFailed(false);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        assertPublisher(publisher);
    }

    @Test
    public void testConstructor() {
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig("HTML", null));
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig("Custom", null));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setGenerators(generators);
        publisher.setCustomGenerators(customGenerators);
        assertPublisher(publisher);
        assertEquals("ECU-TEST", publisher.getToolName());
        assertThat(publisher.getGenerators(), hasSize(1));
        assertThat(publisher.getCustomGenerators(), hasSize(1));
        assertThat(publisher.getGenerators().get(0).getName(), is("HTML"));
        assertTrue(publisher.getGenerators().get(0).getSettings().isEmpty());
        assertThat(publisher.getCustomGenerators().get(0).getName(), is("Custom"));
        assertTrue(publisher.getCustomGenerators().get(0).getSettings().isEmpty());
    }

    @Test
    public void testEmptyGenerators() {
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig(" ", null));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setGenerators(generators);
        assertTrue(publisher.getGenerators().isEmpty());
    }

    @Test
    public void testEmptyCustomGenerators() {
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig(" ", null));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST");
        publisher.setCustomGenerators(customGenerators);
        assertTrue(publisher.getCustomGenerators().isEmpty());
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher
     *            the publisher
     */
    private void assertPublisher(final ReportGeneratorPublisher publisher) {
        assertNotNull(publisher);
        assertNotNull(publisher.getToolName());
        assertNotNull(publisher.getGenerators());
        assertNotNull(publisher.getCustomGenerators());
        assertFalse(publisher.isAllowMissing());
        assertFalse(publisher.isRunOnFailed());
        assertTrue(publisher.isArchiving());
        assertTrue(publisher.isKeepAll());
    }
}
