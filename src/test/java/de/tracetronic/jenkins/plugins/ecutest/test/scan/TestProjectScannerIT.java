/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.scan;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.Launcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.WithoutJenkins;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link TestProjectScanner}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestProjectScannerIT extends IntegrationTestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    @WithoutJenkins
    public void testFilePattern() throws IOException {
        final TestProjectScanner scanner = new TestProjectScanner(null, false, null);
        assertEquals("Wrong file pattern for project files", "*.prj", scanner.getFilePattern());
    }

    @Test
    @WithoutJenkins
    public void testRecursiveFilePattern() throws IOException {
        final TestProjectScanner scanner = new TestProjectScanner(null, true, null);
        assertEquals("Wrong recursive file pattern for project files", "**/**.prj", scanner.getFilePattern());
    }

    @Test
    public void testNoPackages() throws Exception {
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TestProjectScanner scanner = new TestProjectScanner(folder.newFolder().getAbsolutePath(), false, launcher);
        assertTrue("No projects should be found", scanner.scanTestFiles().isEmpty());
    }

    @Test
    public void testScanPackages() throws Exception {
        final File testFolder = folder.newFolder();
        File.createTempFile("test", ".prj", testFolder);
        File.createTempFile("test", ".prj", testFolder);
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TestProjectScanner scanner = new TestProjectScanner(testFolder.getAbsolutePath(), false, launcher);
        assertEquals("Two projects should be found", 2, scanner.scanTestFiles().size());
    }

    @Test
    public void testRecursiveScanPackages() throws Exception {
        folder.newFile("test.prj");
        final File subFolder = folder.newFolder("tests");
        final File subPackage = new File(subFolder, "test.prj");
        subPackage.createNewFile();
        final File subPackage2 = new File(subFolder, "test2.prj");
        subPackage2.createNewFile();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TestProjectScanner scanner = new TestProjectScanner(folder.getRoot().getAbsolutePath(), true, launcher);
        assertEquals("Three projects should be found recursively", 3, scanner.scanTestFiles().size());
    }
}
