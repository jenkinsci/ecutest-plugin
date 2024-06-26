/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting.SettingsGroup;
import hudson.util.Secret;
import org.junit.Test;
import org.jvnet.localizer.LocaleProvider;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXSetting}.
 */
public class ATXSettingTest {

    @Test
    public void testName() {
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        assertThat(setting.getName(), is("settingName"));
    }

    @Test
    public void testTitle() {
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        assertThat(setting.getTitle(), is("Setting Name"));
    }

    @Test
    public void testGermanDescription() {
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
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
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
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
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        assertTrue(setting.isCheckbox());
    }

    @Test
    public void testTextboxType() {
        final ATXTextSetting setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        assertFalse(setting.isCheckbox());
    }

    @Test
    public void testCurrentValueForTextBox() {
        final ATXTextSetting setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        setting.setValue("value");
        assertThat(setting.getValue(), is("value"));
    }

    @Test
    public void testCurrentValueForBoolean() {
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", false);
        setting.setValue(true);
        assertTrue(setting.getValue());
    }

    @Test
    public void testDefaultValueForText() {
        final ATXTextSetting setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        assertThat(setting.getDefaultValue(), is("defaultValue"));
    }

    @Test
    public void testDefaultValueForBoolean() {
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
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
        final ATXBooleanSetting setting = new ATXBooleanSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", true);
        final ATXSetting<Boolean> clone = setting.clone();
        assertThat(clone, not(sameInstance(setting)));
    }

    @Test
    public void testManipulatedClone() {
        final ATXTextSetting setting = new ATXTextSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", "defaultValue");
        final ATXSetting<String> clone = setting.clone();
        clone.setValue("newCloneValue");
        assertThat(clone.getValue(), is(not(setting.getValue())));
    }

    @Test
    public void testSecretType() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", Secret.fromString("defaultValue"));
        assertTrue(setting.isSecret());
    }

    @Test
    public void testSecretFromString() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "s3cr3t");
        assertTrue(setting.isSecret());
        assertThat(setting.getSecretValue(), is("s3cr3t"));
    }

    @Test
    public void testSecretFromSecretSimple() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            Secret.fromString("s3cr3t"));
        assertTrue(setting.isSecret());
        assertThat(setting.getSecretValue(), is("s3cr3t"));
    }

    @Test
    public void testCurrentValueForSecret() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", Secret.fromString("defaultValue"));
        setting.setValue(Secret.fromString("test"));
        assertThat(setting.getSecretValue(), is("test"));
    }

    @Test
    public void testDefaultValueForSecret() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL,
            "descGerman", "descEnglish", Secret.fromString("defaultValue"));
        assertThat(setting.getDefaultValue().getPlainText(), is("defaultValue"));
    }

    @Test
    public void testSecretFromNull() {
        final ATXSecretSetting setting = new ATXSecretSetting("settingName", SettingsGroup.SPECIAL, (String) null);
        assertTrue(setting.isSecret());
        assertThat(setting.getSecretValue(), is(""));
    }
}
