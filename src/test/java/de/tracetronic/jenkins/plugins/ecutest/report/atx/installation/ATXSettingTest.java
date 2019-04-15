/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting.SettingsGroup;
import org.junit.Test;
import org.jvnet.localizer.LocaleProvider;

import java.util.Locale;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXSetting}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXSettingTest {

    @Test
    public void testName() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        assertThat(setting.getName(), is("settingName"));
    }

    @Test
    public void testTitle() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        assertThat(setting.getTitle(), is("Setting Name"));
    }

    @Test
    public void testGermanDescription() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        final LocaleProvider restoreProvider = LocaleProvider.getProvider();
        try {
            // Test expects a German locale.
            LocaleProvider.setProvider(new LocaleProvider() {

                @Override
                public Locale get() {
                    return Locale.GERMAN;
                }
            });

            assertThat(setting.getDescription(), is("descGerman"));
        } finally {
            LocaleProvider.setProvider(restoreProvider);
        }
    }

    @Test
    public void testEnglishDescription() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        final LocaleProvider restoreProvider = LocaleProvider.getProvider();
        try {
            // Test expects an English locale.
            LocaleProvider.setProvider(new LocaleProvider() {

                @Override
                public Locale get() {
                    return Locale.ENGLISH;
                }
            });

            assertThat(setting.getDescription(), is("descEnglish"));
        } finally {
            LocaleProvider.setProvider(restoreProvider);
        }
    }

    @Test
    public void testCheckboxType() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        assertTrue(setting.isCheckbox());
    }

    @Test
    public void testTextboxType() {
        final ATXSetting setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        assertFalse(setting.isCheckbox());
    }

    @Test
    public void testCurrentValueForTextBox() {
        final ATXSetting<String> setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        setting.setValue("value");
        assertThat(setting.getValue(), is("value"));
    }

    @Test
    public void testCurrentValueForBoolean() {
        final ATXSetting<Boolean> setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        setting.setValue(true);
        assertTrue(setting.getValue());
    }

    @Test
    public void testDefaultValueForText() {
        final ATXSetting<String> setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        assertThat(setting.getDefaultValue(), is("defaultValue"));
    }

    @Test
    public void testDefaultValueForBoolean() {
        final ATXSetting<Boolean> setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", true);
        assertTrue(setting.getDefaultValue());
    }

    @Test
    public void testToPythonString() {
        assertThat(ATXSetting.toString(true), is("True"));
        assertThat(ATXSetting.toString(false), is("False"));
    }

    @Test
    public void testClone() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", true);
        final ATXSetting clone = setting.clone();
        assertThat(clone, not(sameInstance(setting)));
    }

    @Test
    public void testManipulatedClone() {
        final ATXSetting<String> setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        final ATXSetting<String> clone = setting.clone();
        clone.setValue("newCloneValue");
        assertThat(clone.getValue(), is(not(setting.getValue())));
    }
}
