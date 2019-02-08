/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches.CacheType;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComCache;

import java.util.Arrays;
import java.util.List;

/**
 * COM object giving access to a specific file cache.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class Cache extends ETComDispatch implements ComCache {

    private final CacheType type;

    /**
     * Instantiates a new {@link Cache}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     * @param type       the cache type
     */
    public Cache(final Dispatch dispatch, final boolean useTimeout, final CacheType type) {
        super(dispatch, useTimeout);
        this.type = type;
    }

    public CacheType getType() {
        return type;
    }

    @Override
    public void insert(final String filePath, final String dbChannel) throws ETComException {
        performRequest("Insert", new Variant(filePath), new Variant(dbChannel));
    }

    @Override
    public void clear(final boolean force) throws ETComException {
        performRequest("Insert", new Variant(force));
    }

    @Override
    public List<String> getFiles() throws ETComException {
        final SafeArray array = performRequest("GetFiles").toSafeArray();
        return Arrays.asList(array.toStringArray());
    }
}
