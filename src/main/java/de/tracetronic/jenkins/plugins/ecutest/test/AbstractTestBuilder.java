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
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;
import hudson.tasks.Builder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jenkins.security.MasterToSlaveCallable;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.TSClient;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Common base class for all test related task builders implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractTestBuilder extends Builder {

    /**
     * Defines the default "Packages" directory in the ECU-TEST workspace.
     */
    private static final String DEFAULT_PACKAGES_DIR = "Packages";

    /**
     * Defines the default "Configurations" directory in the ECU-TEST workspace.
     */
    private static final String DEFAULT_CONFIG_DIR = "Configurations";

    private final String testFile;
    private final TestConfig testConfig;
    private final ExecutionConfig executionConfig;

    /**
     * Instantiates a new {@link AbstractTestBuilder}.
     *
     * @param testFile
     *            the test file
     * @param testConfig
     *            the test configuration
     * @param executionConfig
     *            the execution configuration
     */
    public AbstractTestBuilder(final String testFile, final TestConfig testConfig,
            final ExecutionConfig executionConfig) {
        super();
        this.testFile = StringUtils.trimToEmpty(testFile);
        this.testConfig = testConfig == null ? TestConfig.newInstance() : testConfig;
        this.executionConfig = executionConfig == null ? ExecutionConfig.newInstance() : executionConfig;
    }

    /**
     * @return the default packages directory
     */
    public String getDefaultPackagesDir() {
        return DEFAULT_PACKAGES_DIR;
    }

    /**
     * @return the default configurations directory
     */
    public String getDefaultConfigDir() {
        return DEFAULT_CONFIG_DIR;
    }

    /**
     * @return the test file path
     */
    public String getTestFile() {
        return testFile;
    }

    /**
     * @return the test configuration
     */
    public TestConfig getTestConfig() {
        return testConfig;
    }

    /**
     * @return the execution configuration
     */
    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        // Check OS running this build
        if (!ProcessUtil.checkOS(launcher, listener)) {
            return false;
        }

        final boolean performed = performTest(build, launcher, listener);
        if (!performed && getExecutionConfig().isStopOnError()) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
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
        return performed;
    }

    /**
     * Perform a test execution.
     *
     * @param build
     *            the build
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
    private boolean performTest(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, false)) {
            logger.logError("No running ECU-TEST instance found, please configure one at first!");
            return false;
        }

        // Expand build parameters
        final EnvVars buildEnv = build.getEnvironment(listener);
        final String expTestFile = buildEnv.expand(getTestFile());
        TestConfig expTestConfig = getTestConfig().expand(buildEnv);
        final ExecutionConfig expExecConfig = getExecutionConfig().expand(buildEnv);

        // Get test file path
        final String expPkgDir;
        final File testFile = new File(expTestFile);
        if (testFile.isAbsolute()) {
            expPkgDir = null;
        } else {
            // Determine packages directory by COM API
            final String packageDir = getPackagesDir(launcher, listener);

            // Absolutize packages directory, if not absolute assume relative to ECU-TEST workspace
            expPkgDir = PathUtil.makeAbsolutePath(packageDir, build.getWorkspace());
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
        final File tbcFile = new File(expTestConfig.getTbcFile());
        final File tcfFile = new File(expTestConfig.getTcfFile());
        if (!tbcFile.isAbsolute() || !tcfFile.isAbsolute()) {
            // Determine configuration directory by COM API
            final String configDir = getConfigDir(launcher, listener);

            // Absolutize configuration directory, if not absolute assume relative to ECU-TEST workspace
            final String expConfigDir = PathUtil.makeAbsolutePath(configDir, build.getWorkspace());
            expTbcConfigDir = tbcFile.isAbsolute() ? null : expConfigDir;
            expTcfConfigDir = tcfFile.isAbsolute() ? null : expConfigDir;
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
                expTestConfig.isLoadOnly(), expTestConfig.getConstants());

        // Run tests
        return runTest(expTestFilePath, expTestConfig, expExecConfig, build, launcher, listener);
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
     * @param build
     *            the build
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
            AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException,
            InterruptedException;

    /**
     * Checks already opened ECU-TEST instances.
     *
     * @param launcher
     *            the launcher
     * @param kill
     *            specifies whether to task-kill the running processes
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    private boolean checkETInstance(final Launcher launcher, final boolean kill) throws IOException,
            InterruptedException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, kill);
        return !foundProcesses.isEmpty();
    }

    /**
     * Tries to close already opened ECU-TEST instances via COM first.
     * If this is not successful tries to task-kill the running process.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    private boolean closeETInstance(final Launcher launcher, final BuildListener listener) throws IOException,
            InterruptedException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, false);
        if (foundProcesses.isEmpty()) {
            return false;
        }
        return ETClient.stopProcesses(launcher, listener, true);
    }

    /**
     * Checks already opened Tool-Server instances.
     *
     * @param launcher
     *            the launcher
     * @param kill
     *            specifies whether to task-kill the running processes
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    private boolean checkTSInstance(final Launcher launcher, final boolean kill) throws IOException,
            InterruptedException {
        final List<String> foundProcesses = TSClient.checkProcesses(launcher, kill);
        return !foundProcesses.isEmpty();
    }

    /**
     * Gets the test identifier by the size of {@link TestEnvInvisibleAction}s already added to the build.
     *
     * @param build
     *            the build
     * @return the test id
     */
    protected int getTestId(final AbstractBuild<?, ?> build) {
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        return testEnvActions.size();
    }

    /**
     * Gets the absolute test file path.
     *
     * @param testFile
     *            the expanded test file
     * @param pkgDir
     *            the packages directory containing the test file
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the absolute test file path
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    protected String getTestFilePath(final String testFile, final String pkgDir, final Launcher launcher,
            final BuildListener listener) throws IOException, InterruptedException {
        String testFilePath = null;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (testFile.isEmpty()) {
            logger.logError("No package or project file declared!");
        } else {
            final File fullTestFile = new File(pkgDir, testFile);
            final FilePath fullTestFilePath = new FilePath(launcher.getChannel(), fullTestFile.getPath());
            if (fullTestFilePath.exists()) {
                testFilePath = fullTestFilePath.getRemote();
            } else {
                logger.logError(String.format("%s does not exist!", fullTestFilePath.getRemote()));
            }
        }
        return testFilePath;
    }

    /**
     * Gets the absolute configuration file path.
     *
     * @param configFile
     *            the expanded configuration file
     * @param configDir
     *            the expanded configuration directory containing the configuration file
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the absolute configuration file path
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private String getConfigFilePath(final String configFile, final String configDir, final Launcher launcher,
            final BuildListener listener) throws IOException, InterruptedException {
        String configFilePath = null;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (configFile.isEmpty()) {
            configFilePath = configFile;
        } else {
            final File fullConfigFile = new File(configDir, configFile);
            final FilePath fullConfigFilePath = new FilePath(launcher.getChannel(), fullConfigFile.getPath());
            if (fullConfigFilePath.exists()) {
                configFilePath = fullConfigFilePath.getRemote();
            } else {
                logger.logError(String.format("%s does not exist!", fullConfigFilePath.getRemote()));
            }
        }
        return configFilePath;
    }

    /**
     * Gets the configuration directory of the current ECU-TEST workspace by querying the settings file via COM.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the configuration directory
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    protected String getConfigDir(final Launcher launcher, final BuildListener listener) throws InterruptedException {
        String configDir;
        try {
            configDir = launcher.getChannel().call(new GetSettingCallable("configPath"));
        } catch (final IOException e) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logWarn("Could not get config dir, assuming default values now!");
            configDir = getDefaultConfigDir();
        }
        return configDir;
    }

    /**
     * Gets the packages directory of the current ECU-TEST workspace by querying the settings file via COM.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the package directory
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    protected String getPackagesDir(final Launcher launcher, final BuildListener listener) throws InterruptedException {
        String packagesDir;
        try {
            packagesDir = launcher.getChannel().call(new GetSettingCallable("packagePath"));
        } catch (final IOException e) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logWarn("Could not get packages dir, assuming default values now!");
            packagesDir = getDefaultPackagesDir();
        }
        return packagesDir;
    }

    /**
     * {@link Callable} providing remote access to get a ECU-TEST workspace setting value via COM.
     */
    private static final class GetSettingCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final String settingName;

        /**
         * Instantiates a new {@link GetSettingCallable}.
         *
         * @param settingName
         *            the setting name to request
         */
        GetSettingCallable(final String settingName) {
            this.settingName = settingName;
        }

        @Override
        public String call() throws IOException {
            String settingValue;
            try (ETComClient comClient = new ETComClient()) {
                settingValue = comClient.getSetting(settingName);
                if ("None".equals(settingValue)) {
                    throw new IOException("Setting is not defined: " + settingName);
                }
            } catch (final ETComException e) {
                throw new IOException(e.getMessage(), e);
            }
            return settingValue;
        }
    }

    @Override
    public AbstractTestDescriptor getDescriptor() {
        return (AbstractTestDescriptor) super.getDescriptor();
    }
}
