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
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link JUnitPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitPublisherTest {

    @Test
    public void testNull() {
        final JUnitPublisher publisher = new JUnitPublisher(null, 0, 0, true, true);
        assertNotNull(publisher);
        assertNull(publisher.getToolName());
        assertEquals(0, Double.compare(0, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(0, publisher.getFailedThreshold()));
        assertTrue(publisher.isAllowMissing());
        assertTrue(publisher.isRunOnFailed());
    }

    @Test
    public void testNegativeThresholds() {
        final JUnitPublisher publisher = new JUnitPublisher(null, -1, -1, false, false);
        assertEquals(0, Double.compare(0, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(0, publisher.getFailedThreshold()));
    }

    @Test
    public void testInvalidThresholds() {
        final JUnitPublisher publisher = new JUnitPublisher(null, 101, 101, false, false);
        assertEquals(0, Double.compare(100, publisher.getUnstableThreshold()));
        assertEquals(0, Double.compare(100, publisher.getFailedThreshold()));
    }

    @Test
    public void testFailedPercentage() {
        assertEquals(0, Double.compare(0, JUnitPublisher.getFailedPercentage(0, 0)));
        assertEquals(0, Double.compare(0, JUnitPublisher.getFailedPercentage(0, 100)));
        assertEquals(0, Double.compare(50.0, JUnitPublisher.getFailedPercentage(50, 100)));
        assertEquals(0, Double.compare(100, JUnitPublisher.getFailedPercentage(100, 100)));
    }
}
