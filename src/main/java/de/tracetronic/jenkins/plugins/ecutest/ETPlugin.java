/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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

import hudson.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Main entry point to this plugin for the {@link Jenkins} instance.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETPlugin extends Plugin {

    /**
     * Defines the minimum required ECU-TEST version supported by this plugin.
     */
    public static final ToolVersion ET_MIN_VERSION = new ToolVersion(6, 3, 0, 0);

    /**
     * Defines the maximum allowed ECU-TEST version supported by this plugin.
     */
    public static final ToolVersion ET_MAX_VERSION = new ToolVersion(6, 4, 0, 0);

    /**
     * Defines the TEST-GUIDE version that the provided ATX configuration is based on.
     */
    public static final ToolVersion ATX_VERSION = new ToolVersion(1, 29, 0, 0);

    /**
     * Helper class to easily compare tool versions defined by major, minor, micro and qualifier version. Mainly used to
     * enable or disable plugin features by comparing {@link ETPlugin#ET_MIN_VERSION} with the configured ECU-TEST
     * version.
     */
    public static final class ToolVersion implements Comparable<ToolVersion> {

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
         * Returns an shorter string representation without the qualifier version.
         *
         * @return the short version string
         */
        public String toShortString() {
            return String.format("%d.%d.%d", major, minor, micro);
        }

        @Override
        public boolean equals(final Object that) {
            if (this == that) {
                return true;
            }
            if (that == null) {
                return false;
            }
            if (this.getClass() != that.getClass()) {
                return false;
            }
            return compareTo((ToolVersion) that) == 0;
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
}
