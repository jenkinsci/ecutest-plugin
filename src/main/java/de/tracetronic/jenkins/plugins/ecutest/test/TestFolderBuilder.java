/**
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
package de.tracetronic.jenkins.plugins.ecutest.test;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.scan.TestPackageScanner;
import de.tracetronic.jenkins.plugins.ecutest.test.scan.TestProjectScanner;

/**
 * Builder providing the execution of ECU-TEST packages and projects inside of a test folder.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestFolderBuilder extends AbstractTestBuilder {

    /**
     * Defines the default {@link ScanMode}.
     */
    protected static final ScanMode DEFAULT_SCANMODE = ScanMode.PACKAGES_AND_PROJECTS;

    /**
     * Defines the modes to scan the test folder.
     */
    public enum ScanMode {
        /**
         * Scan packages only.
         */
        PACKAGES_ONLY,

        /**
         * Scan projects only.
         */
        PROJECTS_ONLY,

        /**
         * Scan both packages and projects.
         */
        PACKAGES_AND_PROJECTS
    }

    // Scan settings
    @Nonnull
    private ScanMode scanMode = DEFAULT_SCANMODE;
    private boolean recursiveScan;

    // Test settings
    @Nonnull
    private PackageConfig packageConfig = PackageConfig.newInstance();
    @Nonnull
    private ProjectConfig projectConfig = ProjectConfig.newInstance();

    /**
     * Instantiates a new {@link TestFolderBuilder}.
     *
     * @param testFile
     *            the test folder
     */
    @DataBoundConstructor
    public TestFolderBuilder(final String testFile) {
        super(testFile);
    }

    /**
     * Instantiates a new {@link TestFolderBuilder}.
     *
     * @param testFile
     *            the test folder
     * @param scanMode
     *            the scan mode
     * @param recursiveScan
     *            specifies whether to scan recursively
     * @param testConfig
     *            the test configuration
     * @param packageConfig
     *            the package configuration
     * @param projectConfig
     *            the project configuration
     * @param executionConfig
     *            the execution configuration
     * @deprecated since 1.11 use {@link #TestFolderBuilder(String)}
     */
    @Deprecated
    public TestFolderBuilder(final String testFile, final ScanMode scanMode, final boolean recursiveScan,
            final TestConfig testConfig, final PackageConfig packageConfig, final ProjectConfig projectConfig,
            final ExecutionConfig executionConfig) {
        super(testFile, testConfig, executionConfig);
        this.scanMode = scanMode;
        this.recursiveScan = recursiveScan;
        this.packageConfig = packageConfig == null ? PackageConfig.newInstance() : packageConfig;
        this.projectConfig = projectConfig == null ? ProjectConfig.newInstance() : projectConfig;
    }

    /**
     * @return the scanMode
     */
    @Nonnull
    public ScanMode getScanMode() {
        return scanMode;
    }

    /**
     * @return the recursiveScan
     */
    public boolean isRecursiveScan() {
        return recursiveScan;
    }

    /**
     * @return the package configuration
     */
    @Nonnull
    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    /**
     * @return the project configuration
     */
    @Nonnull
    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    /**
     * @param scanMode
     *            the scan mode
     */
    @DataBoundSetter
    public void setScanMode(@Nonnull final ScanMode scanMode) {
        this.scanMode = scanMode;
    }

    /**
     * @param recursiveScan
     *            the recursive scan mode
     */
    @DataBoundSetter
    public void setRecursiveScan(final boolean recursiveScan) {
        this.recursiveScan = recursiveScan;
    }

    /**
     * @param packageConfig
     *            the package configuration
     */
    @DataBoundSetter
    public void setPackageConfig(@Nonnull final PackageConfig packageConfig) {
        this.packageConfig = packageConfig;
    }

    /**
     * @param projectConfig
     *            the project configuration
     */
    @DataBoundSetter
    public void setProjectConfig(@Nonnull final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    @Override
    protected String getTestFilePath(final String testFile, final String pkgDir, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        String testFolderPath = null;
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (testFile.isEmpty()) {
            logger.logError("No test folder declared!");
        } else {
            final File fullTestFolder = new File(pkgDir, testFile);
            final FilePath fullTestFolderPath = new FilePath(launcher.getChannel(), fullTestFolder.getPath());
            if (fullTestFolderPath.exists()) {
                testFolderPath = fullTestFolderPath.getRemote();
            } else {
                logger.logError(String.format("%s does not exist!", fullTestFolderPath.getRemote()));
            }
        }
        return testFolderPath;
    }

    @Override
    protected boolean runTest(final String testFolder, final TestConfig testConfig,
            final ExecutionConfig executionConfig, final Run<?, ?> run, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        // Scan test folder for tests
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Executing test folder...");
        logger.logInfo(String.format("- Scanning test folder: %s", testFolder));
        final List<String> pkgFiles = scanPackages(testFolder, launcher, listener);
        final List<String> prjFiles = scanProjects(testFolder, launcher, listener);

        // Expand package configuration
        final EnvVars buildEnv = run.getEnvironment(listener);
        final PackageConfig packageConfig = getPackageConfig().expand(buildEnv);

        // Run packages
        for (final String pkgFile : pkgFiles) {
            final PackageClient testClient = new PackageClient(pkgFile, testConfig, packageConfig, executionConfig);
            logger.logInfo(String.format("Executing package %s...", pkgFile));
            if (testClient.runTestCase(launcher, listener)) {
                logger.logInfo("Package executed successfully.");
            } else {
                logger.logError("Executing package failed!");
                return false;
            }

            // Add action for injecting environment variables
            final int testId = getTestId(run);
            final TestEnvInvisibleAction envAction = new TestEnvInvisibleAction(testId, testClient);
            run.addAction(envAction);
        }

        // Expand project configuration
        final ProjectConfig projectConfig = getProjectConfig().expand(buildEnv);

        // Run projects
        for (final String prjFile : prjFiles) {
            final ProjectClient testClient = new ProjectClient(prjFile, testConfig, projectConfig, executionConfig);
            logger.logInfo(String.format("Executing project %s...", prjFile));
            if (testClient.runTestCase(launcher, listener)) {
                logger.logInfo("Project executed successfully.");
            } else {
                logger.logError("Executing project failed!");
                return false;
            }

            // Add action for injecting environment variables
            final int testId = getTestId(run);
            final TestEnvInvisibleAction envAction = new TestEnvInvisibleAction(testId, testClient);
            run.addAction(envAction);
        }

        return true;
    }

    /**
     * Scans for ECU-TEST packages.
     *
     * @param testFolder
     *            the test folder
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the list of found packages
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<String> scanPackages(final String testFolder, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        List<String> pkgFiles = new ArrayList<String>();
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (scanMode.equals(ScanMode.PACKAGES_ONLY) || scanMode.equals(ScanMode.PACKAGES_AND_PROJECTS)) {
            final TestPackageScanner scanner = new TestPackageScanner(testFolder, isRecursiveScan(), launcher);
            pkgFiles = scanner.scanTestFiles();
            if (pkgFiles.isEmpty()) {
                logger.logInfo("-> No packages found!");
            } else {
                logger.logInfo(String.format("-> Found %d package(s).", pkgFiles.size()));
            }
        }
        return pkgFiles;
    }

    /**
     * Scan for ECU-TEST projects.
     *
     * @param testFolder
     *            the test folder
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the list of found projects
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<String> scanProjects(final String testFolder, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        List<String> prjFiles = new ArrayList<String>();
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (scanMode.equals(ScanMode.PROJECTS_ONLY) || scanMode.equals(ScanMode.PACKAGES_AND_PROJECTS)) {
            final TestProjectScanner prjScanner = new TestProjectScanner(testFolder, isRecursiveScan(), launcher);
            prjFiles = prjScanner.scanTestFiles();
            if (prjFiles.isEmpty()) {
                logger.logInfo("-> No projects found!");
            } else {
                logger.logInfo(String.format("-> Found %d project(s).", prjFiles.size()));
            }
        }
        return prjFiles;
    }

    /**
     * DescriptorImpl for {@link TestFolderBuilder}.
     */
    @Extension(ordinal = 1000)
    public static final class DescriptorImpl extends AbstractTestDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(TestFolderBuilder.class);
            load();
        }

        /**
         * @return the default scan mode
         */
        public ScanMode getDefaultScanMode() {
            return ScanMode.PACKAGES_AND_PROJECTS;
        }

        /**
         * Fills the scan mode drop-down menu.
         *
         * @return the scan mode items
         */
        public ListBoxModel doFillScanModeItems() {
            final ListBoxModel items = new ListBoxModel();
            items.add(Messages.TestFolderBuilder_ScanMode_Packages(), ScanMode.PACKAGES_ONLY.toString());
            items.add(Messages.TestFolderBuilder_ScanMode_Projects(), ScanMode.PROJECTS_ONLY.toString());
            items.add(Messages.TestFolderBuilder_ScanMode_Both(), ScanMode.PACKAGES_AND_PROJECTS.toString());
            return items;
        }

        /**
         * Validates the test folder.
         *
         * @param value
         *            the test folder
         * @return the form validation
         */
        @Override
        public FormValidation doCheckTestFile(@QueryParameter final String value) {
            return testValidator.validateTestFolder(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.TestFolderBuilder_DisplayName();
        }
    }
}
