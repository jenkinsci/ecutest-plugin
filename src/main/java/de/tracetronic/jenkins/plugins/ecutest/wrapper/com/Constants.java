/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComConstant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComConstants;

/**
 * COM object giving access to all global constants of the currently loaded test configuration.
 */
public class Constants extends AbstractTestObject implements ComConstants {

    /**
     * Instantiates a new {@link Constants}.
     *
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public Constants(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public int getCount() throws ETComException {
        return performRequest("GetCount").getInt();
    }

    @Override
    public ComConstant item(final int id) throws ETComException {
        return new Constant(performRequest("Item", new Variant(id)).toDispatch(), useTimeout());
    }

    @Override
    public ComConstant item(final String name) throws ETComException {
        return new Constant(performRequest("Item", new Variant(name)).toDispatch(), useTimeout());
    }

}
