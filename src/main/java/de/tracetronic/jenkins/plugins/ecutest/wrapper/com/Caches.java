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
 */
public class Caches extends ETComDispatch implements ComCaches {

    /**
     * Available file caches.
     */
    public enum CacheType {
        /**
         * A2L cache type.
         */
        A2L,
        /**
         * ELF cache type.
         */
        ELF,
        /**
         * Bus cache type.
         */
        BUS,
        /**
         * Model cache type.
         */
        MODEL,
        /**
         * Service cache type.
         */
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

    /**
     * Gets the cache object by type.
     *
     * @param type the cache type
     * @return the cache by type
     * @throws ETComException in case of a COM exception or unsuppored cache type
     */
    public Cache getCacheByType(final CacheType type) throws ETComException {
        switch (type) {
            case A2L:
                return (Cache) getA2lCache();
            case ELF:
                return (Cache) getElfCache();
            case BUS:
                return (Cache) getBusCache();
            case MODEL:
                return (Cache) getModelCache();
            case SERVICE:
                return (Cache) getServiceCache();
            default:
                throw new ETComException("Unsupported cache type:" + type.name());
        }
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
