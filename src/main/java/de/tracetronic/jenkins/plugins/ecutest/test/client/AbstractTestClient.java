/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.client;

import de.tracetronic.jenkins.plugins.ecutest.extension.warnings.WarningsRecorder;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExpandableConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.GlobalConstant;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.AbstractTestObject;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Package;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestConfiguration;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Plugin;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Common base class for {@link PackageClient} and {@link ProjectClient}.
 */
public abstract class AbstractTestClient implements TestClient {

    private final String testFile;
    private final TestConfig testConfig;
    private final ExecutionConfig executionConfig;
    private String testName;
    private String testDescription;
    private String testReportDir;
    private String testResult;
    private boolean isAborted;

    /**
     * Instantiates a new {@link AbstractTestClient}.
     *
     * @param testFile        the test file path
     * @param testConfig      the test configuration
     * @param executionConfig the execution configuration
     */
    public AbstractTestClient(final String testFile, final TestConfig testConfig,
                              final ExecutionConfig executionConfig) {
        this.testFile = StringUtils.trimToEmpty(testFile);
        this.testConfig = testConfig;
        this.executionConfig = executionConfig;
        testName = "";
        testDescription = "";
        testReportDir = "";
        testResult = "";
        isAborted = false;
    }

    public String getTestFile() {
        return testFile;
    }

    public TestConfig getTestConfig() {
        return testConfig;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(final String testName) {
        this.testName = testName;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(final String testDescription) {
        this.testDescription = testDescription;
    }

    public String getTestReportDir() {
        return testReportDir;
    }

    public void setTestReportDir(final String testReportDir) {
        this.testReportDir = testReportDir;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(final String testResult) {
        this.testResult = testResult;
    }

    /**
     * Specifies whether the test execution is aborted.
     *
     * @return {@code true} if is aborted, {@code false} otherwise
     */
    public boolean isAborted() {
        return isAborted;
    }

    public void setAborted(final boolean isAborted) {
        this.isAborted = isAborted;
    }

    /**
     * Records test file checks as Warnings NG issues.
     *
     * @param testInfo  the stored test file information
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if recording detects any issues with ERROR severity, {@code false} otherwise
     */
    protected boolean recordWarnings(final TestInfoHolder testInfo, final Run<?, ?> run, final FilePath workspace,
                                     final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        final Plugin plugin = Jenkins.get().getPlugin("warnings-ng");
        if (plugin == null || !(plugin.getWrapper().isActive())) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logError("Warnings NG plugin not found or disabled, please install using the Update Center!");
            return true;
        }

        boolean hasErrors = false;
        if (StringUtils.isNotBlank(testInfo.warningsIssues)) {
            final String issueFileName = "issues.json";
            final FilePath issuesFile = workspace.child(issueFileName);
            try {
                issuesFile.write(testInfo.getWarningsIssues(), "UTF-8");

                final WarningsRecorder recorder = new WarningsRecorder(
                    "Package Check", testInfo.getTestName(), issueFileName);
                hasErrors = recorder.record(run, workspace, launcher, listener);
            } finally {
                issuesFile.delete();
            }
        }
        return hasErrors;
    }

    /**
     * {@link Callable} providing remote access to open a test file via COM.
     */
    protected abstract static class OpenTestFileCallable extends MasterToSlaveCallable<TestInfoHolder, IOException> {

        private final String testFile;
        private final ExpandableConfig testFileConfig;
        private final ExecutionConfig executionConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link OpenTestFileCallable}.
         *
         * @param testFile        the test file
         * @param testFileConfig  the test file configuration
         * @param executionConfig the execution configuration
         * @param listener        the listener
         */
        public OpenTestFileCallable(final String testFile, final ExpandableConfig testFileConfig,
                                    final ExecutionConfig executionConfig, final TaskListener listener) {
            this.testFile = testFile;
            this.testFileConfig = testFileConfig;
            this.executionConfig = executionConfig;
            this.listener = listener;
        }

        public String getTestFile() {
            return testFile;
        }

        public ExpandableConfig getTestFileConfig() {
            return testFileConfig;
        }

        public ExecutionConfig getExecutionConfig() {
            return executionConfig;
        }

        public TaskListener getListener() {
            return listener;
        }

        @Override
        public abstract TestInfoHolder call() throws IOException;

        @CheckForNull
        protected TestInfoHolder checkTestFile(final AbstractTestObject testObject, final ETComClient comClient,
                                               final TTConsoleLogger logger) throws ETComException {
            TestInfoHolder testInfo = new TestInfoHolder(testObject.getName(), testObject.getDescription());
            final String testType = testObject instanceof Package ? "package" : "project";
            if (executionConfig.isCheckTestFile()) {
                logger.logInfo(String.format("- Checking %s...", testType));
                if (executionConfig.isRecordWarnings()) {
                    final ToolVersion comVersion = ToolVersion.parse(comClient.getVersion());
                    if (comVersion.compareWithoutMicroTo(new ToolVersion(2020, 3, 0)) >= 0) {
                        logger.logInfo(String.format("-> Recording %s checks as Warnings NG issues...", testType));
                        final String checks = testObject.checkNG();
                        // Replace possible null values introduced by an issue in COM API
                        testInfo.setWarningsIssues(checks.replace("null", "0"));
                    } else {
                        logger.logInfo(String.format("-> Recording %s checks as Warnings NG issues will be skipped!",
                            testType));
                        logger.logWarn(String.format(
                            "The configured ECU-TEST version %s does not support recording WarningNG issues. "
                                + "Please use at least ECU-TEST 2020.3 or higher!", comVersion));
                    }
                } else {
                    final List<CheckInfoHolder> checks = testObject.check();
                    for (final CheckInfoHolder check : checks) {
                        final String logMessage = String.format("%s (line %s): %s", check.getFilePath(),
                            check.getLineNumber(), check.getErrorMessage());
                        final CheckInfoHolder.Seriousness seriousness = check.getSeriousness();
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
                        logger.logInfo(String.format("-> %s validated successfully!",
                            StringUtils.capitalize(testType)));
                    }
                }
            }
            return testInfo;
        }
    }

    /**
     * {@link Callable} providing remote access to load configurations via COM.
     */
    protected static final class LoadConfigCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final TestConfig testConfig;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link LoadConfigCallable}.
         *
         * @param testConfig the test configuration
         * @param listener   the listener
         */
        public LoadConfigCallable(final TestConfig testConfig, final TaskListener listener) {
            this.testConfig = testConfig;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isLoaded = false;
            final String tbcFile = testConfig.getTbcFile();
            final String tcfFile = testConfig.getTcfFile();
            final List<GlobalConstant> constants = testConfig.getConstants();
            final TTConsoleLogger logger = new TTConsoleLogger(listener);

            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final String tbcName = getConfigName(tbcFile);
                final String tcfName = getConfigName(tcfFile);
                logger.logInfo(String.format("- Loading configurations: TBC=%s TCF=%s", tbcName, tcfName));
                logger.logDebug(String.format("TBC=%s", tbcFile));
                logger.logDebug(String.format("TCF=%s", tcfFile));
                if (testConfig.isForceReload()) {
                    logger.logInfo("-> Forcing reload configurations...");
                    comClient.stop();
                }
                if (comClient.openTestConfiguration(StringUtils.defaultIfBlank(tcfFile, null))) {
                    if (tcfFile != null && !constants.isEmpty()) {
                        final Map<String, String> constantMap = getGlobalConstantMap();
                        logger.logInfo("-> With global constants: " + constantMap.toString());
                        setGlobalConstants(comClient, constantMap);
                    }
                    logger.logInfo("-> Test configuration loaded successfully.");
                } else {
                    logger.logError(String.format("-> Loading TCF=%s failed!", tcfName));
                }
                if (comClient.openTestbenchConfiguration(StringUtils.defaultIfBlank(tbcFile, null))) {
                    logger.logInfo("-> Test bench configuration loaded successfully.");
                    isLoaded = true;
                } else {
                    logger.logError(String.format("-> Loading TBC=%s failed!", tbcName));
                }
                if (isLoaded) {
                    if (testConfig.isLoadOnly()) {
                        logger.logInfo("-> Starting configurations will be skipped.");
                    } else {
                        logger.logInfo("- Starting configurations...");
                        comClient.start();
                        logger.logInfo("-> Configurations started successfully.");
                    }
                }
            } catch (final ETComException e) {
                logger.logComException(e);
                isLoaded = false;
            }
            return isLoaded;
        }

        /**
         * Gets the name of the given configuration file.
         *
         * @param configFile the configuration file
         * @return the configuration name
         */
        private String getConfigName(final String configFile) {
            final String configName;
            if (StringUtils.isBlank(configFile)) {
                configName = "None";
            } else {
                configName = new File(configFile).getName();
            }
            return configName;
        }

        /**
         * Sets the new global constants for the currently loaded test configuration. This requires to start the
         * configuration, add the constants and reload the configuration.
         *
         * @param comClient   the COM client
         * @param constantMap the constants to set
         * @throws ETComException in case of a COM exception
         */
        private void setGlobalConstants(final ETComClient comClient, final Map<String, String> constantMap)
            throws ETComException {
            comClient.start();
            final TestConfiguration testConfig = (TestConfiguration) comClient.getCurrentTestConfiguration();
            for (final Entry<String, String> newConstant : constantMap.entrySet()) {
                testConfig.setGlobalConstant(newConstant.getKey(), newConstant.getValue());
            }
            comClient.stop();
        }

        /**
         * Converts the global constant list to a map.
         *
         * @return the global constant map
         */
        private Map<String, String> getGlobalConstantMap() {
            final Map<String, String> constantMap = new LinkedHashMap<>();
            for (final GlobalConstant constant : testConfig.getConstants()) {
                constantMap.put(constant.getName(), constant.getValue());
            }
            return constantMap;
        }
    }

    /**
     * Helper class storing information about a test file.
     */
    protected static class TestInfoHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String testName;
        private final String testDescription;
        private String warningsIssues = "";

        /**
         * Instantiates a new {@link TestInfoHolder}.
         *
         * @param testName        the test name
         * @param testDescription the test description
         */
        public TestInfoHolder(final String testName, final String testDescription) {
            this.testName = testName;
            this.testDescription = testDescription;
        }

        public String getTestName() {
            return testName;
        }

        public String getTestDescription() {
            return testDescription;
        }

        public String getWarningsIssues() {
            return warningsIssues;
        }

        public void setWarningsIssues(final String warningsIssues) {
            this.warningsIssues = warningsIssues;
        }
    }

    /**
     * Helper class storing execution information about the test result and the test report directory.
     */
    protected static class ExecutionInfoHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String testResult;
        private final String testReportDir;
        private final boolean isAborted;

        /**
         * Instantiates a new {@link ExecutionInfoHolder}.
         *
         * @param testResult    the test result
         * @param testReportDir the test report directory
         * @param isAborted     specifies whether test execution is aborted
         */
        public ExecutionInfoHolder(final String testResult, final String testReportDir, final boolean isAborted) {
            this.testResult = testResult;
            this.testReportDir = testReportDir;
            this.isAborted = isAborted;
        }

        public String getTestResult() {
            return testResult;
        }

        public String getTestReportDir() {
            return testReportDir;
        }

        public boolean isAborted() {
            return isAborted;
        }
    }

    /**
     * Helper class storing information about the errors returned by checking packages and projects.
     */
    public static final class CheckInfoHolder {

        private final String filePath;
        private final Seriousness seriousness;
        private final String errorMessage;
        private final String lineNumber;

        /**
         * Instantiates a new {@link CheckInfoHolder}.
         *
         * @param filePath     the file path
         * @param seriousness  the seriousness
         * @param errorMessage the error message
         * @param lineNumber   the line number
         */
        public CheckInfoHolder(final String filePath, final Seriousness seriousness, final String errorMessage,
                               final String lineNumber) {
            super();
            this.filePath = filePath;
            this.seriousness = seriousness;
            this.errorMessage = errorMessage;
            this.lineNumber = lineNumber;
        }

        public String getFilePath() {
            return filePath;
        }

        public Seriousness getSeriousness() {
            return seriousness;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getLineNumber() {
            return lineNumber;
        }

        /**
         * Defines the seriousness types for checks.
         */
        public enum Seriousness {
            /**
             * Seriousness indicating the check is informational only.
             */
            NOTE,

            /**
             * Seriousness indicating the check represents a warning.
             */
            WARNING,

            /**
             * Seriousness indicating the check represents an error.
             */
            ERROR
        }
    }
}
