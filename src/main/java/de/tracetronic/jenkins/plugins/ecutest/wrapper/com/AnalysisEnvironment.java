/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComAnalysisEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComAnalysisExecutionInfo;

import java.util.List;

/**
 * COM object representing the currently started analysis environment.
 * This environment supports operations to run analysis jobs.
 */
public class AnalysisEnvironment extends ETComDispatch implements ComAnalysisEnvironment {

    /**
     * Instantiates a new {@link AnalysisEnvironment}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
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
     * @param jobFile the full path name of the analysis job file
     * @return the {@link ComAnalysisExecutionInfo} dispatch
     * @throws ETComException in case of a COM exception
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
     * @param list the list
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
