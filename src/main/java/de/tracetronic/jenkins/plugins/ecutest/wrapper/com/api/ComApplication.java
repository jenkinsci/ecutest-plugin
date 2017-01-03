/**
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
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMApplication API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComApplication {

    /**
     * Starts up the currently loaded test configuration and testbench configuration files.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComTestEnvironment start() throws ETComException;

    /**
     * Stops the currently loaded test configuration and testbench configuration files.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComTestEnvironment stop() throws ETComException;

    /**
     * Returns the test environment.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComTestEnvironment getTestEnvironment() throws ETComException;

    /**
     * Returns the test management module.
     *
     * @return the {@link ComTestEnvironment} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComTestManagement getTestManagement() throws ETComException;

    /**
     * Checks if the application process is running and ready to use.
     *
     * @return {@code true} if application is already running, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean isApplicationRunning() throws ETComException;

    /**
     * Queries the COM-Application version.
     *
     * @return the version string
     * @throws ETComException
     *             in case of a COM exception
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
     * @param settingName
     *            the setting name
     * @return the setting value or {@code null} if not defined
     * @throws ETComException
     *             in case of a COM exception
     */
    String getSetting(String settingName) throws ETComException;

    /**
     * Exits the currently running instance of the application (Soft Exit).
     *
     * @return {@code true} if successful
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean quit() throws ETComException;

    /**
     * Exits the currently running instance of the application (Hard Exit). Prefer the method {@link #quit()} instead.
     *
     * @return {@code true} if successful
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean exit() throws ETComException;

    /**
     * Opens an existing package in COM-Application.
     *
     * @param path
     *            the full path name of the package to open
     * @return the {@link ComPackage} dispatch, if the package is successfully opened, {@code null} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    ComPackage openPackage(String path) throws ETComException;

    /**
     * Closes a package.
     *
     * @param path
     *            the full path name of the package to close
     * @return {@code true} if the package was closed, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean closePackage(String path) throws ETComException;

    /**
     * Opens an existing project in COM-Application.
     *
     * @param path
     *            the full path name of the project to open
     * @param execInCurrentPkgDir
     *            defines whether relative references in the project are resolved starting from the current workspaces
     *            package directory or from the project file location
     * @param filterExpression
     *            a valid filter expression (see the main help document, section 'Projects')
     * @return the {@link ComProject} dispatch, if the project is successfully opened, {@code null} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    ComProject openProject(String path, boolean execInCurrentPkgDir, String filterExpression)
            throws ETComException;

    /**
     * Closes a project.
     *
     * @param path
     *            the full path name of the project to close
     * @return {@code true} if the project was closed, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean closeProject(String path) throws ETComException;

    /**
     * Imports a project from an archive.
     *
     * @param path
     *            the full path name of the project to import
     * @param importPath
     *            the full path name or a relative directory to the default package directory
     *            as the projects/packages destination directory
     * @param importConfigPath
     *            the full path name or a relative directory to the default configuration directory
     *            as the configurations destination directory
     * @param replaceFiles
     *            specifies whether files of same name should be replaced or left untouched
     * @return the {@link ComPackage} dispatch, if the project was successfully imported, {@code null} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean importProject(String path, String importPath, String importConfigPath, boolean replaceFiles)
            throws ETComException;

    /**
     * Opens a test bench configuration file (*.tbc).
     *
     * @param path
     *            the full path name of the test bench configuration file to open
     * @return {@code true} if the configuration was successfully opened, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean openTestbenchConfiguration(String path) throws ETComException;

    /**
     * Opens a test configuration file (*.tcf).
     *
     * @param path
     *            the full path name of the test configuration file to open
     * @return {@code true} if the configuration was successfully opened, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean openTestConfiguration(String path) throws ETComException;

    /**
     * Provides access to settings of the currently active test configuration file.
     *
     * @return the {@link ComTestConfiguration} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComTestConfiguration getCurrentTestConfiguration() throws ETComException;

    /**
     * Waits until the job count in the task manager reaches zero. The timeout parameter specifies the maximum waiting
     * time in seconds.
     *
     * @param timeout
     *            the timeout in seconds
     * @return {@code true} if if a job count of zero was reached within the timeout
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean waitForIdle(int timeout) throws ETComException;

}
