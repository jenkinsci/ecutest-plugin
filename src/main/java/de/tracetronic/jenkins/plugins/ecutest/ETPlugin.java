/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import jenkins.model.Jenkins;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Main entry point to this plugin for the {@link Jenkins} instance.
 */
public class ETPlugin {

    /**
     * Defines the minimum required ECU-TEST version supported by this plugin.
     */
    public static final ToolVersion ET_MIN_VERSION = new ToolVersion(2021, 1, 0);

    /**
     * Defines the maximum allowed ECU-TEST version supported by this plugin.
     */
    public static final ToolVersion ET_MAX_VERSION = new ToolVersion(2023, 1, 1);

    /**
     * Defines the minimum TEST-GUIDE version supported by this plugin.
     */
    public static final ToolVersion ATX_MIN_VERSION = new ToolVersion(1, 65, 0);

    /**
     * Defines the TEST-GUIDE version that the provided ATX configuration is based on.
     */
    public static final ToolVersion ATX_CONFIG_VERSION = new ToolVersion(1, 129, 0);

    /**
     * Retains backward compatibility for renamed classes.
     */
    @SuppressWarnings("rawtypes")
    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void addAliases() {
        final String configPath = "de.tracetronic.jenkins.plugins.ecutest.test.config.";
        final HashMap<String, Class> classMap = new HashMap<>();
        classMap.put(configPath + "ImportPackageTMSConfig", ImportPackageConfig.class);
        classMap.put(configPath + "ImportPackageTMSDirConfig", ImportPackageDirConfig.class);
        classMap.put(configPath + "ImportProjectTMSConfig", ImportProjectConfig.class);
        classMap.put(configPath + "ImportProjectTMSDirConfig", ImportProjectDirConfig.class);

        for (final Entry<String, Class> entry : classMap.entrySet()) {
            Items.XSTREAM2.addCompatibilityAlias(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the icon file name by class specification.
     *
     * @param iconClassName the icon class name
     * @param iconStyle     the icon style
     * @return the icon file name
     */
    public static String getIconFileName(final String iconClassName, final String iconStyle) {
        final String iconClass = iconClassName + " " + iconStyle;
        return IconSet.icons.getIconByClassSpec(iconClass).getUrl();
    }

    /**
     * Synchronizes the current ATX configuration with the default one.
     */
    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public void syncATXConfiguration() {
        final ATXInstallation.DescriptorImpl descriptor = Jenkins.get()
            .getDescriptorByType(ATXInstallation.DescriptorImpl.class);
        if (descriptor != null) {
            descriptor.syncWithDefaultConfig();
            descriptor.save();
        }
    }

    /**
     * Registers the plugin icons to global icon set.
     */
    @Initializer(after = InitMilestone.JOB_LOADED)
    public void registerIcons() {
        for (final String name : new String[]{
            "atx-report",
            "atx-trend",
            "ecu-test",
            "ecu-test-pkg",
            "ecu-test-prj",
            "report-generator",
            "test-guide",
            "test-param",
            "tool-param",
            "trf-report",
            "trace-check",
            "trace-report"
        }) {
            // Register small (16x16) icons
            IconSet.icons.addIcon(new Icon(
                    String.format("icon-ecutest-%s icon-sm", name),
                    String.format("ecutest/icons/16x16/%s.png", name),
                    Icon.ICON_SMALL_STYLE, IconType.PLUGIN)
            );

            // Register medium (24x24) icons
            IconSet.icons.addIcon(new Icon(
                    String.format("icon-ecutest-%s icon-md", name),
                    String.format("ecutest/icons/24x24/%s.png", name),
                    Icon.ICON_MEDIUM_STYLE, IconType.PLUGIN)
            );

            // Register large (32x32) icons
            IconSet.icons.addIcon(new Icon(
                    String.format("icon-ecutest-%s icon-lg", name),
                    String.format("ecutest/icons/32x32/%s.png", name),
                    Icon.ICON_LARGE_STYLE, IconType.PLUGIN)
            );

            // Register x-large (48x48) icons
            IconSet.icons.addIcon(new Icon(
                    String.format("icon-ecutest-%s icon-xlg", name),
                    String.format("ecutest/icons/48x48/%s.png", name),
                    Icon.ICON_XLARGE_STYLE, IconType.PLUGIN)
            );
        }
    }
}
