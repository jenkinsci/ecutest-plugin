/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.TestResultParser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import jenkins.MasterToSlaveFileCallable;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;

/**
 * Class providing a {@link TestResult} by parsing the JUnit report.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitTestResultParser extends TestResultParser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Parses the given JUnit test report files and builds a {@link TestResult} object that represents them.
     *
     * @param xmlFiles
     *            the JUnit report files
     * @param listener
     *            the listener
     * @return the {@link TestResult} instance
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    public TestResult parseResult(final List<FilePath> xmlFiles, final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        TestResult testResult = new TestResult(false);
        for (final FilePath xmlFile : xmlFiles) {
            if (xmlFile.exists()) {
                logger.logInfo(String.format("- Processing UNIT test results: %s", xmlFile));
                testResult = xmlFile.act(new ParseTestResultCallable(testResult));
                testResult.tally();
            }
        }
        return testResult;
    }

    @Override
    public String getDisplayName() {
        return "UNIT XML Parser";
    }

    @Override
    public String getTestResultLocationMessage() {
        return "UNIT XML reports:";
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
