/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComConstant;

/**
 * COM object giving access to the properties of a constant.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class Constant extends AbstractTestObject implements ComConstant {

    /**
     * Instantiates a new {@link Constant}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public Constant(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public String getName() throws ETComException {
        return performRequest("GetName").getString();
    }

    @Override
    public String getDescription() throws ETComException {
        return performRequest("GetDescription").getString();
    }

    @Override
    public String getValue() throws ETComException {
        return performRequest("GetValue").getString();
    }
}
