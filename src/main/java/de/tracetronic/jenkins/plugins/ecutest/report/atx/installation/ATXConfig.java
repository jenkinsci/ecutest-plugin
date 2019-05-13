/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting.SettingsGroup;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class holding the ATX configuration grouped by setting sections.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXConfig extends AbstractDescribableImpl<ATXConfig> implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXPublisher.class.getName());

    /**
     * Settings list used instead of map due to CasC compatibility.
     *
     * @since 2.7.0
     */
    private List<ATXSetting> settings;
    private List<ATXCustomSetting> customSettings;

    /**
     * Maps of complex types are not supported by CasC.
     *
     * @see #readResolve()
     * @since 2.7.0
     * @deprecated due to CasC compatibility
     */
    @Deprecated
    private transient Map<String, List<ATXSetting>> configMap;

    /**
     * Instantiates a new {@link ATXConfig} with the default configuration.
     */
    public ATXConfig() {
        settings = parseDefaultConfig();
        customSettings = new ArrayList<>();
    }

    /**
     * Instantiates a new {@link ATXConfig}.
     *
     * @param settings       the ATX settings map
     * @param customSettings the custom settings
     */
    @DataBoundConstructor
    public ATXConfig(final List<ATXSetting> settings,
                     final List<ATXCustomSetting> customSettings) {
        this.settings = settings == null ? parseDefaultConfig() : settings;
        this.customSettings = customSettings == null ? new ArrayList<>() : customSettings;
    }

    /**
     * Used for backward compatibility using deprecated configuration map.
     *
     * @since 2.7.0
     */
    protected Object readResolve() {
        if (configMap != null) {
            settings = new ArrayList<>();

            for (final Entry<String, List<ATXSetting>> config : configMap.entrySet()) {
                String groupName = config.getKey();
                ATXSetting.SettingsGroup settingsGroup = getSettingsGroup(groupName);

                for (final ATXSetting setting : config.getValue()) {
                    String settingName = setting.getName();
                    String descGerman = setting.getDescGerman();
                    String descEnglish = setting.getDescEnglish();
                    if (setting.isCheckbox()) {
                        ATXBooleanSetting newSetting = new ATXBooleanSetting(settingName, settingsGroup,
                            descGerman, descEnglish, (boolean) setting.getDefaultValue());
                        newSetting.setValue(((ATXBooleanSetting) setting).getValue());
                        settings.add(newSetting);
                    } else {
                        ATXTextSetting newSetting = new ATXTextSetting(settingName, settingsGroup,
                            descGerman, descEnglish, (String) setting.getDefaultValue());
                        newSetting.setValue(((ATXTextSetting) setting).getValue());
                        settings.add(newSetting);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public ATXConfig clone() {
        ATXConfig clone = null;
        try {
            // Clone immutable fields
            final ATXConfig configClone = (ATXConfig) super.clone();

            // Deep clone settings
            final List<ATXSetting> settings = new ArrayList<>();
            for (final ATXSetting setting : configClone.getSettings()) {
                settings.add(setting.clone());
            }

            // Deep clone custom settings
            final List<ATXCustomSetting> customSettings = new ArrayList<>();
            for (final ATXCustomSetting setting : configClone.getCustomSettings()) {
                customSettings.add(setting.clone());
            }
            clone = new ATXConfig(settings, customSettings);
        } catch (final CloneNotSupportedException e) {
            LOGGER.log(Level.SEVERE, "Could not clone ATXConfig!", e);
        }
        return clone;
    }

    /**
     * @return the settings
     */
    public List<ATXSetting> getSettings() {
        return settings;
    }

    /**
     * @param settings the custom settings to set
     */
    public void setSettings(final List<ATXSetting> settings) {
        this.settings = settings;
    }

    /**
     * @return the custom settings
     */
    public List<ATXCustomSetting> getCustomSettings() {
        return customSettings;
    }

    /**
     * @param customSettings the custom settings to set
     */
    public void setCustomSettings(final List<ATXCustomSetting> customSettings) {
        this.customSettings = customSettings;
    }

    /**
     * Parses the default ATX configuration from the ATX template configuration file provided with this plugin.
     *
     * @return the default ATX settings map
     */
    private List<ATXSetting> parseDefaultConfig() {
        Document doc = null;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try (InputStream configFile = ATXConfig.class.getResourceAsStream("config.xml")) {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(configFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error parsing ATX template configuration: " + e.getMessage(), e);
        }
        return ATXSettingParser.parseSettings(doc);
    }

    /**
     * Gets the ATX setting by given setting name.
     *
     * @param name the setting name
     * @return the ATX setting or {@code null} if not found
     */
    public Optional<ATXSetting> getSettingByName(final String name) {
        return settings.stream().filter(setting -> setting.getName().equals(name)).findFirst();
    }

    /**
     * Gets all ATX settings which belongs to a settings group.
     *
     * @param group the settings group
     * @return the list of ATX settings or empty list if not found
     */
    public List<ATXSetting> getSettingsByGroup(final SettingsGroup group) {
        return settings.stream().filter(setting -> setting.getGroup().equals(group)).collect(Collectors.toList());
    }

    /**
     * Gets the ATX settings by group name.
     *
     * @param groupName the group name
     * @return the settings by group name
     */
    public List<ATXSetting> getSettingsByGroupName(final String groupName) {
        SettingsGroup group = getSettingsGroup(groupName);
        return getSettingsByGroup(group);
    }

    /**
     * Gets the ATX setting value by given setting name from a list of ATX settings.
     *
     * @param name     the setting name
     * @param settings the setting list to search in
     * @return the setting value or {@code null} if not found
     */
    @CheckForNull
    public Object getSettingValueByName(final String name, final List<ATXSetting> settings) {
        return settings.stream().filter(setting ->
            setting.getName().equals(name)).findFirst().map(ATXSetting::getValue).orElse(null);
    }

    /**
     * Gets the ATX setting by given setting name and a list of ATX settings.
     *
     * @param name  the setting name
     * @param group the settings group
     * @return the ATX setting or {@code null} if not found
     */
    @CheckForNull
    public Object getSettingValueByGroup(final String name, final SettingsGroup group) {
        final List<ATXSetting> settings = getSettingsByGroup(group);
        return getSettingValueByName(name, settings);
    }

    /**
     * Gets the ATX settings group by name.
     *
     * @param groupName the group name
     * @return the settings group
     */
    public static SettingsGroup getSettingsGroup(final String groupName) {
        return SettingsGroup.fromString(groupName);
    }

    /**
     * DescriptorImpl of {@link ATXConfig}.
     */
    @Symbol("atx-config")
    @Extension
    public static class DescriptorImpl extends Descriptor<ATXConfig> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXConfig_DisplayName();
        }
    }
}
