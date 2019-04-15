/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.model.AbstractDescribableImpl;
import org.jvnet.localizer.LocaleProvider;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding the information of a single ATX setting.
 *
 * @param <T> the type of the setting
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ATXSetting<T> extends AbstractDescribableImpl<ATXSetting<?>> implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXSetting.class.getName());

    private final String name;
    /**
     * The current value of the setting as generic type.
     */
    protected T value;

    /**
     * Transient setting properties which will not be saved to disk.
     *
     * @since 2.7.0
     */
    private transient SettingsGroup group;
    private transient String descGerman;
    private transient String descEnglish;
    private transient T defaultValue;

    /**
     * Instantiates a new {@link ATXSetting}.
     * Settings group, descriptions and default value are synchronized during plugin startup.
     *
     * @param name  the name
     * @param value the current value
     */
    public ATXSetting(final String name, final T value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * Instantiates a new {@link ATXSetting} with associated group, descriptions and default value.
     *
     * @param name         the name
     * @param group        the settings group
     * @param descGerman   the German description
     * @param descEnglish  the English description
     * @param defaultValue the default value
     */
    public ATXSetting(final String name, SettingsGroup group,
                      final String descGerman, final String descEnglish,
                      final T defaultValue) {
        super();
        this.name = name;
        this.group = group;
        this.descGerman = descGerman;
        this.descEnglish = descEnglish;
        this.defaultValue = defaultValue;

        // Initially set to default
        this.value = defaultValue;
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
     * @return the name of the setting
     */
    public String getName() {
        return name;
    }

    /**
     * @return the title name
     */
    public String getTitle() {
        return toSpaceCase(name);
    }

    /**
     * @return the name of the settings group
     */
    public SettingsGroup getGroup() {
        return group;
    }

    /**
     * @return the German description
     */
    public String getDescGerman() {
        return descGerman;
    }

    /**
     * @return the English description
     */
    public String getDescEnglish() {
        return descEnglish;
    }

    /**
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
     * @return the default value
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the current value
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the current value.
     *
     * @param value the new current value
     */
    public void setValue(final T value) {
        this.value = value;
    }

    /**
     * Gets the checkbox status from the current value.
     *
     * @return {@code true} if checkbox is checked, {@code false} otherwise
     */
    public boolean isCheckbox() {
        return this instanceof ATXBooleanSetting;
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

    /**
     * The enum Settings group.
     */
    public enum SettingsGroup {
        /**
         * Upload settings group.
         */
        UPLOAD("uploadConfig"),
        /**
         * Archive settings group.
         */
        ARCHIVE("archiveConfig"),
        /**
         * Attribute settings group.
         */
        ATTRIBUTE("attributeConfig"),
        /**
         * TBC constants settings group.
         */
        TBC_CONSTANTS("tbcConstantConfig"),
        /**
         * TCF constants settings group.
         */
        TCF_CONSTANTS("tcfConstantConfig"),
        /**
         * Special settings group.
         */
        SPECIAL("specialConfig");

        private final String configName;

        /**
         * Instantiates a new {@link SettingsGroup}.
         *
         * @param configName the config name
         */
        SettingsGroup(String configName) {
            this.configName = configName;
        }

        /**
         * @return the config name
         */
        public String getConfigName() {
            return configName;
        }

        /**
         * Get the settings group from config name.
         *
         * @param configName the config name
         * @return the settings group
         * @throws IllegalArgumentException in case of unknown settings group for given config name
         */
        public static SettingsGroup fromString(String configName) throws IllegalArgumentException {
            return Arrays.stream(SettingsGroup.values())
                .filter(v -> v.configName.equals(configName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown settings group for: " + configName));
        }
    }
}
