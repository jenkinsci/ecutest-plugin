/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResult;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link JUnitTestResultParser}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitTestResultParserTest {

    private static final String REPORT_FILE = String.format("%s/%s",
            JUnitPublisher.UNIT_TEMPLATE_NAME, JUnitPublisher.JUNIT_REPORT_FILE);

    @Test
    public void testEmptyTestReport() throws Exception {
        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final List<FilePath> xmlFiles = Collections.emptyList();
        final TestResult testResult = parser.parseResult(xmlFiles, TaskListener.NULL);

        assertEquals("No tests should be found", 0, testResult.getTotalCount());
    }

    @Test
    public void testMissingTestReport() throws Exception {
        final File xmlFile = new File("notfound", REPORT_FILE);
        final List<FilePath> xmlFiles = Arrays.asList(new FilePath(xmlFile));

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parseResult(xmlFiles, TaskListener.NULL);

        assertEquals("No tests should be found", 0, testResult.getTotalCount());
    }

    @Test
    public void testPassedTestReport() throws Exception {
        final URL url = this.getClass().getResource("PassedTestReport");
        final File testReportDir = new File(url.getFile());
        final File xmlFile = new File(testReportDir, REPORT_FILE);
        final List<FilePath> xmlFiles = Arrays.asList(new FilePath(xmlFile));

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parseResult(xmlFiles, TaskListener.NULL);

        assertEquals("One passed test should be found", 1, testResult.getPassCount());
    }

    @Test
    public void testFailedTestReport() throws Exception {
        final URL url = this.getClass().getResource("FailedTestReport");
        final File testReportDir = new File(url.getFile());
        final File xmlFile = new File(testReportDir, REPORT_FILE);
        final List<FilePath> xmlFiles = Arrays.asList(new FilePath(xmlFile));

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parseResult(xmlFiles, TaskListener.NULL);

        assertEquals("One failed test should be found", 1, testResult.getFailCount());
    }

    @Test
    public void testSkippedTestReport() throws Exception {
        final URL url = this.getClass().getResource("SkippedTestReport");
        final File testReportDir = new File(url.getFile());
        final File xmlFile = new File(testReportDir, REPORT_FILE);
        final List<FilePath> xmlFiles = Arrays.asList(new FilePath(xmlFile));

        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parseResult(xmlFiles, TaskListener.NULL);

        assertEquals("One skipped test should be found", 1, testResult.getSkipCount());
    }
}
