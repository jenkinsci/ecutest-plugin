/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class providing ATX related functions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public final class ATXUtil {

    /**
     * Instantiates a new {@link ATXUtil}.
     */
    private ATXUtil() {
    }

    /**
     * Removes special characters from test name and replaces with underscore "_".
     *
     * @param testName
     *            the test name
     * @return the ATX compliant test name
     */
    public static String getValidATXName(final String testName) {
        String validATXName = "DefaultTestName";
        if (testName != null && StringUtils.countMatches(testName, "_") != testName.length()) {
            validATXName = testName;

            final Map<String, String> specialCharMap = new HashMap<String, String>();
            specialCharMap.put("ä", "ae");
            specialCharMap.put("Ä", "Ae");
            specialCharMap.put("ö", "oe");
            specialCharMap.put("Ö", "Oe");
            specialCharMap.put("ü", "ue");
            specialCharMap.put("Ü", "Ue");
            specialCharMap.put("ß", "ss");
            specialCharMap.put("-", "_");
            specialCharMap.put("\\.", "_");
            specialCharMap.put(" ", "");

            // Replace special chars
            for (final Entry<String, String> specialChar : specialCharMap.entrySet()) {
                validATXName = validATXName.replaceAll(specialChar.getKey(), specialChar.getValue());
            }

            // Remove coherent underscores
            validATXName = removeCoherentUnderscores(validATXName);

            // Add 'i' char if test name starts with digit
            if (Character.isDigit(validATXName.charAt(0))) {
                validATXName = String.format("i%s", validATXName);
            }
        }
        return validATXName;
    }

    /**
     * Removes the coherent underscores.
     *
     * @param testName
     *            the test name
     * @return the string without coherent underscores
     */
    private static String removeCoherentUnderscores(final String testName) {
        final String validATXName = testName.replace("__", "_");
        if (validATXName.equals(testName)) {
            return validATXName;
        } else {
            return removeCoherentUnderscores(validATXName);
        }
    }
}
