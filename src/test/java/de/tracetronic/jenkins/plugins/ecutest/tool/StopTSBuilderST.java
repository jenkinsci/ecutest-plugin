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
package de.tracetronic.jenkins.plugins.ecutest.tool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * System tests for {@link StopTSBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StopTSBuilderST extends SystemTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final ETInstallation.DescriptorImpl etDescriptor = jenkins.jenkins
                .getDescriptorByType(ETInstallation.DescriptorImpl.class);
        etDescriptor.setInstallations(new ETInstallation("ECU-TEST", "C:\\ECU-TEST", JenkinsRule.NO_PROPERTIES));
    }

    @Test
    public void testRoundTripConfig() throws Exception {
        final StopTSBuilder before = new StopTSBuilder("ECU-TEST", "120");
        final StopTSBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after, "timeout");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StopTSBuilder before = new StopTSBuilder("ECU-TEST", "30");
        project.getBuildersList().add(before);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.StopTSBuilder_DisplayName());
        jenkins.assertXPath(page, "//select[@name='stopTSBuilder.toolName']");
        jenkins.assertXPath(page, "//option[@value='ECU-TEST']");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "30");
    }

    @Test
    public void testToolId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final StopTSBuilder builder = new StopTSBuilder("ECU-TEST", "120");
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Tool id should be 0", 0, builder.getToolId(build));
    }
}
