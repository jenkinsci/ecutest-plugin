/*
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
package de.tracetronic.jenkins.plugins.ecutest.env.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.model.FreeStyleBuild;

import java.util.Collections;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link TestEnvActionView}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestEnvActionViewST extends SystemTestBase {

    @Test
    public void testWithoutTestEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final TestEnvActionView testEnvActionView = new TestEnvActionView(build, jenkins.createTaskListener());
        build.addAction(testEnvActionView);

        assertEquals("No test env variables should exist", 0, testEnvActionView.getEnvVariables().size());
    }

    @Test
    public void testWithTestEnvInvisibleAction() throws Exception {
        final int testId = 0;
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true, true);
        final PackageClient packageClient = new PackageClient("test.pkg", testConfig, packageConfig, executionConfig);
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(testId, packageClient);
        final FreeStyleBuild build = jenkins.createFreeStyleProject()
                .scheduleBuild2(0, null, Collections.singletonList(testEnvAction)).get();

        assertNotNull("One TestEnvInvisibleAction should exist", build.getAction(TestEnvInvisibleAction.class));
        assertNotNull("One TestEnvActionView should exist", build.getAction(TestEnvActionView.class));
    }
}
