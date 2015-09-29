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
package de.tracetronic.jenkins.plugins.ecutest.log;

import static org.junit.Assert.assertTrue;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;

import java.io.IOException;

import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;

/**
 * System tests for {@link TTConsoleLogger}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TTConsoleLoggerST extends SystemTestBase {

    @Test
    public void testPlainTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                    final BuildListener listener) throws InterruptedException, IOException {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.log("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Plain text log output should be present in build console log",
                consoleLog.asText().contains("TTConsoleLogger"));
    }

    @Test
    public void testInfoTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                    final BuildListener listener) throws InterruptedException, IOException {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logInfo("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated info log output should be present in build console log",
                consoleLog.asText().contains("[TT] INFO: TTConsoleLogger"));
    }

    @Test
    public void testWarnTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                    final BuildListener listener) throws InterruptedException, IOException {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logWarn("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated warn log output should be present in build console log",
                consoleLog.asText().contains("[TT] WARN: TTConsoleLogger"));
    }

    @Test
    public void testErrorTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                    final BuildListener listener) throws InterruptedException, IOException {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logError("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated error log output should be present in build console log",
                consoleLog.asText().contains("[TT] ERROR: TTConsoleLogger"));
    }
}
