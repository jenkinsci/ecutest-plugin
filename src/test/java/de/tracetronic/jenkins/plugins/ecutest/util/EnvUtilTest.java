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
package de.tracetronic.jenkins.plugins.ecutest.util;

import static org.junit.Assert.assertEquals;
import hudson.EnvVars;

import org.junit.Test;

/**
 * Unit tests for {@link EnvUtil}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class EnvUtilTest {

    @Test
    public void testDefaultIfEmptyEnvVar() {
        final String expanded = EnvUtil.expandEnvVar("", new EnvVars(), "default");
        assertEquals("Should expand to default value if empty", "default", expanded);
    }

    @Test
    public void testDefaultIfNullEnvVar() {
        final String expanded = EnvUtil.expandEnvVar(null, new EnvVars(), "default");
        assertEquals("Should expand to default value if null", "default", expanded);
    }

    @Test
    public void testExpandedEnvVar() {
        final String expanded = EnvUtil.expandEnvVar("${test}", new EnvVars("test", "expandedTest"), "default");
        assertEquals("Should expand using env vars", "expandedTest", expanded);
    }

    @Test
    public void testMultipleExpandedEnvVar() {
        final String expanded = EnvUtil.expandEnvVar("${test}${var}",
                new EnvVars("test", "expandedTest", "var", "123"), "default");
        assertEquals("Should expand using multiple env vars", "expandedTest123", expanded);
    }
}
