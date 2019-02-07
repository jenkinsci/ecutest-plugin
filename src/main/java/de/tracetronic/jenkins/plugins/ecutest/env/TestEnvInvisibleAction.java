/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env;

import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import hudson.model.InvisibleAction;

/**
 * Helper invisible action which is used for exchanging information between {@link AbstractTestClient}s
 * and other objects like {@link TestEnvContributor}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestEnvInvisibleAction extends InvisibleAction {

    private final int testId;
    private final String testName;
    private final TestType testType;
    private final String testDescription;
    private final String testFile;
    private final String testTbc;
    private final String testTcf;
    private final String testReportDir;
    private final String testResult;
    private final int timeout;
    /**
     * Instantiates a new {@link TestEnvInvisibleAction}.
     *
     * @param testId     identifies this invisible action and is used as the suffix
     *                   for the test related build environment variables
     * @param testClient the test client holding the relevant information
     */
    public TestEnvInvisibleAction(final int testId, final AbstractTestClient testClient) {
        super();
        this.testId = testId;
        testName = testClient.getTestName();
        if (testClient instanceof PackageClient) {
            testType = TestType.PACKAGE;
        } else {
            testType = TestType.PROJECT;
        }
        testDescription = testClient.getTestDescription();
        testFile = testClient.getTestFile();
        testTbc = testClient.getTestConfig().getTbcFile();
        testTcf = testClient.getTestConfig().getTcfFile();
        testReportDir = testClient.getTestReportDir();
        testResult = testClient.getTestResult();
        timeout = testClient.getExecutionConfig().getParsedTimeout();
    }

    public int getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public TestType getTestType() {
        return testType;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public String getTestFile() {
        return testFile;
    }

    public String getTestTbc() {
        return testTbc;
    }

    public String getTestTcf() {
        return testTcf;
    }

    public String getTestReportDir() {
        return testReportDir;
    }

    public String getTestResult() {
        return testResult;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Defines the test type.
     */
    public enum TestType {
        /**
         * ECU-TEST package or project type.
         */
        PACKAGE, PROJECT
    }
}
