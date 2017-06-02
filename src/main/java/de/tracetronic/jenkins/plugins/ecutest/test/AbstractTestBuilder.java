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

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.tasks.BuildStepMonitor;
import hudson.util.IOUtils;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

/**
 * Common base class for all test related task builders implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
     * @param testFile
     *            the test file
     */
    public AbstractTestBuilder(final String testFile) {
        super();
        this.testFile = StringUtils.trimToEmpty(testFile);
    }

    /**
     * Instantiates a new {@link AbstractTestBuilder}.
     *
     * @param testFile
     *            the test file
     * @param testConfig
     *            the test configuration
     * @param executionConfig
     *            the execution configuration
     * @deprecated since 1.11 use {@link #AbstractTestBuilder(String)}
     */
    @Deprecated
    public AbstractTestBuilder(final String testFile, final TestConfig testConfig,
            final ExecutionConfig executionConfig) {
        super();
        this.testFile = StringUtils.trimToEmpty(testFile);
        this.testConfig = testConfig == null ? TestConfig.newInstance() : testConfig;
        this.executionConfig = executionConfig == null ? ExecutionConfig.newInstance() : executionConfig;
    }

    /**
     * @return the test file path
     */
    @Nonnull
    public String getTestFile() {
        return testFile;
    }

    /**
     * @return the test configuration
     */
    @Nonnull
    public TestConfig getTestConfig() {
        return testConfig;
    }

    /**
     * @return the execution configuration
     */
    @Nonnull
    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    /**
     * @param testConfig
     *            the test configuration
     */
    @DataBoundSetter
    public void setTestConfig(@CheckForNull final TestConfig testConfig) {
        this.testConfig = testConfig == null ? TestConfig.newInstance() : testConfig;
    }

    /**
     * @param executionConfig
     *            the execution configuration
     */
    @DataBoundSetter
    public void setExecutionConfig(@CheckForNull final ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig == null ? ExecutionConfig.newInstance() : executionConfig;
    }

    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        // FIXME: workaround because pipeline node allocation does not create the actual workspace directory
        if (!workspace.exists()) {
            workspace.mkdirs();
        }

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
     * @param run
     *            the build
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if running the test passed, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean performTest(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, false)) {
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
        final String expTbcFilePath = getConfigFilePath(expTestConfig.getTbcFile(),
                expTbcConfigDir, launcher, listener);

        // Configure test configuration file
        final String expTcfFilePath = getConfigFilePath(expTestConfig.getTcfFile(),
                expTcfConfigDir, launcher, listener);

        // Check configuration file existence
        if (expTbcFilePath == null || expTcfFilePath == null) {
            return false;
        }

        // Set expanded test configuration
        expTestConfig = new TestConfig(expTbcFilePath, expTcfFilePath, expTestConfig.isForceReload(),
                expTestConfig.isLoadOnly(), expTestConfig.isKeepConfig(), expTestConfig.getConstants());

        // Run tests
        return runTest(expTestFilePath, expTestConfig, expExecConfig, run, workspace, launcher, listener);
    }

    /**
     * Run the test with given configurations within a defined timeout.
     *
     * @param testFile
     *            the full test file path
     * @param testConfig
     *            the expanded test configuration
     * @param executionConfig
     *            the expanded execution configuration
     * @param run
     *            the build
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if running the test passed, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
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
