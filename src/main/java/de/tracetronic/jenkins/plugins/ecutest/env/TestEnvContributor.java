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
package de.tracetronic.jenkins.plugins.ecutest.env;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction.TestType;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Contributor which adds various test related variables into the build environment variables.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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

    @Override
    @SuppressWarnings("rawtypes")
    public void buildEnvironmentFor(@Nonnull final Run r, @Nonnull final EnvVars envs,
                                    @Nonnull final TaskListener listener) throws IOException, InterruptedException {

        final List<TestEnvInvisibleAction> envActions = r.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction action : envActions) {
            final String id = String.valueOf(action.getTestId());
            final TestType testType = action.getTestType();
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
        }
    }
}
