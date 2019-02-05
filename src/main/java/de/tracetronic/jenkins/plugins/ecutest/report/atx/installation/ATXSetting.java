/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import org.jvnet.localizer.LocaleProvider;

import java.io.Serializable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding the information of a single ATX setting.
 *
 * @param <T> the type of the setting
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
     * @param name         the name
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
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

    /**
     * Converts a boolean value to Python string equivalent.
     *
     * @param value the value
     * @return the string value
     */
    public static String toString(final boolean value) {
        return value ? "True" : "False";
    }

    /**
     * Converts string from CamelCase to SpaceCase representation.
     *
     * @param camelCase the camel case string
     * @return the converted space case string
     */
    private static String toSpaceCase(final String camelCase) {
        final String separated = camelCase.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
        return Character.toString(separated.charAt(0)).toUpperCase(Locale.getDefault()) + separated.substring(1);
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
     * @param currentValue the new current value
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
        return this instanceof ATXBooleanSetting;
    }
}
