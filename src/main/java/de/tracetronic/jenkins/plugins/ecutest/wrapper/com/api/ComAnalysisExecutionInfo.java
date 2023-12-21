/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ecu.test specific COMAnalysisExecutionInfo API.
 */
public interface ComAnalysisExecutionInfo {

    /**
     * Aborts the current analysis execution.
     *
     * @return {@code true} if the abortion succeeded, {@code false},
     * if the analysis execution has already finished or aborted
     * @throws ETComException in case of a COM exception
     */
    boolean abort() throws ETComException;

    /**
     * Queries the path to report database of current (or most recent) analysis job execution.
     *
     * @return the path to report database
     * @throws ETComException in case of a COM exception
     */
    String getReportDb() throws ETComException;

    /**
     * Folder where trace and log files of the currently executed analysis job are stored.
     * Please note, each analysis job has got a separate log folder.
     *
     * @return the log folder
     * @throws ETComException in case of a COM exception
     */
    String getLogFolder() throws ETComException;

    /**
     * Returns the result of the analysis job execution.
     * If the execution has not finished yet, the result equates the result at calling time.
     *
     * @return the current overall result. One of:
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
     * Returns the state of the current analysis job execution.
     *
     * @return the state current analysis job execution. One of:
     * <ol>
     * <li>IDLE</li>
     * <li>RUNNING</li>
     * <li>ABORTED</li>
     * <li>FINISHED</li>
     * </ol>
     * @throws ETComException in case of a COM exception
     */
    String getState() throws ETComException;

}
