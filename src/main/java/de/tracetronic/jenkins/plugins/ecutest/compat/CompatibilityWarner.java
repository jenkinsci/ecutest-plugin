/*
 * Copyright (c) 2015-2022 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.compat;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class CompatibilityWarner {
    /**
     *
     * @param inputMap the map the values of which are checked for single backslashes
     * @param logger logger where the warnings for single backslashes are written to
     * @param info information about the kind of values handed as inputMap
     * @return true if any matches are found, else false
     */
    public boolean ET2022p3AddDebugMessageForSingleBackslash(Map<String, String> inputMap, TTConsoleLogger logger,
                                                          PackageInfo info) {

        boolean isMatchFound = false;

        // pattern matches if a single backslash is found within a string at any position
        final Pattern p = Pattern.compile(".*(?<!\\\\)(\\\\)(?!\\\\).*");
        for (String value : inputMap.values()) {
            final Matcher m = p.matcher(value);
            if (m.matches()) {
                isMatchFound = true;
                logger.logDebug("Single backslash found in " + info.getLogMessage() + " '" + value
                    + "' - note that invalid control characters are not allowed in "
                    + "ECU-TEST 2022.3 and newer versions.");

            }
        }
        return isMatchFound;
    }

    public enum PackageInfo {
        PARAM("parameter"),
        CONST("constant value");

        private final String logMessage;

        PackageInfo(String logMessage) {
            this.logMessage = logMessage;
        }

        public String getLogMessage() {
            return logMessage;
        }
    }
}
