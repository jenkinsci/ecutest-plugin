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
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;

/**
 * Publisher providing links to saved {@link TRFReport}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TRFPublisher extends AbstractReportPublisher {

    /**
     * File name of the TRF file.
     */
    private static final String TRF_FILE_NAME = "report.trf";

    /**
     * The URL name to {@link TRFReport}s holding by {@link AbstractTRFAction}.
     */
    protected static final String URL_NAME = "trf-reports";

    /**
     * Instantiates a new {@link TRFPublisher}.
     *
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     */
    @DataBoundConstructor
    public TRFPublisher(final boolean allowMissing, final boolean runOnFailed) {
        super(allowMissing, runOnFailed);
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing TRF reports...");

        final Result buildResult = build.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            logger.logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return true;
        }

        final List<TRFReport> trfReports = new ArrayList<TRFReport>();
        final List<FilePath> archiveFiles = getArchiveFiles(build, launcher);
        final FilePath archiveTargetDir = getArchiveTarget(build);

        for (final FilePath archiveFile : archiveFiles) {
            final String relArchivePath = archiveFile.getParent().getName() + File.separator
                    + archiveFile.getName();
            final FilePath targetFile = archiveTargetDir.child(relArchivePath);

            try {
                if (archiveFile.exists()) {
                    logger.logInfo(String.format("- Archiving %s", archiveFile));
                    archiveFile.copyTo(targetFile);
                } else {
                    if (isAllowMissing()) {
                        continue;
                    } else {
                        logger.logError(String.format("Specified TRF file '%s' does not exist.", archiveFile));
                        build.setResult(Result.FAILURE);
                        return true;
                    }
                }
            } catch (final IOException e) {
                Util.displayIOException(e, listener);
                logger.logError("Failed publishing TRF reports.");
                build.setResult(Result.FAILURE);
                return true;
            }

            trfReports.add(new TRFReport(String.format("%d", trfReports.size() + 1), archiveFile.getParent()
                    .getName(), relArchivePath, archiveFile.length()));
        }

        if (trfReports.isEmpty() && !isAllowMissing()) {
            logger.logError("Empty test results are not allowed, setting build status to FAILURE!");
            build.setResult(Result.FAILURE);
            return true;
        }

        // Add action for publishing TRF reports
        TRFBuildAction action = build.getAction(TRFBuildAction.class);
        if (action == null) {
            action = new TRFBuildAction();
            build.addAction(action);
        }
        action.addAll(trfReports);

        logger.logInfo("TRF reports published successfully.");
        return true;
    }

    /**
     * Gets the archive target.
     *
     * @param build
     *            the build
     * @return the archive target
     */
    private FilePath getArchiveTarget(final AbstractBuild<?, ?> build) {
        return new FilePath(new File(build.getRootDir(), URL_NAME));
    }

    /**
     * Builds a list of TRF files for archiving.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @return the list of TRF files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private List<FilePath> getArchiveFiles(final AbstractBuild<?, ?> build, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> archiveFiles = new ArrayList<FilePath>();
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath trfFile = new FilePath(launcher.getChannel(), new File(
                    testEnvAction.getTestReportDir(), TRF_FILE_NAME).getPath());
            archiveFiles.add(trfFile);
        }
        return archiveFiles;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new TRFProjectAction();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * DescriptorImpl for {@link TRFPublisher}.
     */
    @Extension(ordinal = 1002)
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.TRFPublisher_DisplayName();
        }
    }
}
