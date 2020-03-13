/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Builder providing the execution of an ECU-TEST project.
 */
public class TestProjectBuilder extends AbstractTestBuilder {

    @Nonnull
    private ProjectConfig projectConfig = ProjectConfig.newInstance();

    /**
     * Instantiates a new {@link TestProjectBuilder}.
     *
     * @param testFile the project file
     */
    @DataBoundConstructor
    public TestProjectBuilder(@Nonnull final String testFile) {
        super(testFile);
    }

    @Nonnull
    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    @DataBoundSetter
    public void setProjectConfig(@CheckForNull final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig == null ? ProjectConfig.newInstance() : projectConfig;
    }

    @Override
    protected boolean runTest(final String testFile, final TestConfig testConfig,
                              final ExecutionConfig executionConfig, final Run<?, ?> run, final FilePath workspace,
                              final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.TestProjectBuilder_DisplayName();
        }
    }
}
