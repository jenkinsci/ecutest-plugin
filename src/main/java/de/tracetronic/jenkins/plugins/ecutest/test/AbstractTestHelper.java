/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.TSClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.tasks.Builder;
import jenkins.security.MasterToSlaveCallable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Helper class providing common used functionalities for all test related task builders.
 */
public abstract class AbstractTestHelper extends Builder {

    /**
     * Defines the default "Packages" directory in the ECU-TEST workspace.
     */
    private static final String DEFAULT_PACKAGES_DIR = "Packages";

    /**
     * Defines the default "Configurations" directory in the ECU-TEST workspace.
     */
    private static final String DEFAULT_CONFIG_DIR = "Configurations";

    public String getDefaultPackagesDir() {
        return DEFAULT_PACKAGES_DIR;
    }

    public String getDefaultConfigDir() {
        return DEFAULT_CONFIG_DIR;
    }

    /**
     * Checks already opened ECU-TEST instances.
     *
     * @param launcher the launcher
     * @param kill     specifies whether to task-kill the running processes
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    protected boolean checkETInstance(final Launcher launcher, final TaskListener listener, final boolean kill)
        throws IOException, InterruptedException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, kill);
        return !foundProcesses.isEmpty();
    }

    /**
     * Tries to close already opened ECU-TEST instances via COM first.
     * If this is not successful tries to task-kill the running process.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    protected boolean closeETInstance(final Launcher launcher, final TaskListener listener) throws IOException,
        InterruptedException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        if (foundProcesses.isEmpty()) {
            return false;
        }
        return ETClient.stopProcesses(launcher, listener, true);
    }

    /**
     * Checks already opened Tool-Server instances.
     *
     * @param launcher the launcher
     * @param kill     specifies whether to task-kill the running processes
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    protected boolean checkTSInstance(final Launcher launcher, final boolean kill) throws IOException,
        InterruptedException {
        final List<String> foundProcesses = TSClient.checkProcesses(launcher, kill);
        return !foundProcesses.isEmpty();
    }

    /**
     * Gets the test identifier by the size of {@link TestEnvInvisibleAction}s already added to the build.
     *
     * @param run the build
     * @return the test id
     */
    protected int getTestId(final Run<?, ?> run) {
        final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
        return testEnvActions.size();
    }

    /**
     * Gets the absolute test file path.
     *
     * @param testFile the expanded test file
     * @param pkgDir   the packages directory containing the test file
     * @param launcher the launcher
     * @param listener the listener
     * @return the absolute test file path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    protected String getTestFilePath(final String testFile, final String pkgDir, final Launcher launcher,
                                     final TaskListener listener) throws IOException, InterruptedException {
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
     * @param configFile the expanded configuration file
     * @param configDir  the expanded configuration directory containing the configuration file
     * @param launcher   the launcher
     * @param listener   the listener
     * @return the absolute configuration file path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    protected String getConfigFilePath(final String configFile, final String configDir, final Launcher launcher,
                                       final TaskListener listener) throws IOException, InterruptedException {
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
     * @param launcher the launcher
     * @param listener the listener
     * @return the configuration directory
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    protected String getConfigDir(final Launcher launcher, final TaskListener listener) throws InterruptedException {
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
     * @param launcher the launcher
     * @param listener the listener
     * @return the package directory
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    protected String getPackagesDir(final Launcher launcher, final TaskListener listener) throws InterruptedException {
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
    public static final class GetSettingCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final String settingName;

        /**
         * Instantiates a new {@link GetSettingCallable}.
         *
         * @param settingName the setting name to request
         */
        public GetSettingCallable(final String settingName) {
            this.settingName = settingName;
        }

        @Override
        public String call() throws IOException {
            final String settingValue;
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
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
}
