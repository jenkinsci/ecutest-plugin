/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import com.google.common.collect.Maps;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class holding ATX server specific settings in order to publish ATX reports.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXServer implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ATXInstallation installation;

    private transient CpsScript script;

    /**
     * Instantiates a new {@link ATXServer}.
     *
     * @param installation the ATX installation
     */
    public ATXServer(final ATXInstallation installation) {
        this.installation = installation;
    }

    /**
     * @return the ATX installation
     */
    @Whitelisted
    public ATXInstallation getInstallation() {
        return installation;
    }

    /**
     * Sets the pipeline script.
     *
     * @param script the pipeline script
     */
    public void setScript(final CpsScript script) {
        this.script = script;
    }

    /**
     * Publishes ATX reports with default archiving settings.
     */
    @Whitelisted
    public void publish() {
        publish(false, false, true, true);
    }

    /**
     * Publishes ATX reports with given archiving settings.
     *
     * @param allowMissing specifies whether missing reports are allowed
     * @param runOnFailed  specifies whether this publisher even runs on a failed build
     * @param archiving    specifies whether archiving artifacts is enabled
     * @param keepAll      specifies whether artifacts are archived for all successful builds,
     *                     otherwise only the most recent
     */
    @Whitelisted
    public void publish(final boolean allowMissing, final boolean runOnFailed,
                        final boolean archiving, final boolean keepAll) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("atxName", installation.getName());
        stepVariables.put("atxInstallation", installation);
        stepVariables.put("allowMissing", allowMissing);
        stepVariables.put("runOnFailed", runOnFailed);
        stepVariables.put("archiving", archiving);
        stepVariables.put("keepAll", keepAll);
        script.invokeMethod("publishATX", stepVariables);
    }

    /**
     * Publishes ATX reports with given archiving settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void publish(final Map<String, Object> settings) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("atxName", installation.getName());
        stepVariables.put("atxInstallation", installation);
        stepVariables.putAll(settings);
        script.invokeMethod("publishATX", stepVariables);
    }

    /**
     * Gets a single ATX setting by name.
     *
     * @param settingName the setting name
     * @return the setting
     */
    @Whitelisted
    public ATXSetting<?> getSetting(String settingName) {
        return installation.getConfig().getSettingByName(settingName).orElse(null);
    }

    /**
     * Gets all ATX settings as map.
     *
     * @return the settings map
     */
    @Whitelisted
    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        installation.getConfig().getSettings().forEach(setting -> {
            settings.put(setting.getName(), setting.getValue());
        });
        return settings;
    }

    /**
     * Overrides an existing ATX setting with the specified value.
     *
     * @param settingName  the setting name
     * @param settingValue the setting value as {@code String} or {@code Boolean}
     */
    @SuppressWarnings("rawtypes")
    @Whitelisted
    public void overrideSetting(String settingName, Object settingValue) {
        final Optional<ATXSetting> setting = installation.getConfig().getSettingByName(settingName);
        if (setting.isPresent()) {
            if (settingValue instanceof String || settingValue instanceof Boolean) {
                script.println(String.format("[TT] INFO: Overriding ATX setting %s=%s", settingName, settingValue));
                setting.get().setValue(settingValue);
            } else {
                script.println("[TT] WARN: Ignore overriding ATX setting due to invalid value!");
            }
        } else {
            script.println("[TT] WARN: Ignore overriding ATX setting due to invalid key!");
        }
    }

    /**
     * Override multiple ATX settings via named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void overrideSettings(final Map<String, Object> settings) {
        settings.forEach(this::overrideSetting);
    }
}
