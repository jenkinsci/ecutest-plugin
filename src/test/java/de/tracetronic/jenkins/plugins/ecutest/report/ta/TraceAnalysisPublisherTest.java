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
package de.tracetronic.jenkins.plugins.ecutest.report.ta;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TraceAnalysisPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TraceAnalysisPublisherTest {

    @Test
    public void testDefaultStep() throws IOException {
        final TraceAnalysisPublisher publisher = new TraceAnalysisPublisher("");
        assertPublisher(publisher);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final TraceAnalysisPublisher publisher = new TraceAnalysisPublisher(null);
        publisher.setTimeout(null);
        publisher.setAllowMissing(false);
        publisher.setRunOnFailed(false);
        publisher.setArchiving(true);
        publisher.setKeepAll(true);
        publisher.setMergeReports(true);
        publisher.setCreateReportDir(false);
        assertPublisher(publisher);
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher
     *            the publisher
     */
    private void assertPublisher(final TraceAnalysisPublisher publisher) {
        assertNotNull(publisher);
        assertNotNull(publisher.getToolName());
        assertNotNull(publisher.getTimeout());
        assertEquals(String.valueOf(TraceAnalysisPublisher.getDefaultTimeout()), publisher.getTimeout());
        assertFalse(publisher.isAllowMissing());
        assertFalse(publisher.isRunOnFailed());
        assertTrue(publisher.isArchiving());
        assertTrue(publisher.isKeepAll());
        assertTrue(publisher.isMergeReports());
        assertFalse(publisher.isCreateReportDir());
    }
}
