/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.model.AbstractDescribableImpl;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
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
*/
public abstract class ATXSetting<T> extends AbstractDescribableImpl<ATXSetting<?>> implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXSetting.class.getName());

    /**
     * The current value of the setting as generic type.
     */
    protected T value;
    private final String name;
    private final SettingsGroup group;

    /**
     * Deprecated property storing the current value.
     *
     * @see #readResolve()
     * @since 2.7.0
     * @deprecated due to CasC compatibility
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    private transient T currentValue;

    /**
     * Transient setting properties which will not be saved to disk.
     *
     * @since 2.7.0
     */
    private transient String descGerman;
    private transient String descEnglish;
    private transient T defaultValue;

    /**
     * Instantiates a new {@link ATXSetting}.
     * Settings group, descriptions and default value are synchronized during plugin startup.
     *
     * @param name  the name
     * @param group the settings group
     * @param value the current value
     */
    public ATXSetting(final String name, final SettingsGroup group, final T value) {
        super();
        this.name = name;
        this.group = group;
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
    public ATXSetting(final String name, final SettingsGroup group,
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

    /**
     * Used for backward compatibility using deprecated configuration map.
     *
     * @return the value to use after deserialization
     * @since 2.7.0
     */
    protected Object readResolve() {
        if (currentValue != null) {
            value = currentValue;
        }
        return this;
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

    @Whitelisted
    public String getName() {
        return name;
    }

    public String getTitle() {
        return toSpaceCase(name);
    }

    public SettingsGroup getGroup() {
        return group;
    }

    public String getDescGerman() {
        return descGerman;
    }

    public String getDescEnglish() {
        return descEnglish;
    }

    /**
     * The system locale description, defaults to English.
     *
     * @return the description
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

    public T getDefaultValue() {
        return defaultValue;
    }

    @Whitelisted
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
        SettingsGroup(final String configName) {
            this.configName = configName;
        }

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
        public static SettingsGroup fromString(final String configName) throws IllegalArgumentException {
            return Arrays.stream(SettingsGroup.values())
                .filter(v -> v.configName.equals(configName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown settings group for: " + configName));
        }
    }
}
