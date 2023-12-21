/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;

/**
 * Represents the ecu.test specific COMTestExecutionInfo API.
 */
public interface ComTestExecutionInfo {

    /**
     * Aborts the current test execution.
     *
     * @return {@code true} if the abortion succeeded, {@code false}, if the test execution has already finished or
     * aborted
     * @throws ETComException in case of a COM exception
     */
    boolean abort() throws ETComException;

    /**
     * Aborts the current project test execution but waits for report generation.
     *
     * @param timeout the timeout in seconds to wait for aborting the current step
     * @return {@code true} if the abortion succeeded, {@code false}, if the test execution has already finished or
     * aborted
     * @throws ETComException in case of a COM exception
     */
    boolean abortAfterCurrentProjectStep(int timeout) throws ETComException;

    /**
     * Queries the path to report database of current (or most recent) test run.
     *
     * @return the path to report database
     * @throws ETComException in case of a COM exception
     */
    String getReportDb() throws ETComException;

    /**
     * Folder where trace and log files of the currently executed package are stored. If there is no test execution in
     * progress the log folder of the most recent package run is returned. Please note, each package run has got a
     * separate log folder.
     *
     * @return the log folder
     * @throws ETComException in case of a COM exception or when using this method on project executions
     */
    String getLogFolder() throws ETComException;

    /**
     * Returns the result of the project execution or package execution, depending on which method ( {@link
     * TestEnvironment#executeProject(String path)} or {@link TestEnvironment#executePackage(String path)}) has called
     * before. If the test execution has not finished yet, the result equates the test result at calling time.
     *
     * @return the current overall test result. One of:
     * <ol>
     * <li>NONE</li>
     * <li>SUCCESS</li>
     * <li>FAILED</li>
     * <li>ERROR</li>
     * </ol>
     * @throws ETComException in case of a COM exception
     */
    String getResult() throws ETComException;

    /**
     * Returns the state of the current test execution.
     *
     * @return the state current test execution. One of:
     * <ol>
     * <li>IDLE</li>
     * <li>RUNNING</li>
     * <li>ABORTED</li>
     * <li>FINISHED</li>
     * </ol>
     * @throws ETComException in case of a COM exception
     */
    String getState() throws ETComException;

    /**
     * Returns the final value of a package variable.
     *
     * @param varName the variable name
     * @return the final variable value
     * @throws ETComException in case of a COM exception
     */
    String getReturnValue(String varName) throws ETComException;
}
