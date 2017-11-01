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
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;

/**
 * Unit tests for {@link ProjectConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ProjectConfigTest {

    @Test
    public void testNullConstructor() {
        final ProjectConfig config = new ProjectConfig(true, null, JobExecutionMode.SEQUENTIAL_EXECUTION);
        assertTrue(config.isExecInCurrentPkgDir());
        assertThat(config.getFilterExpression(), is(""));
        assertThat(config.getJobExecMode(), is(JobExecutionMode.SEQUENTIAL_EXECUTION));
    }

    @Test
    public void testExpand() {
        final ProjectConfig config = new ProjectConfig(true, "${FILTER}", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final EnvVars envVars = new EnvVars();
        envVars.put("FILTER", "filter");
        assertThat(config.expand(envVars).getFilterExpression(), is("filter"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ProjectConfig.class).verify();
    }
}
