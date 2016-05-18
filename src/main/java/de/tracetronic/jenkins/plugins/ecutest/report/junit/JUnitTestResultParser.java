/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.TestResultParser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jenkins.MasterToSlaveFileCallable;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;

/**
 * Class providing a {@link TestResult} by parsing the JUnit report.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitTestResultParser extends TestResultParser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * File name of the UNIT report file.
     */
    private static final String JUNIT_REPORT_FILE = "junit-report.xml";

    @Override
    public String getDisplayName() {
        return "UNIT XML Parser";
    }

    @Override
    public String getTestResultLocationMessage() {
        return "UNIT XML reports:";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public TestResult parse(final String junitDir, final AbstractBuild build, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        if (!(listener instanceof BuildListener)) {
            throw new AssertionError("Unexpected type: " + listener);
        }
        final TTConsoleLogger logger = new TTConsoleLogger((BuildListener) listener);
        TestResult testResult = new TestResult(false);
        final List<FilePath> reportFiles = getReportFiles(junitDir, build, launcher);
        for (final FilePath reportFile : reportFiles) {
            logger.logInfo(String.format("- Processing UNIT test results: %s", reportFile));
            testResult = reportFile.act(new ParseTestResultCallable(testResult));
            testResult.tally();
        }
        return testResult;
    }

    /**
     * Builds a list of report files for parsing the test results.
     * Includes the test results generated during separate sub-project execution.
     *
     * @param junitDir
     *            the UNIT directory
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @return the list of report files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getReportFiles(final String junitDir, final AbstractBuild<?, ?> build,
            final Launcher launcher) throws IOException, InterruptedException {
        final List<FilePath> reportFiles = new ArrayList<FilePath>();
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath testReportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
            if (testReportDir.exists()) {
                reportFiles.addAll(Arrays.asList(testReportDir.list(
                        String.format("**/%s/%s", junitDir, JUNIT_REPORT_FILE))));
            }
        }
        Collections.reverse(reportFiles);
        return reportFiles;
    }

    /**
     * {@link FileCallable} enabling remote file access to parse the JUnit report.
     */
    private static final class ParseTestResultCallable extends MasterToSlaveFileCallable<TestResult> {

        private static final long serialVersionUID = 1L;

        private final TestResult testResult;

        /**
         * Instantiates a new {@link ParseTestResultCallable}.
         *
         * @param testResult
         *            the test result
         */
        ParseTestResultCallable(final TestResult testResult) {
            this.testResult = testResult;
        }

        @Override
        public TestResult invoke(final File file, final VirtualChannel channel) throws IOException,
                InterruptedException {
            testResult.parse(file);
            return testResult;
        }
    }
}
