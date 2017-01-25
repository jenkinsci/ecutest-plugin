/*
 * Copyright (c) 2015 TraceTronic GmbH
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

import com.jacob.com.Dispatch;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestExecutionInfo;

/**
 * COM object providing operations to obtain informations of the currently running test.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestExecutionInfo extends ETComDispatch implements ComTestExecutionInfo {

    /**
     * Instantiates a new {@link TestExecutionInfo}.
     *
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch
     *            the dispatch
     */
    public TestExecutionInfo(final Dispatch dispatch) {
        super(dispatch);
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
