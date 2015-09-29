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
package de.tracetronic.jenkins.plugins.ecutest.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hudson.Launcher;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.WithoutJenkins;

import de.tracetronic.jenkins.plugins.ecutest.SystemTestBase;

/**
 * System test for {@link TestPackageScanner}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestPackageScannerST extends SystemTestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    @WithoutJenkins
    public void testFilePattern() throws IOException {
        final TestPackageScanner scanner = new TestPackageScanner(null, false, null);
        assertEquals("Wrong file pattern for package files", "*.pkg", scanner.getFilePattern()[0]);
    }

    @Test
    @WithoutJenkins
    public void testRecursiveFilePattern() throws IOException {
        final TestPackageScanner scanner = new TestPackageScanner(null, true, null);
        assertEquals("Wrong recursive file pattern for package files", "**/**.pkg", scanner.getFilePattern()[0]);
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
