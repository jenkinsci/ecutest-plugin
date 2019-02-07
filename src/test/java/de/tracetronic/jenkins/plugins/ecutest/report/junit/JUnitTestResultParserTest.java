/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
