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
package de.tracetronic.jenkins.plugins.ecutest;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher.DescriptorImpl;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;

/**
 * Main entry point to this plugin for the {@link Jenkins} instance.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETPlugin {

    /**
     * Defines the minimum required ECU-TEST version supported by this plugin.
     */
    public static final ToolVersion ET_MIN_VERSION = new ToolVersion(6, 3, 0, 0);

    /**
     * Defines the maximum allowed ECU-TEST version supported by this plugin.
     */
    public static final ToolVersion ET_MAX_VERSION = new ToolVersion(7, 0, 0, 0);

    /**
     * Defines the TEST-GUIDE version that the provided ATX configuration is based on.
     */
    public static final ToolVersion ATX_VERSION = new ToolVersion(1, 49, 0, 0);

    /**
     * Helper class to easily compare tool versions defined by major, minor, micro and qualifier version. Mainly used to
     * enable or disable plugin features by comparing {@link ETPlugin#ET_MIN_VERSION} with the configured ECU-TEST
     * version.
     */
    public static final class ToolVersion implements Comparable<ToolVersion>, Serializable {

        private static final long serialVersionUID = 1L;

        private final int major;
        private final int minor;
        private final int micro;
        private final long qualifier;

        /**
         * Instantiates a new {@link ToolVersion}.
         *
         * @param major
         *            the major version
         * @param minor
         *            the minor version
         * @param micro
         *            the micro version
         * @param qualifier
         *            the qualifier version
         */
        public ToolVersion(final int major, final int minor, final int micro, final long qualifier) {
            super();
            if (major < 0 || minor < 0 || micro < 0 || qualifier < 0) {
                throw new IllegalArgumentException("Versions must be greater than or equal to 0");
            }
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.qualifier = qualifier;
        }

        @Override
        public int compareTo(final ToolVersion version) {
            if (major != version.major) {
                return Integer.compare(major, version.major);
            }
            if (minor != version.minor) {
                return Integer.compare(minor, version.minor);
            }
            if (micro != version.micro) {
                return Integer.compare(micro, version.micro);
            }
            if (qualifier != version.qualifier) {
                return Long.compare(qualifier, version.qualifier);
            }
            return 0;
        }

        /**
         * Compares two {@link ToolVersion}s but ignoring micro and qualifier.
         *
         * @param version
         *            the version to be compared
         * @return integer indicating comparison result
         * @see ToolVersion#compareTo(ToolVersion)
         */
        public int compareWithoutMicroTo(final ToolVersion version) {
            if (major != version.major) {
                return Integer.compare(major, version.major);
            }
            if (minor != version.minor) {
                return Integer.compare(minor, version.minor);
            }
            return 0;
        }

        /**
         * Compares two {@link ToolVersion}s but ignoring the qualifier.
         *
         * @param version
         *            the version to be compared
         * @return integer indicating comparison result
         * @see ToolVersion#compareTo(ToolVersion)
         */
        public int compareWithoutQualifierTo(final ToolVersion version) {
            if (major != version.major) {
                return Integer.compare(major, version.major);
            }
            if (minor != version.minor) {
                return Integer.compare(minor, version.minor);
            }
            if (micro != version.micro) {
                return Integer.compare(micro, version.micro);
            }
            return 0;
        }

        @Override
        public String toString() {
            return String.format("%d.%d.%d.%d", major, minor, micro, qualifier);
        }

        /**
         * Returns a shorter string representation without the qualifier version.
         *
         * @return the short version string
         */
        public String toMicroString() {
            return String.format("%d.%d.%d", major, minor, micro);
        }

        /**
         * Returns a shorter string representation without the micro and qualifier version.
         *
         * @return the short version string
         */
        public String toMinorString() {
            return String.format("%d.%d", major, minor);
        }

        @Override
        public boolean equals(final Object other) {
            boolean result = false;
            if (other instanceof ToolVersion) {
                final ToolVersion that = (ToolVersion) other;
                result = compareTo((ToolVersion) that) == 0;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 31).append(major).append(minor).append(micro).append(qualifier)
                    .toHashCode();
        }

        /**
         * Parses a version string and returns a {@link ToolVersion}.
         *
         * @param version
         *            the version string
         * @return the parsed version
         * @throws IllegalArgumentException
         *             if the format of the version string is invalid
         */
        public static ToolVersion parse(final String version) throws IllegalArgumentException {
            final Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\w+))?$");
            final Matcher matcher = pattern.matcher(version);
            if (!matcher.find() || matcher.groupCount() != 4) {
                throw new IllegalArgumentException(
                        "Version must be in form <major>.<minor>.<micro>.<qualifier>");
            }

            final int major = Integer.parseInt(matcher.group(1));
            final int minor = Integer.parseInt(matcher.group(2));
            final int micro = Integer.parseInt(matcher.group(3));
            final long qualifier = Long.parseLong(matcher.group(4));

            return new ToolVersion(major, minor, micro, qualifier);
        }
    }

    /**
     * Retains backward compatibility for renamed classes.
     */
    @SuppressWarnings("rawtypes")
    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    public static void addAliases() {
        final String configPath = "de.tracetronic.jenkins.plugins.ecutest.test.config.";
        final HashMap<String, Class> classMap = new HashMap<String, Class>();
        classMap.put(configPath + "ImportPackageTMSConfig", ImportPackageConfig.class);
        classMap.put(configPath + "ImportPackageTMSDirConfig", ImportPackageDirConfig.class);
        classMap.put(configPath + "ImportProjectTMSConfig", ImportProjectConfig.class);
        classMap.put(configPath + "ImportProjectTMSDirConfig", ImportProjectDirConfig.class);

        for (final Entry<String, Class> entry : classMap.entrySet()) {
            Items.XSTREAM2.addCompatibilityAlias(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Synchronizes the current ATX configuration with the default one.
     */
    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public void syncATXConfiguration() {
        final DescriptorImpl descriptor = Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
        if (descriptor != null) {
            descriptor.syncWithDefaultConfig();
        }
    }

    /**
     * Registers the plugin icons to global icon set.
     */
    @Initializer(after = InitMilestone.JOB_LOADED)
    public void registerIcons() {
        for (final String name : new String[] {
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
                "trace-report",
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

    /**
     * Gets the icon file name by class specification.
     *
     * @param iconClassName
     *            the icon class name
     * @param iconStyle
     *            the icon style
     * @return the icon file name
     */
    public static String getIconFileName(final String iconClassName, final String iconStyle) {
        final String iconClass = iconClassName + " " + iconStyle;
        try {
            // FIXME: workaround signature changes in different versions of {@link IconSet} by reflection
            final Method getIconByClassSpec = IconSet.class.getMethod("getIconByClassSpec", Object.class);
            return ((Icon) getIconByClassSpec.invoke(IconSet.icons, iconClass)).getUrl();
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // ignore
        }
        return IconSet.icons.getIconByClassSpec(iconClass).getUrl();
    }
}
