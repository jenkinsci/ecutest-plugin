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
package de.tracetronic.jenkins.plugins.ecutest.test;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * Builder providing the execution of an ECU-TEST project.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestProjectBuilder extends AbstractTestBuilder {

    @Nonnull
    private ProjectConfig projectConfig = ProjectConfig.newInstance();

    /**
     * Instantiates a new {@link TestProjectBuilder}.
     *
     * @param testFile
     *            the project file
     */
    @DataBoundConstructor
    public TestProjectBuilder(@Nonnull final String testFile) {
        super(testFile);
    }

    /**
     * @return the project configuration
     */
    @Nonnull
    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    /**
     * @param projectConfig
     *            the project configuration
     */
    @DataBoundSetter
    public void setProjectConfig(@CheckForNull final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig == null ? ProjectConfig.newInstance() : projectConfig;
    }

    @Override
    protected boolean runTest(final String testFile, final TestConfig testConfig,
            final ExecutionConfig executionConfig, final Run<?, ?> run, final FilePath workspace,
            final Launcher launcher, final TaskListener listener) throws IOException, InterruptedException {
        // Expand project configuration
        final EnvVars buildEnv = run.getEnvironment(listener);
        final ProjectConfig projectConfig = getProjectConfig().expand(buildEnv);

        // Run test case with project client
        final ProjectClient testClient = new ProjectClient(testFile, testConfig, projectConfig,
                executionConfig);
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(String.format("Executing project %s...", testFile));
        if (testClient.runTestCase(workspace, launcher, listener)) {
            addBuildAction(run, testClient);
            if (testClient.isAborted()) {
                logger.logWarn("Project execution aborted!");
                return false;
            } else {
                logger.logInfo("Project executed successfully.");
            }
        } else {
            logger.logError("Executing project failed!");
            return false;
        }

        return true;
    }

    /**
     * DescriptorImpl for {@link TestProjectBuilder}.
     */
    @Symbol("testProject")
    @Extension(ordinal = 10001)
    public static final class DescriptorImpl extends AbstractTestDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(TestProjectBuilder.class);
            load();
        }

        @Override
        public FormValidation doCheckTestFile(@QueryParameter final String value) {
            return testValidator.validateProjectFile(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.TestProjectBuilder_DisplayName();
        }
    }
}
