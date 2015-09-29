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
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import static org.junit.Assert.assertEquals;
import hudson.Launcher;
import hudson.model.FreeStyleBuild;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResult;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link JUnitTestResultParser}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitTestResultParserST extends SystemTestBase {

    @Test
    public void testEmptyTestReport() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TaskListener listener = launcher.getListener();
        final StreamBuildListener buildListener = new StreamBuildListener((OutputStream) listener.getLogger());

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parse(JUnitReportGenerator.UNIT_TEMPLATE_NAME, build, launcher,
                buildListener);

        assertEquals("No tests should be found", 0, testResult.getTotalCount());
    }

    @Test
    public void testMissingTestReport() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TaskListener listener = launcher.getListener();
        final StreamBuildListener buildListener = new StreamBuildListener((OutputStream) listener.getLogger());

        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true);
        final PackageClient packageClient = new PackageClient("test.pkg", testConfig, packageConfig, executionConfig);
        packageClient.setTestReportDir("notfound");
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(0, packageClient);
        build.addAction(testEnvAction);

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parse(JUnitReportGenerator.UNIT_TEMPLATE_NAME, build, launcher,
                buildListener);

        assertEquals("No tests should be found", 0, testResult.getTotalCount());
    }

    @Test
    public void testPassedTestReport() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TaskListener listener = launcher.getListener();
        final StreamBuildListener buildListener = new StreamBuildListener((OutputStream) listener.getLogger());

        final URL url = this.getClass().getResource("PassedTestReport");
        final File testReportDir = new File(url.getFile());

        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true);
        final PackageClient packageClient = new PackageClient("", testConfig, packageConfig, executionConfig);
        packageClient.setTestReportDir(testReportDir.getAbsolutePath());
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(0, packageClient);
        build.addAction(testEnvAction);

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parse(JUnitReportGenerator.UNIT_TEMPLATE_NAME, build, launcher,
                buildListener);

        assertEquals("One passed test should be found", 1, testResult.getPassCount());
    }

    @Test
    public void testFailedTestReport() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TaskListener listener = launcher.getListener();
        final StreamBuildListener buildListener = new StreamBuildListener((OutputStream) listener.getLogger());

        final URL url = this.getClass().getResource("FailedTestReport");
        final File testReportDir = new File(url.getFile());

        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true);
        final PackageClient packageClient = new PackageClient("", testConfig, packageConfig, executionConfig);
        packageClient.setTestReportDir(testReportDir.getAbsolutePath());
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(0, packageClient);
        build.addAction(testEnvAction);

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parse(JUnitReportGenerator.UNIT_TEMPLATE_NAME, build, launcher,
                buildListener);

        assertEquals("One failed test should be found", 1, testResult.getFailCount());
    }

    @Test
    public void testSkippedTestReport() throws Exception {
        final FreeStyleBuild build = jenkins.createFreeStyleProject().scheduleBuild2(0).get();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TaskListener listener = launcher.getListener();
        final StreamBuildListener buildListener = new StreamBuildListener((OutputStream) listener.getLogger());

        final URL url = this.getClass().getResource("SkippedTestReport");
        final File testReportDir = new File(url.getFile());

        final TestConfig testConfig = new TestConfig("", "");
        final PackageConfig packageConfig = new PackageConfig(true, true);
        final ExecutionConfig executionConfig = new ExecutionConfig(600, true);
        final PackageClient packageClient = new PackageClient("", testConfig, packageConfig, executionConfig);
        packageClient.setTestReportDir(testReportDir.getAbsolutePath());
        final TestEnvInvisibleAction testEnvAction = new TestEnvInvisibleAction(0, packageClient);
        build.addAction(testEnvAction);

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parse(JUnitReportGenerator.UNIT_TEMPLATE_NAME, build, launcher,
                buildListener);

        assertEquals("One skipped test should be found", 1, testResult.getSkipCount());
    }
}
