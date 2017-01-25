/*
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
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import hudson.FilePath;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link ETLogPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogPublisherTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testConstructorStep() {
        final ETLogPublisher publisher = new ETLogPublisher();
        assertPublisher(publisher);

    }

    @Deprecated
    @Test
    public void testConstructor() {
        final ETLogPublisher publisher = new ETLogPublisher(false, false, false, false, false, true, true);
        assertPublisher(publisher);
    }

    @Test
    public void testRunListener() throws IOException {
        folder.create();
        final File infoLog = folder.newFile(ETLogPublisher.INFO_LOG_NAME);
        final File errorLog = folder.newFile(ETLogPublisher.ERROR_LOG_NAME);
        final FilePath settingsDir = new FilePath(folder.getRoot());
        final TaskListener listener = mock(TaskListener.class);
        ETLogPublisher.RunListenerImpl.onStarted(settingsDir, listener);

        assertFalse("Standard log should be deleted", infoLog.exists());
        assertFalse("Errot log should be deleted", errorLog.exists());
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher
     *            the publisher
     */
    private void assertPublisher(final ETLogPublisher publisher) {
        assertNotNull(publisher);
        assertFalse(publisher.isUnstableOnWarning());
        assertFalse(publisher.isFailedOnError());
        assertFalse(publisher.isTestSpecific());
        assertFalse(publisher.isAllowMissing());
        assertFalse(publisher.isRunOnFailed());
        assertTrue(publisher.isArchiving());
        assertTrue(publisher.isKeepAll());
    }
}
