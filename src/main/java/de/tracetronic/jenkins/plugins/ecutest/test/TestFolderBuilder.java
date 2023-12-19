/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.PackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.scan.TestPackageScanner;
import de.tracetronic.jenkins.plugins.ecutest.test.scan.TestProjectScanner;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder providing the execution of ecu.test packages and projects inside of a test folder.
 */
public class TestFolderBuilder extends AbstractTestBuilder {

    /**
     * Defines the default {@link ScanMode}.
     */
    protected static final ScanMode DEFAULT_SCANMODE = ScanMode.PACKAGES_AND_PROJECTS;
    // Scan settings
    @Nonnull
    private ScanMode scanMode = DEFAULT_SCANMODE;
    private boolean recursiveScan;
    private boolean failFast = true;
    // Test settings
    @Nonnull
    private PackageConfig packageConfig = PackageConfig.newInstance();
    @Nonnull
    private ProjectConfig projectConfig = ProjectConfig.newInstance();

    /**
     * Instantiates a new {@link TestFolderBuilder}.
     *
     * @param testFile the test folder
     */
    @DataBoundConstructor
    public TestFolderBuilder(@Nonnull final String testFile) {
        super(testFile);
    }

    @Nonnull
    public ScanMode getScanMode() {
        return scanMode;
    }

    @DataBoundSetter
    public void setScanMode(@Nonnull final ScanMode scanMode) {
        this.scanMode = scanMode;
    }

    public boolean isRecursiveScan() {
        return recursiveScan;
    }

    @DataBoundSetter
    public void setRecursiveScan(final boolean recursiveScan) {
        this.recursiveScan = recursiveScan;
    }

    public boolean isFailFast() {
        return failFast;
    }

    @DataBoundSetter
    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    @Nonnull
    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    @DataBoundSetter
    public void setPackageConfig(@CheckForNull final PackageConfig packageConfig) {
        this.packageConfig = packageConfig == null ? PackageConfig.newInstance() : packageConfig;
    }

    @Nonnull
    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    @DataBoundSetter
    public void setProjectConfig(@CheckForNull final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig == null ? ProjectConfig.newInstance() : projectConfig;
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
                              final ExecutionConfig executionConfig, final Run<?, ?> run, final FilePath workspace,
                              final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
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
            if (testClient.runTestCase(run, workspace, launcher, listener)) {
                addBuildAction(run, testClient);
                if (testClient.isAborted()) {
                    logger.logWarn("Package execution aborted!");
                    return false;
                } else {
                    logger.logInfo("Package executed successfully.");
                }
            } else {
                logger.logError("Executing package failed!");
                if (failFast) {
                    return false;
                } else {
                    logger.logWarn("Package execution will be continued due to disabled fail fast mode.");
                }
            }
        }

        // Expand project configuration
        final ProjectConfig projectConfig = getProjectConfig().expand(buildEnv);

        // Run projects
        for (final String prjFile : prjFiles) {
            final ProjectClient testClient = new ProjectClient(prjFile, testConfig, projectConfig, executionConfig);
            logger.logInfo(String.format("Executing project %s...", prjFile));
            if (testClient.runTestCase(run, workspace, launcher, listener)) {
                addBuildAction(run, testClient);
                if (testClient.isAborted()) {
                    logger.logWarn("Project execution aborted!");
                    return false;
                } else {
                    logger.logInfo("Project executed successfully.");
                }
            } else {
                logger.logError("Executing project failed!");
                if (failFast) {
                    return false;
                } else {
                    logger.logWarn("Project execution will be continued due to disabled fail fast mode.");
                }
            }
        }

        return true;
    }

    /**
     * Scans for ecu.test packages.
     *
     * @param testFolder the test folder
     * @param launcher   the launcher
     * @param listener   the listener
     * @return the list of found packages
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private List<String> scanPackages(final String testFolder, final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        List<String> pkgFiles = new ArrayList<>();
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
     * Scan for ecu.test projects.
     *
     * @param testFolder the test folder
     * @param launcher   the launcher
     * @param listener   the listener
     * @return the list of found projects
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private List<String> scanProjects(final String testFolder, final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        List<String> prjFiles = new ArrayList<>();
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

    /**
     * DescriptorImpl for {@link TestFolderBuilder}.
     */
    @Symbol("testFolder")
    @Extension(ordinal = 10000)
    public static final class DescriptorImpl extends AbstractTestDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(TestFolderBuilder.class);
            load();
        }

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
         * @param value the test folder
         * @return the form validation
         */
        @Override
        public FormValidation doCheckTestFile(@QueryParameter final String value) {
            return testValidator.validateTestFolder(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.TestFolderBuilder_DisplayName();
        }
    }
}
