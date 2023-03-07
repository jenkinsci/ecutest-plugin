/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding all the ATX settings.
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
        final ATXInstallation.DescriptorImpl atxDescriptor =
                instance.getDescriptorByType(ATXInstallation.DescriptorImpl.class);
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

    @Whitelisted
    public String getName() {
        return name;
    }

    /**
     * Gets the tool name.
     *
     * @return the toolName
     */
    @Whitelisted
    public String getToolName() {
        return toolName;
    }

    public ATXConfig getConfig() {
        return config;
    }

    /**
     * Listen to {@link Saveable} actions of this descriptor in order to update the default ATX setting values when
     * invoked by CasC configuration reloads.
     */
    @Extension
    public static class SaveableListenerImpl extends SaveableListener {

        @Override
        public final void onChange(final Saveable o, final XmlFile file) {
            if (o instanceof DescriptorImpl) {
                ((DescriptorImpl) o).syncWithDefaultConfig();
            }
            super.onChange(o, file);
        }
    }

    /**
     * DescriptorImpl of {@link ATXInstallation}.
     */
    @Symbol("testGuide")
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends Descriptor<ATXInstallation> {

        private static final Logger LOGGER = Logger.getLogger(ATXInstallation.DescriptorImpl.class.getName());

        /**
         * Validator to check form fields.
         */
        private final transient ATXValidator atxValidator = new ATXValidator();
        private final transient ATXConfig defaultConfig = new ATXConfig();

        @CopyOnWrite
        @SuppressFBWarnings("VO_VOLATILE_REFERENCE_TO_ARRAY")
        private volatile ATXInstallation[] installations = new ATXInstallation[0];

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
            justification = "Never used in a critical way. Do not change in working legacy code.")
        public DescriptorImpl() {
            super();
            load();
        }

        /**
         * Gets the ATX version that this ATX configuration is based on.
         *
         * @return the related ATX version
         */
        public static String getATXConfigVersion() {
            return ETPlugin.ATX_CONFIG_VERSION.toMicroString();
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
         * Moves the configured installations from old descriptor implementations to this descriptor in order to retain
         * backward compatibility. Old configuration files will be removed automatically.
         *
         * @param oldClass the old descriptor class name
         * @since 2.7
         */
        private void migrateFromOldConfigFile(final Class<ATXPublisher.DescriptorImpl> oldClass) {
            LOGGER.log(Level.FINE, "Migrating ATX installations from: " + oldClass.getName());

            final XStream2 stream = new XStream2();
            stream.addCompatibilityAlias(oldClass.getName(), getClass());

            final XmlFile file = new XmlFile(stream,
                    new File(Jenkins.get().getRootDir(), oldClass.getEnclosingClass().getName() + ".xml"));
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
        }

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
                    final ATXConfig defaultConfig = getDefaultConfig().clone();
                    final List<ATXSetting<?>> settings = defaultConfig.getSettings();

                    // Update custom settings
                    List<ATXCustomSetting> customSettings = req.bindJSONToList(ATXCustomSetting.class,
                            instJson.get("customSettings"));

                    // Remove duplicates of default configuration
                    customSettings.removeIf(atxCustomSetting ->
                            getDefaultConfig().getSettingByName(atxCustomSetting.getName()).isPresent());

                    // Make unique list
                    customSettings = new ArrayList<>(new LinkedHashSet<>(customSettings));

                    // Update current values
                    final ATXConfig config = new ATXConfig(updateCurrentValues(instJson, settings), customSettings);

                    // Fill installations
                    final ATXInstallation installation = new ATXInstallation(name, toolName, config);
                    list.add(installation);
                }
            }

            setInstallations(list.toArray(new ATXInstallation[0]));
            save();
            return true;
        }

        /**
         * Synchronizes current ATX configuration with default configuration by overriding their current values and
         * saving them as new ATX installation.
         *
         * <p>
         * This method will be automatically called by {@link ETPlugin#syncATXConfiguration()} to avoid circular
         * dependencies while loading other plugins. Explicit call to {@link #save()} is required.
         */
        public void syncWithDefaultConfig() {
            final List<ATXInstallation> list = new ArrayList<>();
            for (final ATXInstallation installation : installations.clone()) {
                final ATXConfig currentConfig = installation.getConfig();
                final ATXConfig newConfig = defaultConfig.clone();

                // Synchronize settings
                if (currentConfig != null) {
                    for (final ATXSetting<?> newSetting : newConfig.getSettings()) {
                        final Optional<ATXSetting<?>> currentSetting =
                                currentConfig.getSettingByName(newSetting.getName());
                        currentSetting.ifPresent(atxSetting ->
                                newConfig.setSettingValueByName(atxSetting.getName(),
                                    onDemandValueMigration(atxSetting)));
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
        }

        /**
         * Updates the current values for each ATX setting.
         *
         * @param installation the JSONObject representing one installation
         * @param settings     the default ATX settings
         * @return the updated ATX settings
         */
        private List<ATXSetting<?>> updateCurrentValues(final JSONObject installation,
                                                        final List<ATXSetting<?>> settings) {
            for (final ATXSetting<?> setting : settings) {
                final JSONObject settingsGroup = installation.optJSONObject(setting.getGroup().getConfigName());
                if (settingsGroup != null) {
                    final Object currentSetting = settingsGroup.opt(setting.getName());
                    if (currentSetting instanceof String) {
                        if (setting.isSecret()) {
                            ((ATXSecretSetting) setting).setValue(Secret.fromString((String) currentSetting));
                        } else {
                            ((ATXTextSetting) setting).setValue((String) currentSetting);
                        }
                    } else if (currentSetting instanceof Boolean) {
                        ((ATXBooleanSetting) setting).setValue((Boolean) currentSetting);
                    }
                }
            }
            return settings;
        }

        /**
         * Gets the custom settings of a given ATX installation.
         *
         * @param installation the installation
         * @return the custom settings list
         */
        public List<ATXCustomSetting> getCustomSettings(final ATXInstallation installation) {
            return installation == null ? new ArrayList<>() : installation.getConfig().getCustomSettings();
        }

        /**
         * Gets the applicable custom settings.
         *
         * @return the applicable custom settings
         */
        public List<Descriptor<? extends ATXCustomSetting>> getApplicableCustomSettings() {
            return new ArrayList<>(ATXCustomSetting.all());
        }

        /**
         * Gets the tool descriptor holding the ECU-TEST installations.
         *
         * @return the tool descriptor
         */
        public ETInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(ETInstallation.DescriptorImpl.class);
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
         * @param httpProxy          the HTTP proxy
         * @param httpsProxy         the HTTPS proxy
         * @param ignoreSSL          specifies whether to ignore SSL issues
         * @return the form validation
         */
        @RequirePOST
        public FormValidation doTestConnection(@QueryParameter final String serverURL,
                                               @QueryParameter final String serverPort,
                                               @QueryParameter final String serverContextPath,
                                               @QueryParameter final boolean useHttpsConnection,
                                               @QueryParameter final String httpProxy,
                                               @QueryParameter final String httpsProxy,
                                               @QueryParameter final boolean ignoreSSL) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            final String proxyUrl = useHttpsConnection ? httpsProxy : httpProxy;
            return atxValidator.testConnection(serverURL, serverPort, serverContextPath, useHttpsConnection,
                    proxyUrl, ignoreSSL);
        }

        private Object onDemandValueMigration(final ATXSetting<?> atxSetting) {
            switch (atxSetting.getName()) {
                case "useSettingsFromServer": return migrateFromBooleanToString(atxSetting, "Always", "Never");
                case "archiveRecordings": return migrateFromBooleanToString(atxSetting, "True", "False");
                default: return atxSetting.getValue();
            }

        }

        private String migrateFromBooleanToString(final ATXSetting<?> atxSetting, final String valueForTrue,
                                                  final String valueForFalse) {
            if (atxSetting.getValue() instanceof Boolean) {
                if ((Boolean) atxSetting.getValue()) {
                    return valueForTrue;
                } else {
                    return valueForFalse;
                }
            }
            return (String) atxSetting.getValue();
        }
    }
}
