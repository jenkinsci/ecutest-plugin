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
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Package;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestExecutionInfo;

/**
 * Client to execute ECU-TEST packages via COM interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class PackageClient extends AbstractTestClient {

    private final PackageConfig packageConfig;

    /**
     * Instantiates a new {@link PackageClient}.
     *
     * @param testFile
     *            the package file
     * @param testConfig
     *            the test configuration
     * @param packageConfig
     *            the package configuration
     * @param executionConfig
     *            the execution configuration
     */
    public PackageClient(final String testFile, final TestConfig testConfig,
            final PackageConfig packageConfig, final ExecutionConfig executionConfig) {
        super(testFile, testConfig, executionConfig);
        this.packageConfig = packageConfig;
    }

    /**
     * @return the package configuration
     */
    public PackageConfig getPackageConfig() {
        return packageConfig;
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

        // Open package
        final PackageInfoHolder pkgInfo = launcher.getChannel().call(
                new OpenPackageCallable(getTestFile(), listener));

        // Set package information
        if (pkgInfo != null) {
            setTestName(pkgInfo.getTestName());
            setTestDescription(pkgInfo.getTestDescription());
        } else {
            return false;
        }

        // Run package
        final TestInfoHolder testInfo = launcher.getChannel().call(
                new RunPackageCallable(getTestFile(), getPackageConfig(), getExecutionConfig(), listener));

        // Set test result information
        if (testInfo != null) {
            setTestResult(testInfo.getTestResult());
            setTestReportDir(testInfo.getTestReportDir());
        } else {
            return false;
        }

        // Close package
        if (!launcher.getChannel().call(new ClosePackageCallable(getTestFile(), listener))) {
            return false;
        }

        return true;
    }

    /**
     * {@link Callable} providing remote access to open a package via COM.
     */
    private static final class OpenPackageCallable implements Callable<PackageInfoHolder, IOException> {

        private static final long serialVersionUID = 1L;

        private final String packageFile;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link OpenPackageCallable}.
         *
         * @param packageFile
         *            the package file
         * @param listener
         *            the listener
         */
        public OpenPackageCallable(final String packageFile, final BuildListener listener) {
            this.packageFile = packageFile;
            this.listener = listener;
        }

        @Override
        public PackageInfoHolder call() throws IOException {
            PackageInfoHolder pkgInfo = null;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Opening package...");
            try (ETComClient comClient = new ETComClient();
                    Package pkg = (Package) comClient.openPackage(packageFile)) {
                logger.logInfo("-> Package opened successfully.");
                pkgInfo = new PackageInfoHolder(pkg.getName(), pkg.getDescription());
            } catch (final ETComException e) {
                logger.logError("-> Opening package failed!");
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return pkgInfo;
        }
    }

    /**
     * {@link Callable} providing remote access to run a package via COM.
     */
    private static final class RunPackageCallable implements Callable<TestInfoHolder, IOException> {

        private static final long serialVersionUID = 1L;

        private final String packageFile;
        private final PackageConfig packageConfig;
        private final ExecutionConfig executionConfig;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link RunPackageCallable}.
         *
         * @param packageFile
         *            the package file
         * @param packageConfig
         *            the package configuration
         * @param executionConfig
         *            the execution configuration
         * @param listener
         *            the listener
         */
        public RunPackageCallable(final String packageFile, final PackageConfig packageConfig,
                final ExecutionConfig executionConfig, final BuildListener listener) {
            this.packageFile = packageFile;
            this.packageConfig = packageConfig;
            this.executionConfig = executionConfig;
            this.listener = listener;
        }

        @Override
        public TestInfoHolder call() throws IOException {
            final boolean runTest = packageConfig.isRunTest();
            final boolean runTraceAnalysis = packageConfig.isRunTest();
            final int timeout = executionConfig.getTimeout();
            TestInfoHolder testInfo = null;

            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Running package...");
            final Map<String, String> paramMap = getParameterMap();
            if (!paramMap.isEmpty()) {
                logger.logInfo("-> With parameters: " + paramMap.toString());
            }
            try (ETComClient comClient = new ETComClient();
                    TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                    TestExecutionInfo execInfo = (TestExecutionInfo) testEnv.executePackage(packageFile,
                            runTraceAnalysis, runTest, paramMap)) {
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
                logger.logInfo(String.format("-> Package execution completed with result: %s", testResult));
                final String testReportDir = new File(execInfo.getReportDb()).getParentFile()
                        .getAbsolutePath();
                logger.logInfo(String.format("-> Test report directory: %s", testReportDir));
                testInfo = new TestInfoHolder(testResult, testReportDir);
            } catch (final ETComException e) {
                logger.logError("Caught ComException: " + e.getMessage());
            } catch (final InterruptedException e) {
                logger.logError("Caught InterruptedException: " + e.getMessage());
            } catch (final TimeoutException e) {
                logger.logError("Caught TimeoutException: " + e.getMessage());
            }
            return testInfo;
        }

        /**
         * Converts the parameter list to a parameter map.
         *
         * @return the package parameter map
         */
        private Map<String, String> getParameterMap() {
            final Map<String, String> paramMap = new LinkedHashMap<String, String>();
            for (final PackageParameter param : packageConfig.getParameters()) {
                paramMap.put(param.getName(), param.getValue());
            }
            return paramMap;
        }
    }

    /**
     * {@link Callable} providing remote access to close a package via COM.
     */
    private static final class ClosePackageCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final String packageFile;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link ClosePackageCallable}.
         *
         * @param packageFile
         *            the package file
         * @param listener
         *            the listener
         */
        public ClosePackageCallable(final String packageFile, final BuildListener listener) {
            this.packageFile = packageFile;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isClosed = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Closing package...");
            try (ETComClient comClient = new ETComClient()) {
                if (comClient.closePackage(packageFile)) {
                    isClosed = true;
                    logger.logInfo("-> Package closed successfully.");
                } else {
                    logger.logError("-> Closing package failed!");
                }
            } catch (final ETComException e) {
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isClosed;
        }
    }

    /**
     * Helper class storing information about a package.
     */
    private static final class PackageInfoHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String testName;
        private final String testDescription;

        /**
         * Instantiates a new {@link PackageInfoHolder}.
         *
         * @param testName
         *            the test name
         * @param testDescription
         *            the test description
         */
        public PackageInfoHolder(final String testName, final String testDescription) {
            this.testName = testName;
            this.testDescription = testDescription;
        }

        /**
         * @return the test name
         */
        public String getTestName() {
            return testName;
        }

        /**
         * @return the test description
         */
        public String getTestDescription() {
            return testDescription;
        }
    }
}
