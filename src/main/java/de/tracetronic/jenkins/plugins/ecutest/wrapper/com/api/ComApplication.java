/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

import java.util.List;

/**
 * Represents the ecu.test specific COMApplication API.
 */
public interface ComApplication {

    /**
     * Starts up the currently loaded test configuration and testbench configuration files.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestEnvironment start() throws ETComException;

    /**
     * Stops the currently loaded test configuration and testbench configuration files.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestEnvironment stop() throws ETComException;

    /**
     * Returns the test environment.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestEnvironment getTestEnvironment() throws ETComException;

    /**
     * Returns the analysis environment.
     *
     * @return the {@link ComAnalysisEnvironment} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComAnalysisEnvironment getAnalysisEnvironment() throws ETComException;

    /**
     * Returns the test management module.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestManagement getTestManagement() throws ETComException;

    /**
     * Returns the cache module.
     *
     * @return the {@link ComCaches} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComCaches getCaches() throws ETComException;

    /**
     * Checks if the application process is running and ready to use.
     *
     * @return {@code true} if application is already running, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean isApplicationRunning() throws ETComException;

    /**
     * Queries the COM-Application version.
     *
     * @return the version string
     * @throws ETComException in case of a COM exception
     */
    String getVersion() throws ETComException;

    /**
     * Queries the COM-Application setting value by name.
     * Possible setting names are:
     * <ul>
     * <li>configPath</li>
     * <li>errorLogFile</li>
     * <li>generatorPath</li>
     * <li>language</li>
     * <li>logFile</li>
     * <li>packagePath</li>
     * <li>reportPath</li>
     * <li>templatePath</li>
     * <li>parameterPath</li>
     * <li>traceStepPath</li>
     * <li>userPyModulesPath</li>
     * <li>utilityPath</li>
     * <li>workspacePath</li>
     * <li>offlineModelPath</li>
     * <li>offlineSgbdPath</li>
     * <li>offlineFiuPath</li>
     * <li>settingsPath</li>
     * </ul>
     *
     * @param settingName the setting name
     * @return the setting value or {@code null} if not defined
     * @throws ETComException in case of a COM exception
     */
    String getSetting(String settingName) throws ETComException;

    /**
     * Queries the list of loaded patches of the COM-Application.
     *
     * @return the list of loaded patches
     * @throws ETComException in case of a COM exception
     */
    List<String> getLoadedPatches() throws ETComException;

    /**
     * Exits the currently running instance of the application (Soft Exit).
     * The optional timeout parameter was introduced with ecu.test 8.0.
     *
     * @param timeout the timeout in seconds before giving up to wait for application shutdown and raising an exception
     * @return {@code true} if successful
     * @throws ETComException in case of a COM exception
     */
    boolean quit(int timeout) throws ETComException;

    /**
     * Exits the currently running instance of the application (Hard Exit), prefer {@link #quit(int)} instead.
     * The optional timeout parameter was introduced with ecu.test 8.0.
     *
     * @param timeout the timeout in seconds before giving up to wait for application shutdown and raising an exception
     * @return {@code true} if successful
     * @throws ETComException in case of a COM exception
     */
    boolean exit(int timeout) throws ETComException;

    /**
     * Opens an existing package in COM-Application.
     *
     * @param path the full path name of the package to open
     * @return the {@link ComPackage} dispatch, if the package is successfully opened, {@code null} otherwise
     * @throws ETComException in case of a COM exception
     */
    ComPackage openPackage(String path) throws ETComException;

    /**
     * Closes a package.
     *
     * @param path the full path name of the package to close
     * @return {@code true} if the package was closed, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean closePackage(String path) throws ETComException;

    /**
     * Opens an existing project in COM-Application.
     *
     * @param path                the full path name of the project to open
     * @param execInCurrentPkgDir defines whether relative references in the project are
     *                            resolved starting from the current workspaces
     *                            package directory or from the project file location
     * @param filterExpression    a valid filter expression (see the main help document, section 'Projects')
     * @return the {@link ComProject} dispatch, if the project is successfully opened, {@code null} otherwise
     * @throws ETComException in case of a COM exception
     */
    ComProject openProject(String path, boolean execInCurrentPkgDir, String filterExpression)
        throws ETComException;

    /**
     * Closes a project.
     *
     * @param path the full path name of the project to close
     * @return {@code true} if the project was closed, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean closeProject(String path) throws ETComException;

    /**
     * Imports a project from an archive.
     *
     * @param path             the full path name of the project to import
     * @param importPath       the full path name or a relative directory to the default package directory
     *                         as the projects/packages destination directory
     * @param importConfigPath the full path name or a relative directory to the default configuration directory
     *                         as the configurations destination directory
     * @param replaceFiles     specifies whether files of same name should be replaced or left untouched
     * @return the {@link ComPackage} dispatch, if the project was successfully imported, {@code null} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importProject(String path, String importPath, String importConfigPath, boolean replaceFiles)
        throws ETComException;

    /**
     * Opens a test bench configuration file (*.tbc).
     *
     * @param path the full path name of the test bench configuration file to open
     * @return {@code true} if the configuration was successfully opened, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean openTestbenchConfiguration(String path) throws ETComException;

    /**
     * Opens a test configuration file (*.tcf).
     *
     * @param path the full path name of the test configuration file to open
     * @return {@code true} if the configuration was successfully opened, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean openTestConfiguration(String path) throws ETComException;

    /**
     * Provides access to settings of the currently active test configuration file.
     *
     * @return the {@link ComTestConfiguration} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestConfiguration getCurrentTestConfiguration() throws ETComException;

    /**
     * Provides access to settings of the currently active test bench configuration file.
     *
     * @return the {@link ComTestBenchConfiguration} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestBenchConfiguration getCurrentTestBenchConfiguration() throws ETComException;

    /**
     * Returns whether the currently selected test configuration and testbench configuration are started.
     *
     * @return {@code true} if configurations are started, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean isStarted() throws ETComException;

    /**
     * Waits until the job count in the task manager reaches zero. The timeout parameter specifies the maximum waiting
     * time in seconds.
     *
     * @param timeout the timeout in seconds
     * @return {@code true} if a job count of zero was reached within the timeout
     * @throws ETComException in case of a COM exception
     */
    boolean waitForIdle(int timeout) throws ETComException;

    /**
     * Update all user libraries. Only possible if neither a test nor the analysis is running.
     *
     * @return {@code true} if success, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean updateUserLibraries() throws ETComException;

}
