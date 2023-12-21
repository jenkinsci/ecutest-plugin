/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestBenchConfiguration;

/**
 * COM object representing the currently loaded test bench configuration file and
 * provides methods for accessing the contained settings.
 */
public class TestBenchConfiguration extends ETComDispatch implements ComTestBenchConfiguration {

    /**
     * Instantiates a new {@link TestBenchConfiguration}.
     *
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public TestBenchConfiguration(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public String getFileName() throws ETComException {
        return performRequest("GetFileName").getString();
    }
}
