/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.util.IOUtils;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Common base class for all test related task builders implemented in this plugin.
 */
public abstract class AbstractTestBuilder extends AbstractTestHelper implements SimpleBuildStep {

    @Nonnull
    private final String testFile;
    @Nonnull
    private TestConfig testConfig = TestConfig.newInstance();
    @Nonnull
    private ExecutionConfig executionConfig = ExecutionConfig.newInstance();

    /**
     * Instantiates a new {@link AbstractTestBuilder}.
     *
     * @param testFile the test file path
     */
    public AbstractTestBuilder(final String testFile) {
        super();
        this.testFile = StringUtils.trimToEmpty(testFile);
    }

    @Nonnull
    public String getTestFile() {
        return testFile;
    }

    @Nonnull
    public TestConfig getTestConfig() {
        return testConfig;
    }

    @DataBoundSetter
    public void setTestConfig(@CheckForNull final TestConfig testConfig) {
        this.testConfig = testConfig == null ? TestConfig.newInstance() : testConfig;
    }

    @Nonnull
    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    @DataBoundSetter
    public void setExecutionConfig(@CheckForNull final ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig == null ? ExecutionConfig.newInstance() : executionConfig;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws InterruptedException, IOException {

        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        try {
            ProcessUtil.checkOS(launcher);
            final boolean performed = performTest(run, workspace, launcher, listener);
            if (!performed) {
                if (getExecutionConfig().isStopOnError()) {
                    logger.logInfo("- Closing running ECU-TEST and Tool-Server instances...");
                    if (closeETInstance(launcher, listener)) {
                        logger.logInfo("-> ECU-TEST closed successfully.");
                    } else {
                        logger.logInfo("-> No running ECU-TEST instance found.");
                    }
                    if (checkTSInstance(launcher, true)) {
                        logger.logInfo("-> Tool-Server closed successfully.");
                    } else {
                        logger.logInfo("-> No running Tool-Server instance found.");
                    }
                }
                throw new AbortException("Test executions aborted!");
            }
        } catch (final IOException e) {
            Util.displayIOException(e, listener);
            throw e;
        } catch (final ETPluginException e) {
            logger.logError(e.getMessage());
            throw new AbortException(e.getMessage());
        }
    }

    /**
     * Performs the test execution.
     *
     * @param run       the build
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if running the test passed, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private boolean performTest(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                                final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, listener, false)) {
            logger.logError("No running ECU-TEST instance found, please configure one at first!");
            return false;
        }

        // Expand build parameters
        final EnvVars buildEnv = run.getEnvironment(listener);
        final String expTestFile = buildEnv.expand(getTestFile());
        TestConfig expTestConfig = getTestConfig().expand(buildEnv);
        final ExecutionConfig expExecConfig = getExecutionConfig().expand(buildEnv);

        // Get test file path
        final String expPkgDir;
        if (IOUtils.isAbsolute(expTestFile)) {
            expPkgDir = null;
        } else {
            // Determine packages directory by COM API
            final String packageDir = getPackagesDir(launcher, listener);

            // Absolutize packages directory, if not absolute assume relative to ECU-TEST workspace
            expPkgDir = PathUtil.makeAbsolutePath(packageDir, workspace);
        }

        // Configure test file
        final String expTestFilePath = getTestFilePath(expTestFile, expPkgDir, launcher, listener);

        // Check test file existence
        if (expTestFilePath == null) {
            return false;
        }

        // Configurations not relevant if previous ones used
        String expTbcFilePath = null;
        String expTcfFilePath = null;
        if (!expTestConfig.isKeepConfig()) {
            // Get test configuration file paths
            String expTbcConfigDir = null;
            String expTcfConfigDir = null;
            final String tbcFile = expTestConfig.getTbcFile();
            final String tcfFile = expTestConfig.getTcfFile();
            if (!IOUtils.isAbsolute(tbcFile) || !IOUtils.isAbsolute(tcfFile)) {
                // Determine configuration directory by COM API
                final String configDir = getConfigDir(launcher, listener);

                // Absolutize configuration directory, if not absolute assume relative to ECU-TEST workspace
                final String expConfigDir = PathUtil.makeAbsolutePath(configDir, workspace);
                expTbcConfigDir = IOUtils.isAbsolute(tbcFile) ? null : expConfigDir;
                expTcfConfigDir = IOUtils.isAbsolute(tcfFile) ? null : expConfigDir;
            }

            // Configure test bench configuration file
            expTbcFilePath = getConfigFilePath(expTestConfig.getTbcFile(),
                expTbcConfigDir, launcher, listener);

            // Configure test configuration file
            expTcfFilePath = getConfigFilePath(expTestConfig.getTcfFile(),
                expTcfConfigDir, launcher, listener);

            // Check configuration file existence
            if (expTbcFilePath == null || expTcfFilePath == null) {
                return false;
            }
        }

        // Set expanded test configuration
        expTestConfig = new TestConfig(expTbcFilePath, expTcfFilePath, expTestConfig.isForceReload(),
            expTestConfig.isLoadOnly(), expTestConfig.isKeepConfig(), expTestConfig.getConstants());

        // Run tests
        return runTest(expTestFilePath, expTestConfig, expExecConfig, run, workspace, launcher, listener);
    }

    /**
     * Adds the build action holding test information by injecting environment variables.
     *
     * @param run        the run
     * @param testClient the test client
     */
    protected void addBuildAction(final Run<?, ?> run, final AbstractTestClient testClient) {
        final int builderId = getTestId(run);
        final TestEnvInvisibleAction envAction = new TestEnvInvisibleAction(builderId, testClient);
        run.addAction(envAction);
    }

    /**
     * Run the test with given configurations within a defined timeout.
     *
     * @param testFile        the full test file path
     * @param testConfig      the expanded test configuration
     * @param executionConfig the expanded execution configuration
     * @param run             the build
     * @param workspace       the workspace
     * @param launcher        the launcher
     * @param listener        the listener
     * @return {@code true} if running the test passed, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    protected abstract boolean runTest(String testFile, TestConfig testConfig, ExecutionConfig executionConfig,
                                       Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
        throws IOException, InterruptedException;

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public AbstractTestDescriptor getDescriptor() {
        return (AbstractTestDescriptor) super.getDescriptor();
    }
}
