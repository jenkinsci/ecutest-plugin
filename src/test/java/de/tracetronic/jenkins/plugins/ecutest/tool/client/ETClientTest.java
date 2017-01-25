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
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import hudson.util.ArgumentListBuilder;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link ETClient}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETClientTest {

    private List<String> expectedArgs;
    private final String exe = "C:\\ECU-TEST\\ECU-TEST.exe";
    private final String ws = "C:\\Daten\\ECU-TEST";
    private final String settings = "C:\\Daten\\ECU-TEST\\settings";

    @Test
    public void testNullConstructor() {
        final ETClient client = new ETClient(null, null, null, null, 30, false);
        assertNotNull(client);
        assertEquals("", client.getToolName());
        assertEquals("", client.getInstallPath());
        assertEquals("", client.getWorkspaceDir());
        assertEquals("", client.getSettingsDir());
        assertNotNull(client.getTimeout());
        assertEquals("Check timeout", 30, client.getTimeout());
        assertFalse("Check debug mode", client.isDebug());
        assertEquals("", client.getVersion());
    }

    @Test
    public void testTearDownNullConstructor() {
        final ETClient client = new ETClient(null, 30);
        assertNotNull(client);
        assertEquals("", client.getToolName());
        assertEquals("", client.getInstallPath());
        assertEquals("", client.getWorkspaceDir());
        assertNotNull(client.getTimeout());
        assertEquals("Check timeout", 30, client.getTimeout());
        assertFalse("Check debug mode", client.isDebug());
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

    private void ensureCreateCmdLineArgs(final ETClient client, final List<String> expectedArgs) {
        final ArgumentListBuilder cmdLineArgs = client.createCmdLine();
        assertEquals(expectedArgs, cmdLineArgs.toList());
    }
}
