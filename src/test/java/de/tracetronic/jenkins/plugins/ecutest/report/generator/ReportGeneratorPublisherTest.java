/**
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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link ReportGeneratorPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorPublisherTest {

    @Test
    public void testNullConstructor() {
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher(null, null, null, true, true);
        assertNotNull(publisher);
        assertNull(publisher.getToolName());
        assertNotNull(publisher.getGenerators());
        assertNotNull(publisher.getCustomGenerators());
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
    }

    @Test
    public void testEmptyConstructor() {
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("", null, null, true, true);
        assertTrue(publisher.getToolName().isEmpty());
        assertTrue(publisher.getGenerators().isEmpty());
        assertTrue(publisher.getCustomGenerators().isEmpty());
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
    }

    @Test
    public void testConstructor() {
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig("HTML", null));
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig("Custom", null));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST", generators,
                customGenerators, true, true);
        assertNotNull(publisher);
        assertEquals("ECU-TEST", publisher.getToolName());
        assertThat(publisher.getGenerators(), hasSize(1));
        assertThat(publisher.getCustomGenerators(), hasSize(1));
        assertThat(publisher.getGenerators().get(0).getName(), is("HTML"));
        assertTrue(publisher.getGenerators().get(0).getSettings().isEmpty());
        assertThat(publisher.getCustomGenerators().get(0).getName(), is("Custom"));
        assertTrue(publisher.getCustomGenerators().get(0).getSettings().isEmpty());
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
    }

    @Test
    public void testEmptyGenerators() {
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.add(new ReportGeneratorConfig(" ", null));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST", generators,
                null, true, true);
        assertTrue(publisher.getGenerators().isEmpty());
    }

    @Test
    public void testEmptyCustomGenerators() {
        final List<ReportGeneratorConfig> customGenerators = new ArrayList<ReportGeneratorConfig>();
        customGenerators.add(new ReportGeneratorConfig(" ", null));
        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher("ECU-TEST", null,
                customGenerators, true, true);
        assertTrue(publisher.getCustomGenerators().isEmpty());
    }
}
