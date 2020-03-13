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
 * Integration tests for {@link TestPackageScanner}.
 */
public class TestPackageScannerIT extends IntegrationTestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    @WithoutJenkins
    public void testFilePattern() throws IOException {
        final TestPackageScanner scanner = new TestPackageScanner(null, false, null);
        assertEquals("Wrong file pattern for package files", "*.pkg", scanner.getFilePattern());
    }

    @Test
    @WithoutJenkins
    public void testRecursiveFilePattern() throws IOException {
        final TestPackageScanner scanner = new TestPackageScanner(null, true, null);
        assertEquals("Wrong recursive file pattern for package files", "**/**.pkg", scanner.getFilePattern());
    }

    @Test
    public void testNoPackages() throws Exception {
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TestPackageScanner scanner = new TestPackageScanner(folder.newFolder().getAbsolutePath(), false, launcher);
        assertTrue("No packages should be found", scanner.scanTestFiles().isEmpty());
    }

    @Test
    public void testScanPackages() throws Exception {
        final File testFolder = folder.newFolder();
        File.createTempFile("test", ".pkg", testFolder);
        File.createTempFile("test", ".pkg", testFolder);
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TestPackageScanner scanner = new TestPackageScanner(testFolder.getAbsolutePath(), false, launcher);
        assertEquals("Two packages should be found", 2, scanner.scanTestFiles().size());
    }

    @Test
    public void testRecursiveScanPackages() throws Exception {
        folder.newFile("test.pkg");
        final File subFolder = folder.newFolder("tests");
        final File subPackage = new File(subFolder, "test.pkg");
        subPackage.createNewFile();
        final File subPackage2 = new File(subFolder, "test2.pkg");
        subPackage2.createNewFile();
        final Launcher launcher = jenkins.createOnlineSlave().createLauncher(jenkins.createTaskListener());
        final TestPackageScanner scanner = new TestPackageScanner(folder.getRoot().getAbsolutePath(), true, launcher);
        assertEquals("Three packages should be found recursively", 3, scanner.scanTestFiles().size());
    }
}
