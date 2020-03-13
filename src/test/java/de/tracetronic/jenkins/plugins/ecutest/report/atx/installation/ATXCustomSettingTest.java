/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXCustomSetting}.
 */
public class ATXCustomSettingTest {

    @Test
    public void testBooleanSetting() {
        final ATXCustomBooleanSetting setting = new ATXCustomBooleanSetting("settingName", true);
        assertThat(setting.getName(), is("settingName"));
        assertTrue(setting.isChecked());
    }

    @Test
    public void testTextSetting() {
        final ATXCustomTextSetting setting = new ATXCustomTextSetting("settingName", "settingValue");
        assertThat(setting.getName(), is("settingName"));
        assertThat(setting.getValue(), is("settingValue"));
    }

    @Test
    public void testBooleanSettingClone() {
        final ATXCustomBooleanSetting setting = new ATXCustomBooleanSetting("settingName", true);
        final ATXCustomBooleanSetting clone = (ATXCustomBooleanSetting) setting.clone();
        assertThat(clone, not(sameInstance(setting)));
    }

    @Test
    public void testTextSettingClone() {
        final ATXCustomTextSetting setting = new ATXCustomTextSetting("settingName", "settingValue");
        final ATXCustomTextSetting clone = (ATXCustomTextSetting) setting.clone();
        assertThat(clone, not(sameInstance(setting)));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ATXCustomBooleanSetting.class).withRedefinedSuperclass().verify();
        EqualsVerifier.forClass(ATXCustomTextSetting.class).withRedefinedSuperclass().verify();
    }
}
