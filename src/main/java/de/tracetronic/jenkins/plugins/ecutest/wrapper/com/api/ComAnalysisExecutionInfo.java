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

/**
 * Represents the ECU-TEST specific COMAnalysisExecutionInfo API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
