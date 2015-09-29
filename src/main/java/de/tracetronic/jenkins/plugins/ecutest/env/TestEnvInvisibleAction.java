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
package de.tracetronic.jenkins.plugins.ecutest.env;

import hudson.model.InvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;

/**
 * Helper invisible action which is used for exchanging information between {@link AbstractTestClient}s and other object
 * like {@link TestEnvContributor}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestEnvInvisibleAction extends InvisibleAction {

    /**
     * Defines the test type.
     */
    public enum TestType {
        /**
         * ECU-TEST package or project type.
         */
        PACKAGE, PROJECT
    }

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
     * @param testId
     *            identifies this invisible action and is used as the suffix for the test related build environment
     *            variables
     * @param testClient
     *            the test client holding the relevant information
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
        timeout = testClient.getExecutionConfig().getTimeout();
    }

    /**
     * @return the test id
     */
    public int getTestId() {
        return testId;
    }

    /**
     * @return the test name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * @return the test type
     */
    public TestType getTestType() {
        return testType;
    }

    /**
     * @return the test description
     */
    public String getTestDescription() {
        return testDescription;
    }

    /**
     * @return the test file
     */
    public String getTestFile() {
        return testFile;
    }

    /**
     * @return the test bench configuration
     */
    public String getTestTbc() {
        return testTbc;
    }

    /**
     * @return the test configuration
     */
    public String getTestTcf() {
        return testTcf;
    }

    /**
     * @return the test report directory
     */
    public String getTestReportDir() {
        return testReportDir;
    }

    /**
     * @return the test result
     */
    public String getTestResult() {
        return testResult;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }
}
