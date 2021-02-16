/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.TestResultParser;
import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Class providing a {@link TestResult} by parsing the JUnit report.
 */
public class JUnitTestResultParser extends TestResultParser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Parses the given JUnit test report files and builds a {@link TestResult} object that represents them.
     *
     * @param xmlFiles the JUnit report files
     * @param listener the listener
     * @return the {@link TestResult} instance
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
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

    @SuppressWarnings("deprecation")
    @Override
    public String getDisplayName() {
        return "UNIT XML Parser";
    }

    @SuppressWarnings("deprecation")
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
         * @param testResult the test result
         */
        ParseTestResultCallable(final TestResult testResult) {
            this.testResult = testResult;
        }

        @Override
        public TestResult invoke(final File file, final VirtualChannel channel) throws IOException {
            testResult.parse(file, null);
            return testResult;
        }
    }
}
