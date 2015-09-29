/**
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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.JacobException;
import com.jacob.com.Variant;

/**
 * Custom dispatch to perform requests on application specific COM API.
 * <p>
 * All threads from COM will be automatically released after performing the requests.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETComDispatch extends Dispatch implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ETComDispatch.class.getName());

    /**
     * Instantiates a new {@link ETComDispatch} with default program id.
     */
    public ETComDispatch() {
        super(ETComClient.PROGRAM_ID);
    }

    /**
     * Instantiates a new {@link ETComDispatch}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch
     *            the dispatch
     */
    public ETComDispatch(final Dispatch dispatch) {
        super(dispatch);
    }

    /**
     * Performs a request on this {@link ETComDispatch}, invoking the given method.
     *
     * @param method
     *            the specific COM API method name
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performRequest(final String method) throws ETComException {
        try {
            return Dispatch.call(this, method);
        } catch (final JacobException e) {
            throw new ETComException(e.getMessage(), e);
        } catch (final Throwable t) {
            throw new ETComException(t);
        }
    }

    /**
     * Performs a request on this {@link ETComDispatch}, invoking the given method with parameters.
     *
     * @param method
     *            the parameterized COM API method name
     * @param params
     *            the parameters available for the method
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performRequest(final String method, final Object... params) throws ETComException {
        try {
            return Dispatch.call(this, method, params);
        } catch (final JacobException e) {
            throw new ETComException(e.getMessage(), e);
        } catch (final Throwable t) {
            throw new ETComException(t);
        }
    }

    /**
     * Releases this {@link ETComDispatch}.
     *
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    private void releaseDispatch() throws ETComException {
        try {
            safeRelease();
        } catch (final JacobException e) {
            throw new ETComException(e.getMessage(), e);
        }
    }

    /**
     * Closes this {@link ETComDispatch} quietly.
     */
    @Override
    public void close() {
        try {
            releaseDispatch();
        } catch (final ETComException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            ComThread.Release();
        }
    }
}
