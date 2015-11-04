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
package de.tracetronic.jenkins.plugins.ecutest.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link TestPackageBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestFolderBuilderST extends SystemTestBase {

    @Test
    public void testRoundTripConfig() throws Exception {
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ProjectConfig projectConfig = new ProjectConfig(false, "", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, false);
        final TestFolderBuilder before = new TestFolderBuilder("tests", TestFolderBuilder.DEFAULT_SCANMODE, false,
                testConfig, packageConfig, projectConfig, executionConfig);
        final TestFolderBuilder after = jenkins.configRoundtrip(before);
        jenkins.assertEqualBeans(before, after,
                "testFile,scanMode,recursiveScan,testConfig,packageConfig,projectConfig,executionConfig");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ProjectConfig projectConfig = new ProjectConfig(true, "filter", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true);
        final TestFolderBuilder builder = new TestFolderBuilder("tests", TestFolderBuilder.DEFAULT_SCANMODE, true,
                testConfig, packageConfig, projectConfig, executionConfig);
        project.getBuildersList().add(builder);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.TestFolderBuilder_DisplayName());
        WebAssert.assertInputPresent(page, "_.testFile");
        WebAssert.assertInputContainsValue(page, "_.testFile", "tests");
        jenkins.assertXPath(page, "//input[@name='_.recursiveScan' and @checked='true']");
        WebAssert.assertInputPresent(page, "_.tbcFile");
        WebAssert.assertInputContainsValue(page, "_.tbcFile", "test.tbc");
        WebAssert.assertInputPresent(page, "_.tcfFile");
        WebAssert.assertInputContainsValue(page, "_.tcfFile", "test.tcf");
        jenkins.assertXPath(page, "//input[@name='_.runTest' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.runTraceAnalysis' and @checked='true']");
        jenkins.assertXPath(page, "//input[@name='_.execInCurrentPkgDir' and @checked='true']");
        WebAssert.assertInputPresent(page, "_.filterExpression");
        WebAssert.assertInputContainsValue(page, "_.filterExpression", "filter");
        WebAssert.assertInputPresent(page, "_.timeout");
        WebAssert.assertInputContainsValue(page, "_.timeout", "600");
        jenkins.assertXPath(page, "//input[@name='_.stopOnError' and @checked='true']");
    }

    @Test
    public void testTestId() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final TestConfig testConfig = new TestConfig("test.tbc", "test.tcf");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ProjectConfig projectConfig = new ProjectConfig(false, "", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, false);
        final TestFolderBuilder builder = new TestFolderBuilder("tests", TestFolderBuilder.DEFAULT_SCANMODE, false,
                testConfig, packageConfig, projectConfig, executionConfig);
        project.getBuildersList().add(builder);

        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        when(build.getProject()).thenReturn(project);

        assertEquals("Test id should be 0", 0, builder.getTestId(build));
    }
}
