/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder.Seriousness;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Common base class for {@link Package} and {@link Project} giving access to their properties.
 */
public abstract class AbstractTestObject extends ETComDispatch {

    /**
     * Instantiates a new {@link AbstractTestObject}.
     *
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public AbstractTestObject(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    /**
     * Queries the package name.
     *
     * @return the name of this package
     * @throws ETComException in case of a COM exception
     */
    public String getName() throws ETComException {
        return performRequest("GetName").getString();
    }

    /**
     * Returns a list of the errors of the project.
     *
     * @return the error list
     * @throws ETComException in case of a COM exception
     */
    public List<CheckInfoHolder> check() throws ETComException {
        final List<CheckInfoHolder> errorList = new ArrayList<>();
        final SafeArray array = performRequest("Check").toSafeArray();
        if (array.getNumDim() == 2) {
            final int lBound = array.getLBound(1);
            final int uBound = array.getUBound(1);
            if (array.getUBound(2) == 3) {
                for (int i = lBound; i <= uBound; i++) {
                    errorList.add(new CheckInfoHolder(array.getString(i, 0),
                        Seriousness.valueOf(array.getString(i, 1).toUpperCase(Locale.getDefault())),
                        array.getString(i, 2), array.getString(i, 3)));
                }
            }
        }
        return errorList;
    }

    /**
     * Returns converted error descriptions into specific WarningsNG plugin JSON format.
     *
     * @return the error descriptions as issues in JSON format
     * @throws ETComException in case of a COM exception
     */
    public String checkNG() throws ETComException {
        return performRequest("CheckNG").getString();
    }
}
