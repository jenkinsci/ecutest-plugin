/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches;
import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CacheConfigTest {

    @Test
    public void testNullConstructor() {
        final CacheConfig config = new CacheConfig(null, null, null, false);
        assertNull(config.getType());
        assertThat(config.getFilePath(), is(""));
        assertThat(config.getDbChannel(), is(""));
        assertFalse(config.isClear());
    }

    @Test
    public void testExpand() {
        final CacheConfig config = new CacheConfig(Caches.CacheType.A2L, "${FILE_PATH}", "${DB_CHANNEL}", true);
        final EnvVars envVars = new EnvVars();
        envVars.put("FILE_PATH", "C:\\test.a2l");
        envVars.put("DB_CHANNEL", "test");
        final CacheConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getType(), is(Caches.CacheType.A2L));
        assertThat(expConfig.getFilePath(), is("C:\\test.a2l"));
        assertThat(expConfig.getDbChannel(), is("test"));
        assertTrue(config.isClear());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(CacheConfig.class).verify();
    }
}
