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
