/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env.view;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import hudson.model.FreeStyleBuild;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
