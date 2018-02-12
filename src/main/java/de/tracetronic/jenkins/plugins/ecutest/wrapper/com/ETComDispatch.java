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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private static final Object[] NO_PARAMS = new Object[0];

    private final boolean useTimeout;

    /**
     * Instantiates a new {@link ETComDispatch} with default programmatic identifier.
     *
     * @param useTimeout
     *            specifies whether to apply timeout
     */
    public ETComDispatch(final boolean useTimeout) {
        super(ETComProperty.getInstance().getProgId());
        this.useTimeout = useTimeout;
    }

    /**
     * Instantiates a new {@link ETComDispatch}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch
     *            the dispatch
     * @param useTimeout
     *            specifies whether to apply timeout
     */
    public ETComDispatch(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch);
        this.useTimeout = useTimeout;
    }

    /**
     * @return {@code true} if positive timeout is set, {@code false} otherwise
     */
    public boolean useTimeout() {
        return useTimeout;
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
        return performRequest(method, ETComProperty.getInstance().getTimeout(), NO_PARAMS);
    }

    /**
     * Performs a request on this {@link ETComDispatch}, invoking the given method.
     * Respects the given timeout and aborts the dispatch call if timeout exceeded.
     *
     * @param method
     *            the specific COM API method name
     * @param timeout
     *            the timeout in seconds
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performRequest(final String method, final int timeout) throws ETComException {
        return performRequest(method, timeout, NO_PARAMS);
    }

    /**
     * Performs a request on this {@link ETComDispatch}, invoking the given method with parameters.
     *
     * @param method
     *            the parameterized COM API method name
     * @param params
     *            the parameters for the method
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performRequest(final String method, final Object... params) throws ETComException {
        return performRequest(method, ETComProperty.getInstance().getTimeout(), params);
    }

    /**
     * Performs a request on this {@link ETComDispatch}, invoking the given method with parameters.
     * Respects the given timeout and aborts the dispatch call if timeout exceeded.
     *
     * @param method
     *            the parameterized COM API method name
     * @param timeout
     *            the timeout in seconds
     * @param params
     *            the parameters for the method
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performRequest(final String method, final int timeout, final Object... params)
            throws ETComException {
        if (timeout == 0) {
            return performDirectRequest(method, params);
        }

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<Variant> future = executor.submit(new DispatchCallable(method, params));
        try {
            return future.get(Long.valueOf(timeout), TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            future.cancel(true);
            throw new ETComTimeoutException(String.format("Request timeout of %d seconds exceeded!", timeout), e);
        } catch (ExecutionException | InterruptedException e) {
            throw new ETComException(String.format("Error while performing request: %s", e.getMessage()), e);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Performs a direct synchronous request on this {@link ETComDispatch},
     * invoking the given method and waiting for the result.
     *
     * @param method
     *            the specific COM API method name
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performDirectRequest(final String method) throws ETComException {
        return callDispatch(method, NO_PARAMS);
    }

    /**
     * Performs a direct synchronous request on this {@link ETComDispatch},
     * invoking the given method with parameters and waiting for the result.
     *
     * @param method
     *            the parameterized COM API method name
     * @param params
     *            the parameters for the method
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    protected Variant performDirectRequest(final String method, final Object... params) throws ETComException {
        return callDispatch(method, params);
    }

    /**
     * Performs a request on this {@link ETComDispatch}, invoking the given method with parameters.
     *
     * @param method
     *            the parameterized COM API method name
     * @param params
     *            the parameters for the method
     * @return the {@link Variant} returned by underlying callN
     * @throws ETComException
     *             the underlying {@link JacobException}
     */
    private Variant callDispatch(final String method, final Object... params) throws ETComException {
        try {
            return Dispatch.call(this, method, params);
        } catch (final JacobException e) {
            throw new ETComException(e.getMessage(), e);
        } catch (final Throwable t) {
            throw new ETComException(t);
        }
    }

    @Override
    public boolean isAttached() {
        return super.isAttached();
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
            // noop
        } finally {
            if (!useTimeout) {
                ComThread.Release();
            }
        }
    }

    @SuppressWarnings("checkstyle:superfinalize")
    @Override
    protected void finalize() {
        if (useTimeout) {
            return;
            // noop to prevent JVM crash
        }
    }

    /**
     * {@link Callable} performing the requested method on this {@link ETComDispatch}.
     * The performing call will be canceled if the current thread gets interrupted by timeout.
     */
    private final class DispatchCallable implements Callable<Variant> {

        private final String method;
        private final Object[] params;

        /**
         * Instantiates a new {@link DispatchCallable}.
         *
         * @param method
         *            the specific COM API method name
         * @param params
         *            the parameters for the method
         */
        DispatchCallable(final String method, final Object[] params) {
            this.method = method;
            this.params = params;
        }

        @Override
        public Variant call() throws Exception {
            if (!Thread.interrupted()) {
                return callDispatch(method, params);
            }
            throw new ETComException("Dispatch call is interrupted by timeout thread!");
        }
    }
}
