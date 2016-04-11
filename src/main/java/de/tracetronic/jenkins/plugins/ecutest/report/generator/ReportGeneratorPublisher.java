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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tools.ToolInstallation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

/**
 * Publisher providing links to saved {@link GeneratorReport}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorPublisher extends AbstractReportPublisher {

    /**
     * The URL name to {@link GeneratorReport}s holding by {@link AbstractReportGeneratorAction}.
     */
    protected static final String URL_NAME = "generator-reports";

    private final String toolName;
    private final List<ReportGeneratorConfig> generators;
    private final List<ReportGeneratorConfig> customGenerators;

    /**
     * Instantiates a new {@link ReportGeneratorPublisher}.
     *
     * @param toolName
     *            the tool name
     * @param generators
     *            the report generators
     * @param customGenerators
     *            the custom generators
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     * @param archiving
     *            specifies whether archiving artifacts is enabled
     * @param keepAll
     *            specifies whether artifacts are archived for all successful builds,
     *            otherwise only the most recent
     */
    @DataBoundConstructor
    public ReportGeneratorPublisher(final String toolName, final List<ReportGeneratorConfig> generators,
            final List<ReportGeneratorConfig> customGenerators, final boolean allowMissing, final boolean runOnFailed,
            final boolean archiving, final boolean keepAll) {
        super(allowMissing, runOnFailed, archiving, keepAll);
        this.toolName = toolName;
        this.generators = generators == null ? new ArrayList<ReportGeneratorConfig>()
                : removeEmptyGenerators(generators);
        this.customGenerators = customGenerators == null ? new ArrayList<ReportGeneratorConfig>()
                : removeEmptyGenerators(customGenerators);
    }

    /**
     * Instantiates a new {@link ReportGeneratorPublisher}.
     *
     * @param toolName
     *            the tool name
     * @param generators
     *            the report generators
     * @param customGenerators
     *            the custom generators
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     * @deprecated since 1.9, use
     *             {@link #ReportGeneratorPublisher(String, List, List, boolean, boolean, boolean, boolean)}
     */
    @Deprecated
    public ReportGeneratorPublisher(final String toolName, final List<ReportGeneratorConfig> generators,
            final List<ReportGeneratorConfig> customGenerators, final boolean allowMissing, final boolean runOnFailed) {
        this(toolName, generators, customGenerators, allowMissing, runOnFailed, true, true);
    }

    /**
     * Convert legacy configuration into the new class structure.
     *
     * @return an instance of this class with all the new fields transferred from the old structure to the new one
     */
    public final Object readResolve() {
        return new ReportGeneratorPublisher(toolName, generators, customGenerators, isAllowMissing(), isRunOnFailed(),
                isArchiving() == null ? true : isArchiving(), isKeepAll() == null ? true : isKeepAll());
    }

    /**
     * @return the toolName
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the reportGenerators
     */
    public List<ReportGeneratorConfig> getGenerators() {
        return generators;
    }

    /**
     * @return the customGenerators
     */
    public List<ReportGeneratorConfig> getCustomGenerators() {
        return customGenerators;
    }

    /**
     * Removes empty report generators.
     *
     * @param generators
     *            the generators
     * @return the list of valid generators
     */
    private static List<ReportGeneratorConfig> removeEmptyGenerators(final List<ReportGeneratorConfig> generators) {
        final List<ReportGeneratorConfig> validGenerators = new ArrayList<ReportGeneratorConfig>();
        for (final ReportGeneratorConfig generator : generators) {
            if (StringUtils.isNotBlank(generator.getName())) {
                validGenerators.add(generator);
            }
        }
        return validGenerators;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param envVars
     *            the environment variables
     * @return the tool installation
     */
    @CheckForNull
    public AbstractToolInstallation getToolInstallation(final EnvVars envVars) {
        final String expToolName = envVars.expand(toolName);
        for (final AbstractToolInstallation installation : getDescriptor().getInstallations()) {
            if (StringUtils.equals(expToolName, installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        // Check OS running this build
        if (!ProcessUtil.checkOS(launcher, listener)) {
            return false;
        }

        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing generator reports...");

        final Result buildResult = build.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            logger.logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return true;
        }

        final List<FilePath> reportFiles = getReportFiles(build, launcher);
        if (reportFiles.isEmpty() && !isAllowMissing()) {
            logger.logError("Empty test results are not allowed, setting build status to FAILURE!");
            return false;
        }

        final List<GeneratorReport> reports = new ArrayList<GeneratorReport>();
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, false);
        final boolean isETRunning = !foundProcesses.isEmpty();

        // Start ECU-TEST if necessary
        if (isETRunning) {
            reports.addAll(generateReports(reportFiles, build, launcher, listener));
        } else {
            // Get selected ECU-TEST installation
            final AbstractToolInstallation installation = configureToolInstallation(toolName, listener,
                    build.getEnvironment(listener));
            if (installation instanceof ETInstallation) {
                final String installPath = installation.getExecutable(launcher);
                final String workspaceDir = getWorkspaceDir(build);
                final String settingsDir = getSettingsDir(build);
                final String expandedToolName = build.getEnvironment(listener).expand(installation.getName());
                final ETClient etClient = new ETClient(expandedToolName, installPath, workspaceDir, settingsDir,
                        StartETBuilder.DEFAULT_TIMEOUT, false);
                logger.logInfo(String.format("Starting %s...", toolName));
                if (etClient.start(false, launcher, listener)) {
                    logger.logInfo(String.format("%s started successfully.", toolName));
                    reports.addAll(generateReports(reportFiles, build, launcher, listener));
                } else {
                    logger.logError(String.format("Starting %s failed.", toolName));
                }
                logger.logInfo(String.format("Stopping %s...", toolName));
                if (etClient.stop(true, launcher, listener)) {
                    logger.logInfo(String.format("%s stopped successfully.", toolName));
                } else {
                    logger.logError(String.format("Stopping %s failed.", toolName));
                }
            } else {
                logger.logError("Selected ECU-TEST installation is not configured for this node!");
                return false;
            }
        }

        if (isArchiving()) {
            addBuildAction(build, reports);
        } else {
            logger.logInfo("Archiving TRF reports is disabled.");
        }

        logger.logInfo("Generator reports published successfully.");
        return true;
    }

    /**
     * Generates the reports with the configured report generators.
     *
     * @param reportFiles
     *            the report files
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return the list of generated reports
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws InterruptedException
     *             the interrupted exception
     */
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private List<GeneratorReport> generateReports(final List<FilePath> reportFiles, final AbstractBuild<?, ?> build,
            final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<GeneratorReport> reports = new ArrayList<GeneratorReport>();
        final FilePath archiveTarget = getArchiveTarget(build);
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();
        generators.addAll(getGenerators());
        generators.addAll(getCustomGenerators());

        // Removing old artifacts at project level
        if (!reportFiles.isEmpty() && !isKeepAll()) {
            archiveTarget.deleteRecursive();
            removePreviousReports(build, ReportGeneratorBuildAction.class);
        }

        // Generate reports with all generators
        int index = 0;
        for (final ReportGeneratorConfig config : generators) {
            final EnvVars envVars = build.getEnvironment(listener);
            final ReportGeneratorConfig expConfig = config.expand(envVars);
            final ReportGenerator generator = new ReportGenerator(expConfig);
            final boolean isGenerated = generator.generate(reportFiles, launcher, listener);
            if (isArchiving() && isGenerated && !reportFiles.isEmpty()) {
                // Archive generated reports
                logger.logInfo("- Archiving generated reports...");
                final String templateName = expConfig.getName();
                final FilePath archiveTargetDir = archiveTarget.child(templateName);
                for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
                    final FilePath testReportDir = new FilePath(launcher.getChannel(),
                            testEnvAction.getTestReportDir());
                    try {
                        final int copiedFiles = testReportDir.copyRecursiveTo(String.format("**/%s/**", templateName),
                                archiveTargetDir.child(testReportDir.getName()));
                        logger.logInfo(String.format("-> Archived %d report file(s) for %s.", copiedFiles,
                                testEnvAction.getTestName()));
                    } catch (final IOException e) {
                        Util.displayIOException(e, listener);
                        logger.logError("Failed archiving generated reports.");
                    }
                }

                // Collect reports
                if (archiveTargetDir.exists()) {
                    final GeneratorReport report = new GeneratorReport(String.format("%d", ++index), templateName,
                            templateName, getFileSize(archiveTargetDir));
                    reports.add(report);
                    for (final FilePath testReportDir : archiveTargetDir.listDirectories()) {
                        final GeneratorReport subReport = new GeneratorReport(String.format("%d", ++index),
                                testReportDir.getBaseName(), String.format("%s/%s", templateName,
                                        testReportDir.getBaseName()), getFileSize(testReportDir));
                        report.addSubReport(subReport);
                    }
                }
            }
        }

        return reports;
    }

    /**
     * Gets the total size of given directory recursively.
     *
     * @param directory
     *            the directory
     * @return the file size
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    private long getFileSize(final FilePath directory) throws IOException, InterruptedException {
        long size = 0;
        final FilePath[] files = directory.list("**");
        for (final FilePath file : files) {
            size += file.length();
        }
        return size;
    }

    /**
     * Adds the {@link ReportGeneratorBuildAction} to the build holding the found {@link GeneratorReport}s.
     *
     * @param build
     *            the build
     * @param reports
     *            the list of {@link GeneratorReport}s to add
     */
    private void addBuildAction(final AbstractBuild<?, ?> build, final List<GeneratorReport> reports) {
        ReportGeneratorBuildAction action = build.getAction(ReportGeneratorBuildAction.class);
        if (action == null) {
            action = new ReportGeneratorBuildAction(!isKeepAll());
            build.addAction(action);
        }
        action.addAll(reports);
    }

    @Override
    protected String getUrlName() {
        return URL_NAME;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new ReportGeneratorProjectAction(!isKeepAll());
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * DescriptorImpl for {@link ReportGeneratorPublisher}.
     */
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @CopyOnWrite
        private ETInstallation[] installations = new ETInstallation[0];

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
            load();
        }

        /**
         * Gets the tool descriptor.
         *
         * @return the tool descriptor
         */
        public ETInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(ETInstallation.DescriptorImpl.class);
        }

        /**
         * @return the list of ECU-TEST installations
         */
        public ETInstallation[] getInstallations() {
            return installations.clone();
        }

        /**
         * Sets the installations.
         *
         * @param installations
         *            the new installations
         */
        public void setInstallations(final ETInstallation... installations) {
            this.installations = installations;
            save();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ReportGeneratorPublisher_DisplayName();
        }
    }
}
