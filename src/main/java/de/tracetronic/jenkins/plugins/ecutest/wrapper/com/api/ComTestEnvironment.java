/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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

import java.util.Map;

/**
 * Represents the ECU-TEST specific COMTestEnvironment API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
     *                            0 .. no job execution
     *                            1 .. sequential job execution (default)
     *                            2 .. parallel job execution
     *                            5 .. sequential job execution with separate test report
     *                            6 .. parallel job execution with separate test report
     *                            </pre>
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
     * @param reportFormat      name of the report format or handler which should be used
     * @param waitUntilFinished defines whether the API call should block until generation is finished
     * @param parameters        the configuration parameters
     * @return {@code true} if successful, {@code false} otherwise
     * @throws ETComException in case of a COM exception or invalid parameters
     */
    boolean generateTestReportDocumentFromDB(String dbFile, String reportDir, String reportFormat,
                                             boolean waitUntilFinished, Map<String, String> parameters)
        throws ETComException;

}
