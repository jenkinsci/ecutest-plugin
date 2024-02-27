/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import hudson.FilePath;
import hudson.model.TaskListener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ETLogPublisher}.
 */
public class ETLogPublisherTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testConstructorStep() {
        final ETLogPublisher publisher = new ETLogPublisher();
        assertPublisher(publisher);

    }

    @Test
    public void testRunListener() throws IOException {
        folder.create();
        final List<File> files = new ArrayList<>();
        for (String fileName : ETLogPublisher.INFO_LOG_NAMES) {
            files.add(folder.newFile(fileName));
        }
        for (String fileName : ETLogPublisher.ERROR_LOG_NAMES) {
            files.add(folder.newFile(fileName));
        }

        final FilePath settingsDir = new FilePath(folder.getRoot());
        final TaskListener listener = mock(TaskListener.class);
        ETLogPublisher.RunListenerImpl.onStarted(settingsDir, listener);

        files.forEach(f -> assertFalse("Log file should be deleted",f.exists()));
    }

    /**
     * Asserts the publisher properties.
     *
     * @param publisher the publisher
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
