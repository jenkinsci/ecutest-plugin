/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link CacheBuilder}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class CacheBuilderTest {

    @Test
    public void testDefaultStep() {
        CacheConfig config = new CacheConfig(Caches.CacheType.A2L, "C:\\test.a2l", "", false);
        final CacheBuilder builder = new CacheBuilder(Collections.singletonList(config));
        assertBuilder(builder);
    }

    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
    @Test
    public void testNullStep() {
        final CacheBuilder builder = new CacheBuilder(null);
        assertBuilder(builder);
    }

    /**
     * Asserts the builder properties.
     *
     * @param builder the builder
     */
    private void assertBuilder(final CacheBuilder builder) {
        assertNotNull(builder);
        assertNotNull(builder.getCaches());
    }
}
