/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder.Seriousness;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageOutputParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Package;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestExecutionInfo;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client to execute ECU-TEST packages via COM interface.
 */
public class PackageClient extends AbstractTestClient {

    private final PackageConfig packageConfig;
    private Map<String, String> outputParameters;

    /**
     * Instantiates a new {@link PackageClient}.
     *
     * @param testFile        the package file
     * @param testConfig      the test configuration
     * @param packageConfig   the package configuration
     * @param executionConfig the execution configuration
     */
    public PackageClient(final String testFile, final TestConfig testConfig,
                         final PackageConfig packageConfig, final ExecutionConfig executionConfig) {
        super(testFile, testConfig, executionConfig);
        this.packageConfig = packageConfig;
    }

    /**
     * Gets package config.
     *
     * @return the package config
     */
    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    /**
     * Gets output parameters.
     *
     * @return the output parameters
     */
    public Map<String, String> getOutputParameters() {
        return outputParameters;
    }

    /**
     * Sets output parameters.
     *
     * @param outParams the out params
     */
    public void setOutputParameters(final Map<String, String> outParams) {
        this.outputParameters = outParams;
    }

    @Override
    public boolean runTestCase(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                               final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Load JACOB library
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }

        // Load test configuration
        if (!getTestConfig().isKeepConfig() && !launcher.getChannel().call(
            new LoadConfigCallable(getTestConfig(), listener))) {
            return false;
        }

        // Open and check package
        final TestInfoHolder pkgInfo = launcher.getChannel().call(
                new OpenPackageCallable(getTestFile(), getExecutionConfig().isCheckTestFile(),
                        getExecutionConfig().isRecordWarnings(), listener));

        // Set package information
        if (pkgInfo != null) {
            setTestName(pkgInfo.getTestName());
            setTestDescription(pkgInfo.getTestDescription());
            if (!recordWarnings(pkgInfo, run, workspace, launcher, listener)) {
                return false;
            }
        } else {
            return false;
        }

        try {
            // Run package
            final PackageExecutioInfoHolder testInfo = launcher.getChannel().call(
                new RunPackageCallable(getTestFile(), getPackageConfig(), getExecutionConfig(), listener));

            // Set test result information
            if (testInfo != null) {
                setTestResult(testInfo.getTestResult());
                setTestReportDir(testInfo.getTestReportDir());
                setAborted(testInfo.isAborted());
                setOutputParameters(testInfo.getOutputParameters());
            } else {
                return false;
            }
        } catch (final InterruptedException e) {
            logger.logError("Test execution has been interrupted!");
            return false;
        }

        // Close package
        return launcher.getChannel().call(new ClosePackageCallable(getTestFile(), listener));
    }



    /**
     * {@link Callable} providing remote access to open and check a package via COM.
     */
    private static final class OpenPackageCallable extends MasterToSlaveCallable<TestInfoHolder, IOException> {

        private static final long serialVersionUID = 1L;

        private final String packageFile;
        private final boolean checkTestFile;
        private final boolean recordWarnings;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link OpenPackageCallable}.
         *
         * @param packageFile    the package file
         * @param checkTestFile  specifies whether to check the package file
         * @param recordWarnings specifies whether to record returned package checks as Warnings NG issues
         * @param listener       the listener
         */
        OpenPackageCallable(final String packageFile, final boolean checkTestFile, final boolean recordWarnings,
                            final TaskListener listener) {
            this.packageFile = packageFile;
            this.checkTestFile = checkTestFile;
            this.recordWarnings = recordWarnings;
            this.listener = listener;
        }

        @Override
        public TestInfoHolder call() throws IOException {
            TestInfoHolder testInfo = null;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Opening package...");
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId);
                 Package pkg = (Package) comClient.openPackage(packageFile)) {
                logger.logInfo("-> Package opened successfully.");
                testInfo = new TestInfoHolder(pkg.getName(), pkg.getDescription());
                if (checkTestFile) {
                    logger.logInfo("- Checking package...");
                    if (recordWarnings) {
                        final ToolVersion comVersion = ToolVersion.parse(comClient.getVersion());
                        if (comVersion.compareWithoutMicroTo(new ToolVersion(2020, 3, 0)) >= 0) {
                            logger.logInfo("-> Recording package checks as Warnings NG issues...");
                            final String checks = pkg.checkNG();
                            // Replace possible null values introduced by an issue in COM API
                            testInfo.setWarningsIssues(checks.replace("null", "0"));
                        } else {
                            logger.logInfo("-> Recording package checks as Warnings NG issues will be skipped!");
                            logger.logWarn(String.format(
                                    "The configured ECU-TEST version %s does not support recording WarningNG issues. "
                                            + "Please use at least ECU-TEST 2020.3 or higher!", comVersion));
                        }
                    } else {
                        final List<CheckInfoHolder> checks = pkg.check();
                        for (final CheckInfoHolder check : checks) {
                            final String logMessage = String.format("%s (line %s): %s", check.getFilePath(),
                                    check.getLineNumber(), check.getErrorMessage());
                            final Seriousness seriousness = check.getSeriousness();
                            switch (seriousness) {
                                case NOTE:
                                    logger.logInfo(logMessage);
                                    break;
                                case WARNING:
                                    logger.logWarn(logMessage);
                                    break;
                                case ERROR:
                                    logger.logError(logMessage);
                                    testInfo = null;
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (checks.isEmpty()) {
                            logger.logInfo("-> Package validated successfully.");
                        }
                    }
                }
            } catch (final ETComException e) {
                logger.logComException("-> Opening package failed", e);
            }
            return testInfo;
        }
    }

    /**
     * {@link Callable} providing remote access to run a package via COM.
     */
    private static final class RunPackageCallable extends MasterToSlaveCallable<PackageExecutioInfoHolder, IOException> {

        private static final long serialVersionUID = 1L;

        private final String packageFile;
        private final PackageConfig packageConfig;
        private final ExecutionConfig executionConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link RunPackageCallable}.
         *
         * @param packageFile     the package file
         * @param packageConfig   the package configuration
         * @param executionConfig the execution configuration
         * @param listener        the listener
         */
        RunPackageCallable(final String packageFile, final PackageConfig packageConfig,
                           final ExecutionConfig executionConfig, final TaskListener listener) {
            this.packageFile = packageFile;
            this.packageConfig = packageConfig;
            this.executionConfig = executionConfig;
            this.listener = listener;
        }

        @Override
        public PackageExecutioInfoHolder call() throws IOException {
            final boolean runTest = packageConfig.isRunTest();
            final boolean runTraceAnalysis = packageConfig.isRunTraceAnalysis();
            final int timeout = executionConfig.getParsedTimeout();
            PackageExecutioInfoHolder testInfo = null;

            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Running package...");
            final Map<String, String> paramMap = getParameterMap();
            if (!paramMap.isEmpty()) {
                logger.logInfo("-> With parameters: " + paramMap.toString());
            }
            final List<String> outParamList =
                    packageConfig.getOutputParameters().stream().map(PackageOutputParameter::getName).collect(
                        Collectors.toList());
            if (!outParamList.isEmpty()) {
                logger.logInfo("-> With output parameters: " + outParamList.toString());
            }
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId);
                 TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                 TestExecutionInfo execInfo = (TestExecutionInfo) testEnv.executePackage(packageFile,
                     runTraceAnalysis, runTest, paramMap)) {
                boolean isAborted = false;
                int tickCounter = 0;
                final long endTimeMillis = System.currentTimeMillis() + (long) timeout * 1000L;
                while ("RUNNING".equals(execInfo.getState())) {
                    if (tickCounter % 60 == 0) {
                        logger.logInfo("-- tick...");
                    }
                    if (timeout > 0 && System.currentTimeMillis() > endTimeMillis) {
                        logger.logWarn(String.format("-> Test execution timeout of %d seconds reached! "
                            + "Aborting package now...", timeout));
                        isAborted = true;
                        execInfo.abort();
                        break;
                    }
                    Thread.sleep(1000L);
                    tickCounter++;
                }
                testInfo = getTestInfo(execInfo, isAborted, logger, outParamList);
                postExecution(timeout, comClient, logger);
            } catch (final ETComException e) {
                logger.logComException(e);
            } catch (final InterruptedException e) {
                testInfo = abortTestExecution(timeout, progId, logger, outParamList);
            }
            return testInfo;
        }

        /**
         * Converts the parameter list to a parameter map.
         *
         * @return the package parameter map
         */
        private Map<String, String> getParameterMap() {
            final Map<String, String> paramMap = new LinkedHashMap<>();
            for (final PackageParameter param : packageConfig.getParameters()) {
                paramMap.put(param.getName(), param.getValue());
            }
            return paramMap;
        }

        /**
         * Gets the information of the executed package.
         *
         * @param execInfo      the execution info
         * @param isAborted     specifies whether the package execution is aborted
         * @param logger        the logger
         * @param outParamList  the output parameter list
         * @return the test information
         * @throws ETComException in case of a COM exception
         */
        private PackageExecutioInfoHolder getTestInfo(final TestExecutionInfo execInfo, final boolean isAborted,
                                                      final TTConsoleLogger logger, final List<String> outParamList)
            throws ETComException {

            final String testResult = execInfo.getResult();
            logger.logInfo(String.format("-> Package execution %s with result: %s",
                isAborted ? "aborted" : "completed", testResult));
            final String testReportDir = new File(execInfo.getReportDb()).getParentFile().getAbsolutePath();
            logger.logInfo(String.format("-> Test report directory: %s", testReportDir));
            final Map<String, String> outParamMap =
                outParamList.stream().collect(Collectors.toMap(e -> e.toUpperCase(Locale.getDefault()), e -> {
                    try {
                        return execInfo.getReturnValue(e);
                    } catch (final ETComException exception) {
                        logger.logComException(exception);
                        return "";
                    }
                }));

            return new PackageExecutioInfoHolder(testResult, testReportDir, isAborted, outParamMap);
        }

        /**
         * Aborts the test execution.
         *
         * @param timeout       the timeout
         * @param progId        the programmatic id
         * @param logger        the logger
         * @param outParamList  the output parameter list
         * @return the test information
         */
        private PackageExecutioInfoHolder abortTestExecution(final int timeout, final String progId,
                                                             final TTConsoleLogger logger,
                                                             final List<String> outParamList) {
            PackageExecutioInfoHolder testInfo = null;
            try (ETComClient comClient = new ETComClient(progId);
                 TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                 TestExecutionInfo execInfo = (TestExecutionInfo) testEnv.getTestExecutionInfo()) {
                logger.logWarn("-> Build interrupted! Aborting test exection...");
                execInfo.abort();
                testInfo = getTestInfo(execInfo, true, logger, outParamList);
                postExecution(timeout, comClient, logger);
            } catch (final ETComException e) {
                logger.logComException(e);
            }
            return testInfo;
        }

        /**
         * Timeout handling for post execution.
         *
         * @param timeout   the timeout
         * @param comClient the COM client
         * @param logger    the logger
         * @throws ETComException in case of a COM exception
         */
        private void postExecution(final int timeout, final ETComClient comClient, final TTConsoleLogger logger)
            throws ETComException {
            if (!comClient.waitForIdle(timeout)) {
                logger.logWarn(String.format("-> Post-execution timeout of %d seconds reached!", timeout));
            }
        }
    }

    /**
     * {@link Callable} providing remote access to close a package via COM.
     */
    private static final class ClosePackageCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final String packageFile;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ClosePackageCallable}.
         *
         * @param packageFile the package file
         * @param listener    the listener
         */
        ClosePackageCallable(final String packageFile, final TaskListener listener) {
            this.packageFile = packageFile;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isClosed = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo("- Closing package...");
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                if (comClient.closePackage(packageFile)) {
                    isClosed = true;
                    logger.logInfo("-> Package closed successfully.");
                } else {
                    logger.logError("-> Closing package failed!");
                }
            } catch (final ETComException e) {
                logger.logComException(e);
            }
            return isClosed;
        }
    }

    /**
     * Helper class storing package execution information.
     */
    protected static final class PackageExecutioInfoHolder extends ExecutionInfoHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Map<String, String> outputParameters;

        /**
         * Instantiates a new {@link PackageExecutioInfoHolder}.
         *
         * @param testResult        the test result
         * @param testReportDir     the test report directory
         * @param isAborted         specifies whether test execution is aborted
         * @param outputParameters  the output parameter map
         */
        public PackageExecutioInfoHolder(final String testResult, final String testReportDir, final boolean isAborted,
                                         final Map<String, String> outputParameters) {
            super(testResult, testReportDir, isAborted);
            this.outputParameters = outputParameters;
        }

        public Map<String, String> getOutputParameters() {
            return outputParameters;
        }
    }
}
