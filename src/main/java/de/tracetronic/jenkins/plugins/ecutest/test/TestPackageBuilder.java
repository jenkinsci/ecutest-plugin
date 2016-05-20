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
package de.tracetronic.jenkins.plugins.ecutest.test;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * Builder providing the execution of an ECU-TEST package.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestPackageBuilder extends AbstractTestBuilder {

    @Nonnull
    private PackageConfig packageConfig = PackageConfig.newInstance();

    /**
     * Instantiates a new {@link TestPackageBuilder}.
     *
     * @param testFile
     *            the package file
     */
    @DataBoundConstructor
    public TestPackageBuilder(final String testFile) {
        super(testFile);
    }

    /**
     * Instantiates a new {@link TestPackageBuilder}.
     *
     * @param testFile
     *            the package file
     * @param testConfig
     *            the test configuration
     * @param packageConfig
     *            the package configuration
     * @param executionConfig
     *            the execution configuration
     * @deprecated since 1.11 use {@link #TestPackageBuilder(String)}
     */
    @Deprecated
    public TestPackageBuilder(final String testFile, final TestConfig testConfig,
            final PackageConfig packageConfig, final ExecutionConfig executionConfig) {
        super(testFile, testConfig, executionConfig);
        this.packageConfig = packageConfig == null ? PackageConfig.newInstance() : packageConfig;
    }

    /**
     * @return the package configuration
     */
    @Nonnull
    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    /**
     * @param packageConfig
     *            the package configuration
     */
    @DataBoundSetter
    public void setPackageConfig(@Nonnull final PackageConfig packageConfig) {
        this.packageConfig = packageConfig;
    }

    @Override
    protected boolean runTest(final String testFile, final TestConfig testConfig,
            final ExecutionConfig executionConfig, final Run<?, ?> run, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        // Expand package configuration
        final EnvVars buildEnv = run.getEnvironment(listener);
        final PackageConfig packageConfig = getPackageConfig().expand(buildEnv);

        // Run test case with package client
        final PackageClient testClient = new PackageClient(testFile, testConfig, packageConfig,
                executionConfig);
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(String.format("Executing package %s...", testFile));
        if (testClient.runTestCase(launcher, listener)) {
            logger.logInfo("Package executed successfully.");
        } else {
            logger.logError("Executing package failed!");
            return false;
        }

        // Add action for injecting environment variables
        final int builderId = getTestId(run);
        final TestEnvInvisibleAction envAction = new TestEnvInvisibleAction(builderId, testClient);
        run.addAction(envAction);

        return true;
    }

    /**
     * DescriptorImpl for {@link TestPackageBuilder}.
     */
    @Extension(ordinal = 1002)
    public static final class DescriptorImpl extends AbstractTestDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(TestPackageBuilder.class);
            load();
        }

        @Override
        public FormValidation doCheckTestFile(@QueryParameter final String value) {
            return testValidator.validatePackageFile(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.TestPackageBuilder_DisplayName();
        }
    }
}
