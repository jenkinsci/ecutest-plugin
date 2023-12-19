/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.compat;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Warns the user in case of possible compatibility issues between this version of the ecu.test plugin and the version
 * of ecu.test used by the user. Different methods hint at different compatibility issues.
 */
public class CompatibilityWarner {
    /**
     * Adds a debug message in case the user uses single backslashes in Constants and package parameters (relevant from
     * ecu.test 2022.3 upwards).
     * @param inputMap the map the values of which are checked for single backslashes
     * @param logger logger where the warnings for single backslashes are written to
     * @param info information about the kind of values handed as inputMap
     * @return true if any matches are found, else false
     */
    public boolean et2022p3AddDebugMessageForSingleBackslash(final Map<String, String> inputMap,
                                                             final TTConsoleLogger logger,
                                                             final PackageInfo info) {

        boolean isMatchFound = false;

        // pattern matches if a single backslash is found within a string at any position
        final Pattern p = Pattern.compile(".*(?<!\\\\)(\\\\)(?!\\\\).*");
        for (String value : inputMap.values()) {
            final Matcher m = p.matcher(value);
            if (m.matches()) {
                isMatchFound = true;
                logger.logDebug("Single backslash found in " + info.getLogMessage() + " '" + value
                    + "' - note that invalid control characters are not allowed in "
                    + "ecu.test 2022.3 and newer versions.");

            }
        }
        return isMatchFound;
    }

    /**
     * Enum to be used as variable for generic warning messages.
     */
    public enum PackageInfo {
        /**
         * Using string specific for parameter.
         */
        PARAM("parameter"),

        /**
         * Using string specific for constant.
         */
        CONST("constant value");

        private final String logMessage;

        /**
         * Constructor.
         * @param logMessage log message
         */
        PackageInfo(final String logMessage) {
            this.logMessage = logMessage;
        }

        /**
         * Gets the log message.
         * @return log message
         */
        public String getLogMessage() {
            return logMessage;
        }
    }
}
