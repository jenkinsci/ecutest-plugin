/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestExecutionInfo;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * COM object representing the currently started test environment.
 * This environment supports operations to run packages and projects.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestEnvironment extends ETComDispatch implements ComTestEnvironment {

    /**
     * Instantiates a new {@link TestEnvironment}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public TestEnvironment(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public ComTestExecutionInfo getTestExecutionInfo() throws ETComException {
        return new TestExecutionInfo(performRequest("GetTestExecutionInfo").toDispatch(), useTimeout());
    }

    /**
     * Same as {@link #executePackage(String, boolean, boolean, Map)} but with default parameters.
     *
     * @param path the full path name of the package file to execute
     * @return the {@link ComTestExecutionInfo} dispatch
     * @throws ETComException if the package was not opened before or the format of the parameter values is wrong
     * @see #executePackage(String, boolean, boolean, Map)
     */
    public ComTestExecutionInfo executePackage(final String path) throws ETComException {
        return executePackage(path, true, true);
    }

    /**
     * Same as {@link #executePackage(String, boolean, boolean, Map)} but with default parameters.
     *
     * @param path             the full path name of the package file to execute
     * @param runTraceAnalysis the run trace analysis
     * @param runTest          the run test
     * @return the {@link ComTestExecutionInfo} dispatch
     * @throws ETComException if the package was not opened before or the format of the parameter values is wrong
     * @see #executePackage(String, boolean, boolean, Map)
     */
    public ComTestExecutionInfo executePackage(final String path, final boolean runTraceAnalysis,
                                               final boolean runTest) throws ETComException {
        return executePackage(path, runTraceAnalysis, runTest, Collections.emptyMap());
    }

    @Override
    public ComTestExecutionInfo executePackage(final String path, final boolean runTraceAnalysis,
                                               final boolean runTest, final Map<String, String> parameters)
        throws ETComException {
        final Object[][] params = getArrayFromMap(parameters, true);
        return new TestExecutionInfo(performRequest("ExecutePackage", new Variant(path),
            new Variant(runTraceAnalysis), new Variant(runTest), params).toDispatch(), useTimeout());
    }

    /**
     * Same as {@link #executeProject(String, boolean, int)} but with default parameters.
     *
     * @param path the full path name of the project file
     * @return the {@link ComTestExecutionInfo} dispatch
     * @throws ETComException if the project was not opened before
     */
    public ComTestExecutionInfo executeProject(final String path) throws ETComException {
        return executeProject(path, true, 1);
    }

    @Override
    public ComTestExecutionInfo executeProject(final String path, final boolean closeProgressDialog,
                                               final int jobExecutionMode) throws ETComException {
        return new TestExecutionInfo(performRequest("ExecuteProject", new Variant(path),
            new Variant(closeProgressDialog), new Variant(jobExecutionMode)).toDispatch(), useTimeout());
    }

    /**
     * Same as {@link #generateTestReportDocumentFromDB(String, String, String, boolean, Map)} but with default
     * parameters.
     *
     * @param dbFile       the full path name of the data base file
     * @param reportDir    the full path name of output directory
     * @param reportFormat name of the report format or handler which should be used
     * @return {@code true} if successful, {@code false} otherwise
     * @throws ETComException in case of a COM exception or invalid parameters
     * @see #generateTestReportDocumentFromDB(String, String, String, boolean, Map)
     */
    public boolean generateTestReportDocumentFromDB(final String dbFile, final String reportDir,
                                                    final String reportFormat) throws ETComException {
        return generateTestReportDocumentFromDB(dbFile, reportDir, reportFormat, false,
            Collections.emptyMap());
    }

    /**
     * Same as {@link #generateTestReportDocumentFromDB(String, String, String, boolean, Map)} but with default
     * parameters.
     *
     * @param dbFile            the full path name of the data base file
     * @param reportDir         the full path name of output directory
     * @param reportFormat      name of the report format or handler which should be used
     * @param waitUntilFinished defines whether the API call should block until generation is finished
     * @return {@code true} if successful, {@code false} otherwise
     * @throws ETComException in case of a COM exception or invalid parameters
     * @see #generateTestReportDocumentFromDB(String, String, String, boolean, Map)
     */
    public boolean generateTestReportDocumentFromDB(final String dbFile, final String reportDir,
                                                    final String reportFormat, final boolean waitUntilFinished)
        throws ETComException {
        return generateTestReportDocumentFromDB(dbFile, reportDir, reportFormat, waitUntilFinished,
            Collections.emptyMap());
    }

    @Override
    public boolean generateTestReportDocumentFromDB(final String dbFile, final String reportDir,
                                                    final String reportFormat, final boolean waitUntilFinished,
                                                    final Map<String, String> parameters) throws ETComException {
        final Object[][] settings = getArrayFromMap(parameters, false);
        return performDirectRequest("GenerateTestReportDocumentFromDB", new Variant(dbFile),
            new Variant(reportDir), new Variant(reportFormat), new Variant(waitUntilFinished), settings)
            .getBoolean();
    }

    @Override
    public boolean generateTestReportDocument(final String dbFile, final String reportDir, final String reportConfig,
                                              final boolean waitUntilFinished) throws ETComException {
        return performDirectRequest("GenerateTestReportDocument", new Variant(dbFile),
            new Variant(reportDir), new Variant(reportConfig), new Variant(waitUntilFinished)).getBoolean();
    }

    /**
     * Gets an 2-dimensional object array from a String-based map.
     *
     * @param map       the parameter map
     * @param castToInt specifies whether to convert numeric strings to integer
     * @return the converted object array
     */
    private Object[][] getArrayFromMap(final Map<String, String> map, final boolean castToInt) {
        final Object[][] params = new Object[map.size()][2];
        int index = 0;
        for (final Entry<String, String> param : map.entrySet()) {
            params[index][0] = param.getKey();
            final String value = param.getValue();
            if (castToInt && StringUtils.isNotEmpty(value) && StringUtils.isNumeric(value)) {
                params[index][1] = Integer.valueOf(value);
            } else {
                params[index][1] = value;
            }
            index++;
        }
        return params;
    }
}
