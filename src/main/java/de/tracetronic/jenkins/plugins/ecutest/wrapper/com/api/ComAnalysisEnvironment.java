/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

import java.util.List;

/**
 * Represents the ecu.test specific COMAnalysisEnvironment API.
 */
public interface ComAnalysisEnvironment {

    /**
     * Gets the current analysis execution info.
     *
     * @return the {@link ComAnalysisExecutionInfo} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComAnalysisExecutionInfo getAnalysisExecutionInfo() throws ETComException;

    /**
     * Starts the execution of an analysis job.
     *
     * @param jobFile         the full path name of the analysis job file
     * @param createReportDir specifies whether a new report directory is created
     *                        or whether the report should be stored next to the job
     * @return the {@link ComAnalysisExecutionInfo} dispatch
     * @throws ETComException in case of a COM exception
     */
    ComAnalysisExecutionInfo executeJob(String jobFile, boolean createReportDir) throws ETComException;

    /**
     * Merges reports of analysis job executions into a main report.
     *
     * @param mainReportFilename the full path of the main report
     * @param jobReports         the list of file names to reports of analysis job executions
     * @return {@code true} if merge was successful, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean mergeJobReports(String mainReportFilename, List<String> jobReports) throws ETComException;

}
