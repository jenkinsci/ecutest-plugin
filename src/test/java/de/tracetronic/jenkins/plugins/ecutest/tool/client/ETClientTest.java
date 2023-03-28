/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import hudson.util.ArgumentListBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link ETClient}.
 */
public class ETClientTest {

    private final String exe = "C:\\ECU-TEST\\ECU-TEST.exe";
    private final String ws = "C:\\Daten\\ECU-TEST";
    private final String settings = "C:\\Daten\\ECU-TEST\\settings";
    private List<String> expectedArgs;

    @Test
    public void testNullConstructor() {
        final ETClient client = new ETClient(null, null, null, null, 30, false);
        assertNotNull(client);
        assertEquals("", client.getToolName());
        assertEquals("", client.getInstallPath());
        assertEquals("", client.getWorkspaceDir());
        assertEquals("", client.getSettingsDir());
        assertEquals("Check timeout", 30, client.getTimeout());
        assertFalse("Check debug mode", client.isDebugMode());
        assertFalse("Check license check", client.isLicenseCheck());
        assertEquals("", client.getVersion());
    }

    @Test
    public void testTearDownNullConstructor() {
        final ETClient client = new ETClient(null, 30);
        assertNotNull(client);
        assertEquals("", client.getToolName());
        assertEquals("", client.getInstallPath());
        assertEquals("", client.getWorkspaceDir());
        assertEquals("Check timeout", 30, client.getTimeout());
        assertFalse("Check debug mode", client.isDebugMode());
        assertFalse("Check license check", client.isLicenseCheck());
        assertEquals("", client.getVersion());
    }

    @Test
    public void testTypicalCmdLineArgs() {
        final ETClient etClient = new ETClient("ECU-TEST", exe, ws, settings, 120, false);
        expectedArgs = Arrays.asList(exe, "--workspaceDir", ws, "-s", settings, "--startupAutomated=CreateDirs");
        ensureCreateCmdLineArgs(etClient, expectedArgs);
    }

    @Test
    public void testDebugCmdLineArgs() {
        final ETClient etClient = new ETClient("ECU-TEST", exe, ws, settings, 120, true);
        expectedArgs = Arrays.asList(exe, "--workspaceDir", ws, "-s", settings, "-d", "--startupAutomated=CreateDirs");
        ensureCreateCmdLineArgs(etClient, expectedArgs);
    }

    @Test
    public void testLicenseCmdLineArgs() {
        final ETClient etClient = new ETClient("ECU-TEST", exe, "", "", 0, false);
        etClient.setLicenseCheck(true);
        expectedArgs = Arrays.asList(exe, "--startupAutomated=True", "-q");
        ensureCreateCmdLineArgs(etClient, expectedArgs);
    }

    private void ensureCreateCmdLineArgs(final ETClient client, final List<String> expectedArgs) {
        final ArgumentListBuilder cmdLineArgs = client.createCmdLine();
        assertEquals(expectedArgs, cmdLineArgs.toList());
    }
}
