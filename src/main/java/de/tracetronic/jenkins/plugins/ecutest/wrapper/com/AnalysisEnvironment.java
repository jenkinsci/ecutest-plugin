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
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import java.util.List;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComAnalysisEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComAnalysisExecutionInfo;

/**
 * COM object representing the currently started analysis environment.
 * This environment supports operations to run analysis jobs.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class AnalysisEnvironment extends ETComDispatch implements ComAnalysisEnvironment {

    /**
     * Instantiates a new {@link AnalysisEnvironment}.
     *
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch
     *            the dispatch
     * @param useTimeout
     *            specifies whether to apply timeout
     */
    public AnalysisEnvironment(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public ComAnalysisExecutionInfo getAnalysisExecutionInfo() throws ETComException {
        return new AnalysisExecutionInfo(performRequest("GetAnalysisExecutionInfo").toDispatch(), useTimeout());
    }

    /**
     * Same as {@link #executeJob(String, boolean)} but with default parameters.
     *
     * @param jobFile
     *            the full path name of the analysis job file
     * @return the {@link ComAnalysisExecutionInfo} dispatch
     * @throws ETComException
     *             in case of a COM exception
     */
    public ComAnalysisExecutionInfo executeJob(final String jobFile) throws ETComException {
        return executeJob(jobFile, true);
    }

    @Override
    public ComAnalysisExecutionInfo executeJob(final String jobFile, final boolean createReportDir)
            throws ETComException {
        return new AnalysisExecutionInfo(performRequest("ExecuteJob", new Variant(jobFile),
                new Variant(createReportDir)).toDispatch(), useTimeout());
    }

    @Override
    public boolean mergeJobReports(final String mainReportFilename, final List<String> jobReports)
            throws ETComException {
        final Object[] jobList = getArrayFromList(jobReports);
        return performRequest("MergeJobReports", new Variant(mainReportFilename), jobList).getBoolean();
    }

    /**
     * Gets an 1-dimensional object array from a String-based list.
     *
     * @param list
     *            the list
     * @return the converted object array
     */
    private Object[] getArrayFromList(final List<String> list) {
        final Object[] params = new Object[list.size()];
        int index = 0;
        for (final String param : list) {
            params[index++] = param;
        }
        return params;
    }
}
