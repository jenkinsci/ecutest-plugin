/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestExecutionInfo;

/**
 * COM object providing operations to obtain informations of the currently running test.
 */
public class TestExecutionInfo extends ETComDispatch implements ComTestExecutionInfo {

    /**
     * Instantiates a new {@link TestExecutionInfo}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public TestExecutionInfo(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public boolean abort() throws ETComException {
        return performRequest("Abort").getBoolean();
    }

    @Override
    public String getReportDb() throws ETComException {
        return performRequest("GetReportDb").getString();
    }

    @Override
    public String getLogFolder() throws ETComException {
        return performRequest("GetLogFolder").getString();
    }

    @Override
    public String getResult() throws ETComException {
        return performRequest("GetResult").getString();
    }

    @Override
    public String getState() throws ETComException {
        return performRequest("GetState").getString();
    }
}
