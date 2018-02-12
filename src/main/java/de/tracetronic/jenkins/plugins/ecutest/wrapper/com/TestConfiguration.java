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

import org.apache.commons.lang.StringUtils;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComConstants;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestConfiguration;

/**
 * COM object representing the currently loaded test configuration file and
 * provides methods for accessing the contained settings.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestConfiguration extends ETComDispatch implements ComTestConfiguration {

    /**
     * Instantiates a new {@link TestConfiguration}.
     * 
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch
     *            the dispatch
     * @param useTimeout
     *            specifies whether to apply timeout
     */
    public TestConfiguration(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public void setGlobalConstant(final String name, final String value) throws ETComException {
        Object objValue;
        if (StringUtils.isNotEmpty(value) && StringUtils.isNumeric(value)) {
            // Assume Python integer literal
            objValue = value;
        } else {
            // Convert to Python string literal
            objValue = String.format("'%s'", value);
        }
        performRequest("SetGlobalConstant", new Variant(name), new Variant(objValue));
    }

    @Override
    public ComConstants getGlobalConstants() throws ETComException {
        return new Constants(performRequest("GetGlobalConstants").toDispatch(), useTimeout());
    }

    @Override
    public String getFileName() throws ETComException {
        return performRequest("GetFileName").getString();
    }
}
