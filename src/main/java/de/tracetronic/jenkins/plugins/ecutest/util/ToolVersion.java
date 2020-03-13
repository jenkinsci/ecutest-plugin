/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to easily compare tool versions defined by major, minor, micro and qualifier version. Mainly used to
 * enable or disable plugin features by comparing {@link de.tracetronic.jenkins.plugins.ecutest.ETPlugin#ET_MIN_VERSION}
 * with the configured ECU-TEST version.
 */
public final class ToolVersion implements Comparable<ToolVersion>, Serializable {

    private static final long serialVersionUID = 1L;

    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;

    /**
     * Instantiates a new {@link ToolVersion}.
     *
     * @param major the major version
     * @param minor the minor version
     * @param micro the micro version
     */
    public ToolVersion(final int major, final int minor, final int micro) {
        this(major, minor, micro, "");
    }

    /**
     * Instantiates a new {@link ToolVersion}.
     *
     * @param major     the major version
     * @param minor     the minor version
     * @param micro     the micro version
     * @param qualifier the qualifier version
     */
    public ToolVersion(final int major, final int minor, final int micro, final String qualifier) {
        super();

        if (major < 0 || minor < 0 || micro < 0) {
            throw new IllegalArgumentException("Versions must be greater than or equal to 0");
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
    }

    /**
     * Parses a version string and returns a {@link ToolVersion}.
     *
     * @param version the version string
     * @return the parsed version
     * @throws IllegalArgumentException if the format of the version string is invalid
     */
    public static ToolVersion parse(final String version) throws IllegalArgumentException {
        final Pattern pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:[.#](.*))?$");
        final Matcher matcher = pattern.matcher(version);
        if (!matcher.find() || matcher.groupCount() != 4) {
            throw new IllegalArgumentException(
                    "Version must be in form <major>.<minor>.<micro>.<qualifier>");
        }

        final int major = Integer.parseInt(matcher.group(1));
        final int minor = Integer.parseInt(matcher.group(2));
        final int micro = Integer.parseInt(matcher.group(3));
        final String qualifier = matcher.group(4);

        return new ToolVersion(major, minor, micro, qualifier);
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
        if (qualifier != null) {
            if (version.qualifier == null) {
                return 1;
            } else {
                return qualifier.compareTo(version.qualifier);
            }
        } else if (version.qualifier != null) {
            return -1;
        }

        return 0;
    }

    /**
     * Compares two {@link ToolVersion}s but ignoring micro and qualifier.
     *
     * @param version the version to be compared
     * @return integer indicating comparison result
     * @see #compareTo(ToolVersion)
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
     * @param version the version to be compared
     * @return integer indicating comparison result
     * @see #compareTo(ToolVersion)
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
        return String.format("%d.%d.%d.%s", major, minor, micro, qualifier);
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
            result = compareTo(that) == 0;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(major).append(minor).append(micro).append(qualifier).toHashCode();
    }
}

