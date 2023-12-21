/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractToolPublisher;
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
 */
public class ReportGeneratorPublisher extends AbstractToolPublisher {

    /**
     * The URL name to {@link GeneratorReport}s holding by {@link AbstractReportGeneratorAction}.
     */
    protected static final String URL_NAME = "generator-reports";

    @Nonnull
    private List<ReportGeneratorConfig> generators = new ArrayList<>();
    @Nonnull
    private List<ReportGeneratorConfig> customGenerators = new ArrayList<>();

    /**
     * Instantiates a new {@link ReportGeneratorPublisher}.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     */
    @DataBoundConstructor
    public ReportGeneratorPublisher(@Nonnull final String toolName) {
        super(toolName);
    }

    /**
     * Removes empty report generators.
     *
     * @param generators the generators
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

    @Nonnull
    public List<ReportGeneratorConfig> getGenerators() {
        return generators;
    }

    @DataBoundSetter
    public void setGenerators(final List<ReportGeneratorConfig> generators) {
        this.generators = generators == null ? new ArrayList<>()
            : removeEmptyGenerators(generators);
    }

    @Nonnull
    public List<ReportGeneratorConfig> getCustomGenerators() {
        return customGenerators;
    }

    @DataBoundSetter
    public void setCustomGenerators(final List<ReportGeneratorConfig> customGenerators) {
        this.customGenerators = customGenerators == null ? new ArrayList<>()
            : removeEmptyGenerators(customGenerators);
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
        if (isETRunning(launcher, listener)) {
            reports.addAll(generateReports(reportFiles, run, workspace, launcher, listener));
        } else {
            final ETClient etClient = getToolClient(run, workspace, launcher, listener);
            if (etClient.start(false, workspace, launcher, listener)) {
                reports.addAll(generateReports(reportFiles, run, workspace, launcher, listener));
            } else {
                logger.logError(String.format("Starting %s failed.", getToolName()));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", getToolName()));
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
     * @param reportFiles the report files
     * @param run         the run
     * @param workspace   the workspace
     * @param launcher    the launcher
     * @param listener    the listener
     * @return the list of generated reports
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private List<GeneratorReport> generateReports(final List<FilePath> reportFiles, final Run<?, ?> run,
                                                  final FilePath workspace, final Launcher launcher,
                                                  final TaskListener listener)
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
                    final GeneratorReport report = new GeneratorReport(randomId(), templateName,
                        templateName, getDirectorySize(archiveTargetDir));
                    reports.add(report);
                    for (final FilePath reportDir : reportDirs) {
                        final GeneratorReport subReport = new GeneratorReport(randomId(),
                            reportDir.getBaseName(), String.format("%s/%s", templateName,
                            reportDir.getBaseName()), getDirectorySize(reportDir));
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
     * @param run     the run
     * @param reports the list of {@link GeneratorReport}s to add
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
