/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;
import hudson.CopyOnWrite;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding all the ATX settings.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXInstallation extends AbstractDescribableImpl<ATXInstallation> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String toolName;
    private final ATXConfig config;

    /**
     * Instantiates a new {@link ATXInstallation}.
     *
     * @param name     the name
     * @param toolName the tool name
     * @param config   the configuration
     */
    @DataBoundConstructor
    public ATXInstallation(final String name, final String toolName, final ATXConfig config) {
        this.name = StringUtils.trimToEmpty(name);
        this.toolName = toolName;
        this.config = config == null ? new ATXConfig() : config;
    }

    /**
     * Gets all ATX installations.
     *
     * @return all available installations, never {@code null}
     */
    public static ATXInstallation[] all() {
        final Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance == null) {
            return new ATXInstallation[0];
        }
        final ATXInstallation.DescriptorImpl atxDescriptor = instance
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        return atxDescriptor.getInstallations();
    }

    /**
     * Gets the ATX installation by name.
     *
     * @param name the name
     * @return installation by name, {@code null} if not found
     */
    @CheckForNull
    public static ATXInstallation get(final String name) {
        final ATXInstallation[] installations = all();
        for (final ATXInstallation installation : installations) {
            if (StringUtils.equals(name, installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    /**
     * @return the name of the installation
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the tool name.
     *
     * @return the toolName
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the configuration
     */
    public ATXConfig getConfig() {
        return config;
    }

    /**
     * DescriptorImpl of {@link ATXInstallation}.
     */
    @Symbol("test-guide")
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends Descriptor<ATXInstallation> {

        private static final Logger LOGGER = Logger.getLogger(ATXInstallation.DescriptorImpl.class.getName());

        /**
         * Validator to check form fields.
         */
        private final transient ATXValidator atxValidator;
        private final transient ATXConfig defaultConfig;

        @CopyOnWrite
        private volatile ATXInstallation[] installations = new ATXInstallation[0];

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
            load();
            defaultConfig = new ATXConfig();
            atxValidator = new ATXValidator();
        }

        @Override
        public synchronized void load() {
            if (getConfigFile().exists()) {
                super.load();
            } else {
                migrateFromOldConfigFile(ATXPublisher.DescriptorImpl.class);
                save();
            }
        }

        /**
         * Moves the configured installations from old descriptor implementations to this descriptor
         * in order to retain backward compatibility. Old configuration files will be removed automatically.
         *
         * @param oldClass the old descriptor class name
         * @since 2.7
         */
        @SuppressWarnings("rawtypes")
        private void migrateFromOldConfigFile(final Class oldClass) {
            LOGGER.log(Level.FINE, "Migrating ATX installations from: " + oldClass.getName());

            final XStream2 stream = new XStream2();
            stream.addCompatibilityAlias(oldClass.getName(), getClass());

            final XmlFile file = new XmlFile(stream,
                new File(Jenkins.getInstance().getRootDir(), oldClass.getEnclosingClass().getName() + ".xml"));
            if (file.exists()) {
                try {
                    file.unmarshal(this);
                } catch (final IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to migrate ATX installations from: " + file, e);
                } finally {
                    file.delete();
                }
            }
        }

        /**
         * Gets the ATX version that this ATX configuration is based on.
         *
         * @return the related ATX version
         */
        public static String getATXVersion() {
            return ETPlugin.ATX_VERSION.toMicroString();
        }

        /**
         * @return the list of ATX installations
         */
        public ATXInstallation[] getInstallations() {
            return installations.clone();
        }

        /**
         * Sets the installations.
         *
         * @param installations the new installations
         */
        public void setInstallations(final ATXInstallation... installations) {
            // Remove empty installations
            final List<ATXInstallation> inst = new ArrayList<>();
            if (installations != null) {
                Collections.addAll(inst, installations);
                for (final ATXInstallation installation : installations) {
                    if (StringUtils.isBlank(installation.getName())) {
                        inst.remove(installation);
                    }
                }
            }
            this.installations = inst.toArray(new ATXInstallation[0]);
            save();
        }

        /**
         * @return the default ATX configuration
         */
        public ATXConfig getDefaultConfig() {
            return defaultConfig;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) {
            final List<ATXInstallation> list = new ArrayList<>();
            final JSONArray instArray = new JSONArray();
            final JSONArray inst = json.optJSONArray("installation");
            if (inst == null) {
                instArray.add(json.getJSONObject("installation"));
            } else {
                instArray.addAll(inst);
            }

            // Parse installations
            for (final Object instObject : instArray) {
                if (instObject instanceof JSONObject) {
                    final JSONObject instJson = (JSONObject) instObject;
                    final String name = instJson.getString("name");
                    final String toolName = instJson.getString("toolName");
                    final Map<String, List<ATXSetting>> configMap = getDefaultConfig().getConfigMap();

                    // Update custom settings
                    List<ATXCustomSetting> customSettings = req.bindJSONToList(ATXCustomSetting.class,
                        instJson.get("customSettings"));

                    // Remove duplicates of default configuration
                    customSettings.removeIf(atxCustomSetting ->
                        getDefaultConfig().getSettingByName(atxCustomSetting.getName()) != null);

                    // Make unique list
                    customSettings = new ArrayList<>(new LinkedHashSet<>(customSettings));

                    // Update current values
                    final ATXConfig config = new ATXConfig(updateCurrentValues(instJson, configMap), customSettings);

                    // Fill installations
                    final ATXInstallation installation = new ATXInstallation(name, toolName, config);
                    list.add(installation);
                }
            }

            setInstallations(list.toArray(new ATXInstallation[0]));
            return true;
        }

        /**
         * Synchronizes current ATX configuration with default configuration
         * by overriding their current values and saving them as new ATX installation.
         * <p>
         * This method will be automatically called by {@link ETPlugin#syncATXConfiguration()} to
         * avoid circular dependencies while loading other plugins.
         */
        @SuppressWarnings("unchecked")
        public void syncWithDefaultConfig() {
            final List<ATXInstallation> list = new ArrayList<>();
            for (final ATXInstallation installation : installations.clone()) {
                final ATXConfig currentConfig = installation.getConfig();
                final ATXConfig newConfig = defaultConfig.clone();

                // Synchronize settings
                if (currentConfig != null) {
                    for (final Map.Entry<String, List<ATXSetting>> newConfigMap : newConfig.getConfigMap().entrySet()) {
                        for (final ATXSetting newSetting : newConfigMap.getValue()) {
                            final ATXSetting currentSetting = currentConfig.getSettingByName(newSetting.getName());
                            if (currentSetting != null) {
                                newSetting.setCurrentValue(currentSetting.getCurrentValue());
                            }
                        }
                    }
                    final List<ATXCustomSetting> customSettings = currentConfig.getCustomSettings();
                    newConfig.setCustomSettings(customSettings == null ? new ArrayList<>() : customSettings);
                }

                // Fill installations
                final ATXInstallation inst = new ATXInstallation(installation.getName(),
                    installation.getToolName(), newConfig);
                list.add(inst);
            }
            setInstallations(list.toArray(new ATXInstallation[0]));
            load(); // Reload from disk
        }

        /**
         * Updates the current values for each ATX setting.
         *
         * @param instJson  the JSONObject representing one installation
         * @param configMap the default ATX configuration
         * @return the updated ATX configuration
         */
        @SuppressWarnings("unchecked")
        private Map<String, List<ATXSetting>> updateCurrentValues(final JSONObject instJson,
                                                                  final Map<String, List<ATXSetting>> configMap) {
            final Map<String, List<ATXSetting>> newConfigMap = new LinkedHashMap<>();
            for (final Map.Entry<String, List<ATXSetting>> entry : configMap.entrySet()) {
                final List<ATXSetting> newSettings = new ArrayList<>();
                final List<ATXSetting> defaultSettings = entry.getValue();

                // Deep copy setting list
                for (final ATXSetting defaultSetting : defaultSettings) {
                    newSettings.add(defaultSetting.clone());
                }

                // Update each setting
                final JSONObject configObject = instJson.optJSONObject(entry.getKey());
                if (configObject != null) {
                    for (final ATXSetting newSetting : newSettings) {
                        final Object configSetting = configObject.opt(newSetting.getName());
                        if (configSetting != null) {
                            newSetting.setCurrentValue(configSetting);
                        }
                    }
                }

                // Fill configuration
                newConfigMap.put(entry.getKey(), newSettings);
            }
            return newConfigMap;
        }

        /**
         * Gets the custom settings of a given ATX installation.
         *
         * @param installation the installation
         * @return the custom settings list
         */
        public List<ATXCustomSetting> getCustomSettings(final ATXInstallation installation) {
            return installation == null ?
                new ArrayList<>() : installation.getConfig().getCustomSettings();
        }

        /**
         * Gets the applicable custom settings.
         *
         * @return the applicable custom settings
         */
        public List<Descriptor<? extends ATXCustomSetting>> getApplicableCustomSettings() {
            final List<Descriptor<? extends ATXCustomSetting>> list = new ArrayList<>();
            final DescriptorExtensionList<ATXCustomSetting, Descriptor<ATXCustomSetting>> settings = ATXCustomSetting
                .all();
            if (settings != null) {
                list.addAll(settings);
            }
            return list;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXInstallation_DisplayName();
        }

        /**
         * Validates the TEST-GUIDE name which is a required field.
         *
         * @param value the name
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return atxValidator.validateName(value);
        }

        /**
         * Validates the current setting field.
         *
         * @param name  the field name
         * @param value the field value
         * @return the form validation
         */
        public FormValidation doCheckSetting(@QueryParameter final String name,
                                             @QueryParameter final String value) {
            return atxValidator.validateSetting(name, value);
        }

        /**
         * Tests the server connection.
         *
         * @param serverURL          the server URL
         * @param serverPort         the server port
         * @param serverContextPath  the server context path
         * @param useHttpsConnection if secure connection is used
         * @param ignoreSSL          specifies whether to ignore SSL issues
         * @return the form validation
         */
        @RequirePOST
        public FormValidation doTestConnection(@QueryParameter final String serverURL,
                                               @QueryParameter final String serverPort,
                                               @QueryParameter final String serverContextPath,
                                               @QueryParameter final boolean useHttpsConnection,
                                               @QueryParameter final boolean ignoreSSL) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            return atxValidator.testConnection(serverURL, serverPort, serverContextPath, useHttpsConnection, ignoreSSL);
        }
    }
}
