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
import static org.junit.Assert.assertNotNull;
import hudson.util.ArgumentListBuilder;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link TSClient}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TSClientTest {

    private List<String> expectedArgs;
    private final String exe = "C:\\ECU-TEST\\ToolServer\\Tool-Server.exe";
    private final String tlIni = "C:\\ECU-TEST\\ToolServer\\ToolLibs.ini";

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
        final TSClient client = new TSClient("ECU-TEST", exe, 30, tlIni, 5017);
        expectedArgs = Arrays.asList(exe, "--port", "5017", "--toollibsini", tlIni);
        ensureCreateCmdLineArgs(client, expectedArgs);
    }

    @Test
    public void testDefaultCmdLineArgs() {
        final TSClient client = new TSClient("ECU-TEST", exe, 0, "", 0);
        expectedArgs = Arrays.asList(exe, "--port", "5017");
        ensureCreateCmdLineArgs(client, expectedArgs);
    }

    private void ensureCreateCmdLineArgs(final TSClient client, final List<String> expectedArgs) {
        final ArgumentListBuilder cmdLineArgs = client.createCmdLine();
        assertEquals(expectedArgs, cmdLineArgs.toList());
    }
}
