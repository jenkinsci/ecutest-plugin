/*
 * Copyright (c) 2015-2022 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.compat;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(value= Enclosed.class)
public class CompatibilityWarnerTest {


    @RunWith(Parameterized.class)
    public static class TestET2022p3AddDebugMessageForSingleBackslash {

        private String key;
        private String value;
        private boolean expected;

        public TestET2022p3AddDebugMessageForSingleBackslash(String key, String value, boolean expected) {
            this.key = key;
            this.value = value;
            this.expected = expected;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                {"", "\\value", true},
                {"", "val\\ue", true},
                {"", "value\\", true},
                {"", "\\\\value", false},
                {"", "val\\\\ue", false},
                {"", "value\\\\", false},
                {"", "\\\\val\\ue", true},
                {"", "\\\\value\\", true}
            });
        }

        @Test
        public void testET2022p3AddDebugMessageForSingleBackslashCorrectMatch() {

            CompatibilityWarner warner = new CompatibilityWarner();

            TTConsoleLogger logger = Mockito.mock(TTConsoleLogger.class);
            CompatibilityWarner.PackageInfo info = CompatibilityWarner.PackageInfo.PARAM;

            assert expected == warner.ET2022p3AddDebugMessageForSingleBackslash(toMap(key, value), logger, info);
        }

        private Map<String, String> toMap(String key, String value) {
            return Stream.of(new String[][]{
                {key, value}
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        }
    }
}
