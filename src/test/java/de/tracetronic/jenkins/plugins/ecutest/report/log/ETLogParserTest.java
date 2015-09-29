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
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import hudson.FilePath;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitTestResultParser;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogAnnotation.Severity;

/**
 * System tests for {@link JUnitTestResultParser}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogParserTest {

    @Test
    public void testMissingLog() throws Exception {
        final List<ETLogAnnotation> annotations = parseResults("");
        assertEquals(0, annotations.size());
    }

    @Test
    public void testEmptyLog() throws Exception {
        final List<ETLogAnnotation> annotations = parseResults("empty.log");
        assertEquals(0, annotations.size());
    }

    @Test
    public void testStandardLog() throws Exception {
        final List<ETLogAnnotation> annotations = parseResults("ECU_TEST_OUT.log");
        assertEquals(1, annotations.size());

        final ETLogAnnotation warning = annotations.get(0);
        assertThat(warning.getLineNumber(), is(19));
        assertThat(warning.getTimestamp(), is("2015-09-01 18:00:00.000"));
        assertThat(warning.getContext(), is("MainThread"));
        assertThat(warning.getSeverity(), is(Severity.WARNING));
        assertThat(warning.getMessage(), is("Test warning message"));
    }

    @Test
    public void testErrorLog() throws Exception {
        final List<ETLogAnnotation> annotations = parseResults("ECU_TEST_ERR.log");
        assertEquals(2, annotations.size());

        final ETLogAnnotation warning = annotations.get(1);
        assertThat(warning.getLineNumber(), is(29));
        assertThat(warning.getTimestamp(), is("2015-09-01 18:00:00.000"));
        assertThat(warning.getContext(), is("MainThread"));
        assertThat(warning.getSeverity(), is(Severity.ERROR));
        assertThat(warning.getMessage(), containsString("ParamError: Ungültiger Parametername: result"));
    }

    private List<ETLogAnnotation> parseResults(final String filename) throws InvocationTargetException {
        final ETLogParser parser = new ETLogParser();
        final URL url = this.getClass().getResource(filename);
        final FilePath logFile = new FilePath(new File(url.getFile()));
        final List<ETLogAnnotation> list = new ArrayList<ETLogAnnotation>();
        for (final ETLogAnnotation annotation : parser.parse(logFile)) {
            list.add(annotation);
        }
        return list;
    }
}
