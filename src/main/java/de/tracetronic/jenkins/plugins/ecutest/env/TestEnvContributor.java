/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction.TestType;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Contributor which adds various test related variables into the build environment variables.
 */
@Extension
public class TestEnvContributor extends EnvironmentContributor {

    /**
     * Prefix for all build environment variables created by this {@link TestEnvContributor}.
     */
    public static final String PREFIX = "TT_";

    /**
     * Build environment variable part for the test name.
     */
    public static final String TEST_NAME = "TEST_NAME_";

    /**
     * Build environment variable part for the test type.
     */
    public static final String TEST_TYPE = "TEST_TYPE_";

    /**
     * Build environment variable part for the test name.
     */
    public static final String TEST_DESCRIPTION = "TEST_DESCRIPTION_";

    /**
     * Build environment variable part for the test file.
     */
    public static final String TEST_FILE = "TEST_FILE_";

    /**
     * Build environment variable part for the test bench configuration.
     */
    public static final String TEST_TBC = "TEST_TBC_";

    /**
     * Build environment variable part for the test configuration.
     */
    public static final String TEST_TCF = "TEST_TCF_";

    /**
     * Build environment variable part for the test report directory.
     */
    public static final String TEST_REPORT = "TEST_REPORT_";

    /**
     * Build environment variable part for the test result.
     */
    public static final String TEST_RESULT = "TEST_RESULT_";

    /**
     * Build environment variable part for the timeout running the test.
     */
    public static final String TEST_TIMEOUT = "TEST_TIMEOUT_";

    /**
     * Build environment variable part for the output parameter.
     */
    public static final String TEST_RETVAL = "TEST_RETVAL_";


    @Override
    public void buildEnvironmentFor(@Nonnull final Run r, @Nonnull final EnvVars envs,
                                    @Nonnull final TaskListener listener) {
        final List<TestEnvInvisibleAction> envActions = r.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction action : envActions) {
            final String id = String.valueOf(action.getTestId());
            final TestType testType = action.getTestType();
            final Map<String, String> outParams = action.getOutParams();
            // Exclude test description for project type
            if (testType.equals(TestType.PACKAGE)) {
                envs.put(PREFIX + TEST_DESCRIPTION + id, action.getTestDescription());
            }
            envs.put(PREFIX + TEST_NAME + id, action.getTestName());
            envs.put(PREFIX + TEST_TYPE + id, testType.name());
            envs.put(PREFIX + TEST_FILE + id, action.getTestFile());
            envs.put(PREFIX + TEST_TBC + id, action.getTestTbc());
            envs.put(PREFIX + TEST_TCF + id, action.getTestTcf());
            envs.put(PREFIX + TEST_REPORT + id, action.getTestReportDir());
            envs.put(PREFIX + TEST_RESULT + id, action.getTestResult());
            envs.put(PREFIX + TEST_TIMEOUT + id, String.valueOf(action.getTimeout()));
            for (Map.Entry<String, String> outParam : outParams.entrySet()) {
                envs.put(PREFIX + TEST_RETVAL + outParam.getKey() + "_" + id, outParam.getValue());
            }
        }
    }
}
