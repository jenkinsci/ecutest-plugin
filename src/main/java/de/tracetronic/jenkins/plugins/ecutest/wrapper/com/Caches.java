/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComCache;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComCaches;

/**
 * COM object giving access to the properties of a constant.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class Caches extends ETComDispatch implements ComCaches {

    /**
     * Available file caches.
     */
    enum CacheType {
        A2L,
        ELF,
        BUS,
        MODEL,
        SERVICE
    }

    /**
     * Instantiates a new {@link Caches}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public Caches(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public ComCache getA2lCache() throws ETComException {
        return new Cache(performRequest("GetA2lCache").toDispatch(), useTimeout(), CacheType.A2L);
    }

    @Override
    public ComCache getElfCache() throws ETComException {
        return new Cache(performRequest("GetElfCache").toDispatch(), useTimeout(), CacheType.ELF);
    }

    @Override
    public ComCache getBusCache() throws ETComException {
        return new Cache(performRequest("GetBusCache").toDispatch(), useTimeout(), CacheType.BUS);
    }

    @Override
    public ComCache getModelCache() throws ETComException {
        return new Cache(performRequest("GetModelCache").toDispatch(), useTimeout(), CacheType.MODEL);
    }

    @Override
    public ComCache getServiceCache() throws ETComException {
        return new Cache(performRequest("GetServiceCache").toDispatch(), useTimeout(), CacheType.SERVICE);
    }
}
