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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Nonnull
    private final String toolName;
    @Nonnull
    private List<ReportGeneratorConfig> generators = new ArrayList<>();
    @Nonnull
    private List<ReportGeneratorConfig> customGenerators = new ArrayList<>();

    /**
     * Instantiates a new {@link ReportGeneratorPublisher}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public ReportGeneratorPublisher(@Nonnull final String toolName) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
    }

    /**
     * @return the {@link ETInstallation} name
     */
    @Nonnull
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the reportGenerators
     */
    @Nonnull
    public List<ReportGeneratorConfig> getGenerators() {
        return generators;
    }

    /**
     * @return the customGenerators
     */
    @Nonnull
    public List<ReportGeneratorConfig> getCustomGenerators() {
        return customGenerators;
    }

    /**
     * @param generators
     *            the report generators
     */
    @DataBoundSetter
    public void setGenerators(final List<ReportGeneratorConfig> generators) {
        this.generators = generators == null ? new ArrayList<>()
                : removeEmptyGenerators(generators);
    }

    /**
     * @param customGenerators
     *            the custom report generators
     */
    @DataBoundSetter
    public void setCustomGenerators(final List<ReportGeneratorConfig> customGenerators) {
        this.customGenerators = customGenerators == null ? new ArrayList<>()
                : removeEmptyGenerators(customGenerators);
    }

    /**
     * Removes empty report generators.
     *
     * @param generators
     *            the generators
     * @return the list of valid generators
     */
    private static List<ReportGeneratorConfig> removeEmptyGenerators(final List<ReportGeneratorConfig> generators) {
        final List<ReportGeneratorConfig> validGenerators = new ArrayList<>();
        for (final ReportGeneratorConfig generator : generators) {
            if (StringUtils.isNotBlank(generator.getName())) {
                validGenerators.add(generator);
            }
        }
        return validGenerators;
    }

    @Override
    public void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final TTConsoleLogger logger = getLogger();
        logger.logInfo("Publishing generator reports...");

        if (isSkipped(true, run, launcher)) {
            return;
        }

        final List<FilePath> reportFiles = getReportFiles(run, workspace, launcher);
        if (reportFiles.isEmpty() && !isAllowMissing()) {
            throw new ETPluginException("Empty test results are not allowed, setting build status to FAILURE!");
        }

        final List<GeneratorReport> reports = new ArrayList<>();
        if (isETRunning(launcher)) {
            reports.addAll(generateReports(reportFiles, run, workspace, launcher, listener));
        } else {
            final ETClient etClient = getToolClient(toolName, run, workspace, launcher, listener);
            if (etClient.start(false, workspace, launcher, listener)) {
                reports.addAll(generateReports(reportFiles, run, workspace, launcher, listener));
            } else {
                logger.logError(String.format("Starting %s failed.", toolName));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", toolName));
            }
        }

        if (isArchiving()) {
            addBuildAction(run, reports);
        } else {
            logger.logInfo("Archiving TRF reports is disabled.");
        }

        logger.logInfo("Generator reports published successfully.");
    }

    /**
     * Generates the reports with the configured report generators.
     *
     * @param reportFiles
     *            the report files
     * @param run
     *            the run
     * @param workspace
     *            the workspace
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
    private List<GeneratorReport> generateReports(final List<FilePath> reportFiles, final Run<?, ?> run,
            final FilePath workspace, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = getLogger();
        final List<GeneratorReport> reports = new ArrayList<>();
        final FilePath archiveTarget = getArchiveTarget(run);
        final List<ReportGeneratorConfig> generators = new ArrayList<>();
        generators.addAll(getGenerators());
        generators.addAll(getCustomGenerators());

        // Removing old artifacts at project level
        if (!reportFiles.isEmpty() && !isKeepAll()) {
            archiveTarget.deleteRecursive();
            removePreviousReports(run, ReportGeneratorBuildAction.class);
        }

        // Generate reports with all generators
        int index = 0;
        for (final ReportGeneratorConfig config : generators) {
            final EnvVars envVars = run.getEnvironment(listener);
            final ReportGeneratorConfig expConfig = config.expand(envVars);
            final ReportGenerator generator = new ReportGenerator(expConfig);
            final boolean isGenerated = generator.generate(reportFiles, launcher, listener);
            if (isArchiving() && isGenerated && !reportFiles.isEmpty()) {
                // Archive generated reports
                logger.logInfo("- Archiving generated reports...");
                final String templateName = expConfig.getName();
                final FilePath archiveTargetDir = archiveTarget.child(templateName);
                final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
                for (final FilePath reportDir : reportDirs) {
                    try {
                        final int copiedFiles = reportDir.copyRecursiveTo(String.format("**/%s/**", templateName),
                                archiveTargetDir.child(reportDir.getName()));
                        logger.logInfo(String.format("-> Archived %d report file(s).", copiedFiles));
                    } catch (final IOException e) {
                        Util.displayIOException(e, listener);
                        logger.logError("Failed archiving generated reports.");
                    }
                }
                // Collect reports
                if (archiveTargetDir.exists()) {
                    final GeneratorReport report = new GeneratorReport(String.format("%d", ++index), templateName,
                            templateName, getDirectorySize(archiveTargetDir));
                    reports.add(report);
                    for (final FilePath testReportDir : archiveTargetDir.listDirectories()) {
                        final GeneratorReport subReport = new GeneratorReport(String.format("%d", ++index),
                                testReportDir.getBaseName(), String.format("%s/%s", templateName,
                                        testReportDir.getBaseName()), getDirectorySize(testReportDir));
                        report.addSubReport(subReport);
                    }
                }
            }
        }

        return reports;
    }

    /**
     * Adds the {@link ReportGeneratorBuildAction} to the build holding the found {@link GeneratorReport}s.
     *
     * @param run
     *            the run
     * @param reports
     *            the list of {@link GeneratorReport}s to add
     */
    private void addBuildAction(final Run<?, ?> run, final List<GeneratorReport> reports) {
        ReportGeneratorBuildAction action = run.getAction(ReportGeneratorBuildAction.class);
        if (action == null) {
            action = new ReportGeneratorBuildAction(!isKeepAll());
            run.addAction(action);
        }
        action.addAll(reports);
    }

    @Override
    protected String getUrlName() {
        return URL_NAME;
    }

    /**
     * DescriptorImpl for {@link ReportGeneratorPublisher}.
     */
    @Symbol("publishGenerators")
    @Extension(ordinal = 10004)
    public static class DescriptorImpl extends AbstractReportDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ReportGeneratorPublisher_DisplayName();
        }
    }
}
