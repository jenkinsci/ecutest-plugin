/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

/**
 * Unit tests for {@link ImportProjectArchiveConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectArchiveConfigTest {

    @Test
    public void testNullConstructor() {
        final ImportProjectArchiveConfig config = new ImportProjectArchiveConfig(null, null, null, false);
        assertThat(config.getProjectPath(), is(""));
        assertThat(config.getImportPath(), is(""));
        assertThat(config.getImportConfigPath(), is(""));
        assertFalse(config.isReplaceFiles());
    }

    @Test
    public void testExpand() {
        final ImportProjectArchiveConfig config = new ImportProjectArchiveConfig("${ARCHIVE_PATH}", "${IMPORT_PATH}",
                "${IMPORT_CONFIG_PATH}", true);
        final EnvVars envVars = new EnvVars();
        envVars.put("ARCHIVE_PATH", "test.prz");
        envVars.put("IMPORT_PATH", "import");
        envVars.put("IMPORT_CONFIG_PATH", "import");
        final ImportProjectArchiveConfig expConfig = config.expand(envVars);
        assertThat(expConfig.getProjectPath(), is("test.prz"));
        assertThat(expConfig.getImportPath(), is("import"));
        assertThat(expConfig.getImportConfigPath(), is("import"));
        assertTrue(config.isReplaceFiles());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImportProjectArchiveConfig.class).verify();
    }
}
