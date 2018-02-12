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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;

import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder.Seriousness;

/**
 * Common base class for {@link Package} and {@link Project} giving access to their properties.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractTestObject extends ETComDispatch {

    /**
     * Instantiates a new {@link AbstractTestObject}.
     * 
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch
     *            the dispatch
     * @param useTimeout
     *            specifies whether to apply timeout
     */
    public AbstractTestObject(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    /**
     * Queries the package name.
     *
     * @return the name of this package
     * @throws ETComException
     *             in case of a COM exception
     */
    public String getName() throws ETComException {
        return performRequest("GetName").getString();
    }

    /**
     * Returns a list of the errors of the project.
     *
     * @return the error list
     * @throws ETComException
     *             in case of a COM exception
     */
    public List<CheckInfoHolder> check() throws ETComException {
        final List<CheckInfoHolder> errorList = new ArrayList<CheckInfoHolder>();
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
}
