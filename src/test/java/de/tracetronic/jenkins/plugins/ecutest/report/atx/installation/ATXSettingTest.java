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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.junit.Test;
import org.jvnet.localizer.LocaleProvider;

import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ATXSetting}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@SuppressWarnings("rawtypes")
public class ATXSettingTest {

    @Test
    public void testName() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", false);
        assertThat(setting.getName(), is("settingName"));
    }

    @Test
    public void testTitle() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", false);
        assertThat(setting.getTitle(), is("Setting Name"));
    }

    @Test
    public void testGermanDescription() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", false);
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
        final ATXSetting setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", false);
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
        final ATXSetting setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", false);
        assertTrue(setting.isCheckbox());
    }

    @Test
    public void testTextboxType() {
        final ATXSetting setting = new ATXTextSetting("settingName", "descGerman", "descEnglish", "defaultValue");
        assertFalse(setting.isCheckbox());
    }

    @Test
    public void testCurrentValueForTextBox() {
        final ATXSetting<String> setting = new ATXTextSetting("settingName", "descGerman", "descEnglish",
                "defaultValue");
        setting.setCurrentValue("currentValue");
        assertThat(setting.getCurrentValue(), is("currentValue"));
    }

    @Test
    public void testCurrentValueForBoolean() {
        final ATXSetting<Boolean> setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", false);
        setting.setCurrentValue(true);
        assertTrue(setting.getCurrentValue());
    }

    @Test
    public void testDefaultValueForText() {
        final ATXSetting<String> setting = new ATXTextSetting("settingName", "descGerman", "descEnglish",
                "defaultValue");
        assertThat(setting.getDefaultValue(), is("defaultValue"));
    }

    @Test
    public void testDefaultValueForBoolean() {
        final ATXSetting<Boolean> setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", true);
        assertTrue(setting.getDefaultValue());
    }

    @Test
    public void testToPythonString() {
        assertThat(ATXSetting.toString(true), is("True"));
        assertThat(ATXSetting.toString(false), is("False"));
    }

    @Test
    public void testClone() {
        final ATXSetting setting = new ATXBooleanSetting("settingName", "descGerman", "descEnglish", true);
        final ATXSetting clone = setting.clone();
        assertThat(clone, not(sameInstance(setting)));
    }

    @Test
    public void testManipulatedClone() {
        final ATXSetting<String> setting = new ATXTextSetting("settingName", "descGerman", "descEnglish",
                "defaultValue");
        final ATXSetting<String> clone = setting.clone();
        clone.setCurrentValue("newCloneValue");
        assertThat(clone.getCurrentValue(), is(not(setting.getCurrentValue())));
    }
}
