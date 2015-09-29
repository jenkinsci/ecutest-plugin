/**
 * Copyright (c) 2015 TraceTronic GmbH
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

import java.io.Serializable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.localizer.LocaleProvider;

/**
 * Class holding the information of a single ATX setting.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 * @param <T>
 *            the type of the setting
 */
public abstract class ATXSetting<T> implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXSetting.class.getName());

    private final String name;
    private final String descGerman;
    private final String descEnglish;
    private final T defaultValue;

    /**
     * The current value of the setting with a generic type.
     */
    protected T currentValue;

    /**
     * Instantiates a new {@link ATXSetting}.
     *
     * @param name
     *            the name
     * @param descGerman
     *            the German description
     * @param descEnglish
     *            the English description
     * @param defaultValue
     *            the default value
     */
    public ATXSetting(final String name, final String descGerman, final String descEnglish,
            final T defaultValue) {
        super();
        this.name = name;
        this.descGerman = descGerman;
        this.descEnglish = descEnglish;
        this.defaultValue = defaultValue;

        // Initially set to default
        this.currentValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ATXSetting<T> clone() {
        ATXSetting<T> clone = null;
        try {
            clone = (ATXSetting<T>) super.clone();
        } catch (final CloneNotSupportedException e) {
            LOGGER.log(Level.SEVERE, "Could not clone ATXSetting!", e);
        }
        return clone;
    }

    /**
     * Gets the name.
     *
     * @return the name of the setting
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the title name of the setting to display in the configuration dialog.
     *
     * @return the title name
     */
    public String getTitle() {
        return toSpaceCase(name);
    }

    /**
     * Gets the desc german.
     *
     * @return the German description
     */
    public String getDescGerman() {
        return descGerman;
    }

    /**
     * Gets the desc english.
     *
     * @return the English description
     */
    public String getDescEnglish() {
        return descEnglish;
    }

    /**
     * Gets the related description for the system locale.
     *
     * @return the system locale description, defaults to English
     */
    public String getDescription() {
        final String locale = LocaleProvider.getLocale().getLanguage();
        final String description;
        if (locale.equals(new Locale("de").getLanguage())) {
            description = getDescGerman();
        } else {
            description = getDescEnglish();
        }
        return description;
    }

    /**
     * Gets the default value.
     *
     * @return the default value
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public T getCurrentValue() {
        return currentValue;
    }

    /**
     * Sets the current value.
     *
     * @param currentValue
     *            the new current value
     */
    public void setCurrentValue(final T currentValue) {
        this.currentValue = currentValue;
    }

    /**
     * Gets the checkbox status from the current value.
     *
     * @return {@code true} if checkbox is checked, {@code false} otherwise
     */
    public boolean isCheckbox() {
        return this instanceof ATXBooleanSetting ? true : false;
    }

    /**
     * Converts a boolean value to Python string equivalent.
     *
     * @param value
     *            the value
     * @return the string value
     */
    public static String toString(final boolean value) {
        return value ? "True" : "False";
    }

    /**
     * Converts string from CamelCase to SpaceCase representation.
     *
     * @param camelCase
     *            the camel case string
     * @return the converted space case string
     */
    private static String toSpaceCase(final String camelCase) {
        final String separated = camelCase.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
        return Character.toString(separated.charAt(0)).toUpperCase() + separated.substring(1);
    }
}
