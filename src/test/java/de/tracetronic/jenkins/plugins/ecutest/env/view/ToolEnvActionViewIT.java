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

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;

/**
 * Integration tests for {@link ToolEnvActionView}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolEnvActionViewIT extends IntegrationTestBase {

    @Test
    public void testWithoutToolEnvInvisibleAction() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final ToolEnvActionView toolEnvActionView = new ToolEnvActionView(build, jenkins.createTaskListener());
        build.addAction(toolEnvActionView);

        assertEquals("No tool env variables should exist", 0, toolEnvActionView.getEnvVariables().size());
    }

    @Test
    public void testWithToolEnvInvisibleAction() throws Exception {
        final int toolId = 0;
        final ETClient etClient = new ETClient("ECU-TEST", "ECU-TEST", "workspace", "settings", 0, false);
        final ToolEnvInvisibleAction toolEnvAction = new ToolEnvInvisibleAction(toolId, etClient);
        final FreeStyleBuild build = jenkins.createFreeStyleProject()
                .scheduleBuild2(0, null, Collections.singletonList(toolEnvAction)).get();

        assertNotNull("One ToolEnvInvisibleAction should exist", build.getAction(ToolEnvInvisibleAction.class));
        assertNotNull("One ToolEnvActionView should exist", build.getAction(ToolEnvActionView.class));
    }

}
