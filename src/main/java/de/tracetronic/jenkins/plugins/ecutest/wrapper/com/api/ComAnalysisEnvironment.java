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

import java.util.List;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMAnalysisEnvironment API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComAnalysisEnvironment {

    /**
     * Gets the current analysis execution info.
     *
     * @return the {@link ComAnalysisExecutionInfo} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComAnalysisExecutionInfo getAnalysisExecutionInfo() throws ETComException;

    /**
     * Starts the execution of an analysis job.
     *
     * @param jobFile
     *            the full path name of the analysis job file
     * @param createReportDir
     *            specifies whether a new report directory is created
     *            or whether the report should be stored next to the job
     * @return the {@link ComAnalysisExecutionInfo} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    ComAnalysisExecutionInfo executeJob(String jobFile, boolean createReportDir) throws ETComException;

    /**
     * Merges reports of analysis job executions into a main report.
     *
     * @param mainReportFilename
     *            the full path of the main report
     * @param jobReports
     *            the list of file names to reports of analysis job executions
     * @return {@code true} if merge was successful, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean mergeJobReports(String mainReportFilename, List<String> jobReports) throws ETComException;

}
