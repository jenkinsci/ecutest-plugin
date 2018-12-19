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

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.CheckForNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding the ATX configuration grouped by setting sections.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@SuppressWarnings("rawtypes")
public class ATXConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ATXPublisher.class.getName());

    private final Map<String, List<ATXSetting>> configMap;
    private List<ATXCustomSetting> customSettings;

    /**
     * Instantiates a new {@link ATXConfig} with the default configuration.
     */
    public ATXConfig() {
        configMap = parseDefaultConfig();
        customSettings = new ArrayList<>();
    }

    /**
     * Instantiates a new {@link ATXConfig}.
     *
     * @param configMap      the configuration map
     * @param customSettings the custom settings
     */
    public ATXConfig(final Map<String, List<ATXSetting>> configMap,
                     final List<ATXCustomSetting> customSettings) {
        this.configMap = configMap == null ? new LinkedHashMap<>() : configMap;
        this.customSettings = customSettings == null ? new ArrayList<>() : customSettings;
    }

    @Override
    public ATXConfig clone() {
        ATXConfig clone = null;
        try {
            // Clone immutable fields
            final ATXConfig configClone = (ATXConfig) super.clone();

            // Deep clone objects in map
            final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<>();
            for (final Entry<String, List<ATXSetting>> config : configClone.getConfigMap().entrySet()) {
                final List<ATXSetting> settings = new ArrayList<>();
                for (final ATXSetting setting : config.getValue()) {
                    settings.add(setting.clone());
                }
                configMap.put(config.getKey(), settings);
            }

            // Deep clone custom settings
            final List<ATXCustomSetting> customSettings = new ArrayList<>();
            for (final ATXCustomSetting setting : configClone.getCustomSettings()) {
                customSettings.add(setting.clone());
            }
            clone = new ATXConfig(configMap, customSettings);
        } catch (final CloneNotSupportedException e) {
            LOGGER.log(Level.SEVERE, "Could not clone ATXConfig!", e);
        }
        return clone;
    }

    /**
     * Parses the default ATX configuration from the ATX template configuration file provided with this plugin.
     *
     * @return the default ATX settings map
     */
    private Map<String, List<ATXSetting>> parseDefaultConfig() {
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
     * Gets the configuration map containing a the grouped ATX settings.
     *
     * @return the configuration map
     */
    public Map<String, List<ATXSetting>> getConfigMap() {
        return configMap;
    }

    /**
     * Gets the ATX setting group by given configuration name.
     *
     * @param configName the configuration name
     * @return the ATX setting group list
     */
    public List<ATXSetting> getConfigByName(final String configName) {
        return configMap.get(configName);
    }

    /**
     * Gets the ATX setting by given setting name.
     *
     * @param settingName the setting name
     * @return the ATX setting or {@code null} if not found
     */
    @CheckForNull
    public ATXSetting getSettingByName(final String settingName) {
        ATXSetting settingByName = null;
        boolean found = false;
        for (final Entry<String, List<ATXSetting>> config : configMap.entrySet()) {
            for (final ATXSetting setting : config.getValue()) {
                if (setting.getName().equals(settingName)) {
                    settingByName = setting;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        return settingByName;
    }

    /**
     * Gets the ATX setting by given setting name and a list of ATX settings.
     *
     * @param settingName the setting name
     * @param settings    the setting list to search in
     * @return the ATX setting or {@code null} if not found
     */
    @CheckForNull
    public Object getSettingValueByName(final String settingName, final List<ATXSetting> settings) {
        ATXSetting settingByName = null;
        if (settings != null) {
            for (final ATXSetting setting : settings) {
                if (setting.getName().equals(settingName)) {
                    settingByName = setting;
                    break;
                }
            }
        }
        return settingByName == null ? null : settingByName.getCurrentValue();
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
}
