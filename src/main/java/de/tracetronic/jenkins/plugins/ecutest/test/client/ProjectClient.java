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
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FilenameUtils;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Project;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestExecutionInfo;

/**
 * Client to execute ECU-TEST projects via COM interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ProjectClient extends AbstractTestClient {

    private final ProjectConfig projectConfig;

    /**
     * Instantiates a new {@link ProjectClient}.
     *
     * @param testFile
     *            the project file
     * @param testConfig
     *            the test configuration
     * @param projectConfig
     *            the project configuration
     * @param executionConfig
     *            the execution configuration
     */
    public ProjectClient(final String testFile, final TestConfig testConfig,
            final ProjectConfig projectConfig, final ExecutionConfig executionConfig) {
        super(testFile, testConfig, executionConfig);
        this.projectConfig = projectConfig;
    }

    /**
     * @return the project configuration
     */
    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    @Override
    public boolean runTestCase(final Launcher launcher, final BuildListener listener) throws IOException,
    InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Load JACOB library
        if (!DllUtil.loadLibrary()) {
            logger.logError("Could not load JACOB library!");
            return false;
        }

        // Load test configuration
        if (!launcher.getChannel().call(
                new LoadConfigCallable(getTestConfig(), getExecutionConfig(), listener))) {
            return false;
        }

        // Open project
        if (!launcher.getChannel().call(new OpenProjectCallable(getTestFile(), getProjectConfig(), listener))) {
            return false;
        }

        // Run project
        final TestInfoHolder testInfo = launcher.getChannel().call(
                new RunProjectCallable(getTestFile(), getProjectConfig(), getExecutionConfig(), listener));

        // Set default project information
        setTestDescription("");
        setTestName(FilenameUtils.getBaseName(new File(getTestFile()).getName()));

        // Set project information
        if (testInfo != null) {
            setTestResult(testInfo.getTestResult());
            setTestReportDir(testInfo.getTestReportDir());
        } else {
            return false;
        }

        // Close project
        if (!launcher.getChannel().call(new CloseProjectCallable(getTestFile(), listener))) {
            return false;
        }

        return true;
    }

    /**
     * {@link Callable} providing remote access to open a project via COM.
     */
    private static final class OpenProjectCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final String projectFile;
        private final ProjectConfig projectConfig;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link OpenProjectCallable}.
         *
         * @param projectFile
         *            the project file
         * @param projectConfig
         *            the project configuration
         * @param listener
         *            the listener
         */
        public OpenProjectCallable(final String projectFile, final ProjectConfig projectConfig,
                final BuildListener listener) {
            this.projectFile = projectFile;
            this.projectConfig = projectConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            final boolean execInCurrentPkgDir = projectConfig.isExecInCurrentPkgDir();
            final String filterExpression = projectConfig.getFilterExpression();
            boolean isOpened = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Opening project...");
            try (ETComClient comClient = new ETComClient();
                    Project project = (Project) comClient.openProject(projectFile, execInCurrentPkgDir,
                            filterExpression)) {
                logger.logInfo("-> Project opened successfully.");
            } catch (final ETComException e) {
                isOpened = false;
                logger.logError("-> Opening project failed!");
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isOpened;
        }
    }

    /**
     * {@link Callable} providing remote access to run a project via COM.
     */
    private static final class RunProjectCallable implements Callable<TestInfoHolder, IOException> {

        private static final long serialVersionUID = 1L;

        private final String projectFile;
        private final ProjectConfig projectConfig;
        private final ExecutionConfig executionConfig;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link RunProjectCallable}.
         *
         * @param projectFile
         *            the project file
         * @param projectConfig
         *            the project configuration
         * @param executionConfig
         *            the execution configuration
         * @param listener
         *            the listener
         */
        public RunProjectCallable(final String projectFile, final ProjectConfig projectConfig,
                final ExecutionConfig executionConfig, final BuildListener listener) {
            this.projectFile = projectFile;
            this.projectConfig = projectConfig;
            this.executionConfig = executionConfig;
            this.listener = listener;
        }

        @Override
        public TestInfoHolder call() throws IOException {
            final int jobExecutionMode = projectConfig.getJobExecutionMode();
            final int timeout = executionConfig.getTimeout();
            TestInfoHolder testInfo = null;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Running project...");
            try (ETComClient comClient = new ETComClient();
                    TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                    TestExecutionInfo execInfo = (TestExecutionInfo) testEnv.executeProject(projectFile,
                            jobExecutionMode)) {
                int tickCounter = 0;
                final long endTimeMillis = System.currentTimeMillis() + Long.valueOf(timeout) * 1000L;
                while ("RUNNING".equals(execInfo.getState())) {
                    if (tickCounter % 60 == 0) {
                        logger.logInfo("-- tick...");
                    }
                    if (timeout > 0 && System.currentTimeMillis() > endTimeMillis) {
                        execInfo.abort();
                        throw new TimeoutException("Test execution timeout reached! Aborting now...");
                    }
                    Thread.sleep(1000L);
                    tickCounter++;
                }

                final String testResult = execInfo.getResult();
                logger.logInfo(String.format("-> Project execution completed with result: %s", testResult));
                final String testReportDir = new File(execInfo.getReportDb()).getParentFile()
                        .getAbsolutePath();
                logger.logInfo(String.format("-> Test report directory: %s", testReportDir));
                testInfo = new TestInfoHolder(testResult, testReportDir);
            } catch (final ETComException e) {
                logger.logError("Caught ComException: " + e.getMessage());
            } catch (final TimeoutException e) {
                logger.logError("Caught TimeoutException: " + e.getMessage());
            } catch (final InterruptedException e) {
                logger.logError("Caught InterruptedException: " + e.getMessage());
            }
            return testInfo;
        }
    }

    /**
     * {@link Callable} providing remote access to close a project via COM.
     */
    private static final class CloseProjectCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final String projectFile;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link CloseProjectCallable}.
         *
         * @param projectFile
         *            the project file
         * @param listener
         *            the listener
         */
        public CloseProjectCallable(final String projectFile, final BuildListener listener) {
            this.projectFile = projectFile;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isClosed = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Closing project...");
            try (ETComClient comClient = new ETComClient()) {
                if (comClient.closeProject(projectFile)) {
                    isClosed = true;
                    logger.logInfo("-> Project closed successfully.");
                } else {
                    logger.logError("-> Closing project failed!");
                }
            } catch (final ETComException e) {
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isClosed;
        }
    }
}
