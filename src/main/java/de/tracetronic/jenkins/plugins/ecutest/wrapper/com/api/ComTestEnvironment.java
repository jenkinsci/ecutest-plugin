/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

import java.util.Map;

/**
 * Represents the ECU-TEST specific COMTestEnvironment API.
 */
public interface ComTestEnvironment {

    /**
     * Gets the current test execution info.
     *
     * @return the {@link ComTestExecutionInfo} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComTestExecutionInfo getTestExecutionInfo() throws ETComException;

    /**
     * Starts the execution of the specified package file. The package must be already opened with
     * {@link ComApplication#openPackage}. If it is not opened a call of this function will raise an error.
     *
     * @param path             the full path name of the package file to execute
     * @param runTraceAnalysis specifies whether the trace analysis should be executed
     * @param runTest          specifies whether the test should be executed
     * @param parameters       the package parameters to populate as test variables for the test and the trace analysis
     * @return the {@link ComTestExecutionInfo} dispatch
     * @throws ETComException if the package was not opened before or the format of the parameter values is wrong
     */
    ComTestExecutionInfo executePackage(String path, boolean runTraceAnalysis, boolean runTest,
                                        Map<String, String> parameters) throws ETComException;

    /**
     * Starts the execution of the specified project file. The project must be already opened with
     * {@link ComApplication#openProject}. If it is not opened a call of this function will raise an error.
     *
     * @param path                the full path name of the project file
     * @param closeProgressDialog determines whether the progress dialog will be closed when finished
     * @param jobExecutionMode    specifies whether and how the analysis jobs should be executed:
     *                            <pre>
     *                                                       0 .. no job execution
     *                                                       1 .. sequential job execution (default)
     *                                                       2 .. parallel job execution
     *                                                       5 .. sequential job execution with separate test report
     *                                                       6 .. parallel job execution with separate test report
     *                                                       </pre>
     * @return the {@link ComTestExecutionInfo} dispatch
     * @throws ETComException if the project was not opened before
     */
    ComTestExecutionInfo executeProject(String path, boolean closeProgressDialog, int jobExecutionMode)
        throws ETComException;

    /**
     * Generates a handler based test report on the file system.
     *
     * @param dbFile            the full path name of the data base file
     * @param reportDir         the full path name of output directory
     * @param reportFormat      the name of the report format or handler which should be used
     * @param waitUntilFinished defines whether the API call should block until generation is finished
     * @param parameters        the configuration parameters
     * @return {@code true} if successful, {@code false} otherwise
     * @throws ETComException in case of a COM exception or invalid parameters
     */
    boolean generateTestReportDocumentFromDB(String dbFile, String reportDir, String reportFormat,
                                             boolean waitUntilFinished, Map<String, String> parameters)
        throws ETComException;

    /**
     * Generates a handler based test report on the file system.
     *
     * @param dbFile            the full path name of the data base file
     * @param reportDir         the full path name of output directory
     * @param reportConfig      the full path to persisted report generator settings file (XML)
     * @param waitUntilFinished defines whether the API call should block until generation is finished
     * @return {@code true} if successful, {@code false} otherwise
     * @throws ETComException in case of a COM exception or invalid parameters
     */
    boolean generateTestReportDocument(String dbFile, String reportDir, String reportConfig, boolean waitUntilFinished)
        throws ETComException;
}
