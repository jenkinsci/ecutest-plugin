/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import hudson.util.ArgumentListBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link TSClient}.
 */
public class TSClientTest {

    private final String exe = "C:\\ECU-TEST\\ToolServer\\Tool-Server.exe";
    private final String tlIni = "C:\\ECU-TEST\\ToolServer\\ToolLibs.ini";
    private List<String> expectedArgs;

    @Test
    public void testBlankConstructor() {
        final TSClient client = new TSClient("", "", 0, "", 0);
        assertEquals("Check default TCP port", TSClient.DEFAULT_TCP_PORT, client.getTcpPort());
    }

    @Test
    public void testNullConstructor() {
        final TSClient client = new TSClient(null, null, 0, null, 0);
        assertNotNull(client);
        assertEquals("", client.getToolName());
        assertEquals("", client.getInstallPath());
        assertEquals("", client.getToolLibsIniPath());
        assertEquals("Check default TCP port", TSClient.DEFAULT_TCP_PORT, client.getTcpPort());
        assertNotNull(client.getTimeout());
        assertEquals("Check timeout", 0, client.getTimeout());
    }

    @Test
    public void testTypicalCmdLineArgs() {
        final TSClient client = new TSClient("ecu.test", exe, 30, tlIni, 5017);
        expectedArgs = Arrays.asList(exe, "--port", "5017", "--toollibsini", tlIni);
        ensureCreateCmdLineArgs(client, expectedArgs);
    }

    @Test
    public void testDefaultCmdLineArgs() {
        final TSClient client = new TSClient("ecu.test", exe, 0, "", 0);
        expectedArgs = Arrays.asList(exe, "--port", "5017");
        ensureCreateCmdLineArgs(client, expectedArgs);
    }

    private void ensureCreateCmdLineArgs(final TSClient client, final List<String> expectedArgs) {
        final ArgumentListBuilder cmdLineArgs = client.createCmdLine();
        assertEquals(expectedArgs, cmdLineArgs.toList());
    }
}
