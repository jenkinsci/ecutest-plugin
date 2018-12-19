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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.EnvVars;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ReportGeneratorSetting}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorSettingTest {

    @Test
    public void testNullConstructor() {
        final ReportGeneratorSetting setting = new ReportGeneratorSetting(null, null);
        assertThat(setting.getName(), is(""));
        assertThat(setting.getValue(), is(""));
    }

    @Test
    public void testConstructor() {
        final ReportGeneratorSetting setting = new ReportGeneratorSetting("name", "value");
        assertThat(setting.getName(), is("name"));
        assertThat(setting.getValue(), is("value"));
    }

    @Test
    public void testExpand() {
        final ReportGeneratorSetting setting = new ReportGeneratorSetting("${NAME}", "${VALUE}");
        final EnvVars envVars = new EnvVars();
        envVars.put("NAME", "name");
        envVars.put("VALUE", "value");
        final ReportGeneratorSetting expSetting = setting.expand(envVars);
        assertThat(expSetting.getName(), is("name"));
        assertThat(expSetting.getValue(), is("value"));
    }
}
