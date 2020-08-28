/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class holding the downstream configuration.
 */
public class DownStreamPublisher extends Recorder implements SimpleBuildStep {

    @Nonnull
    private final String workspace;
    @Nonnull
    private List<AbstractReportPublisher> publishers = new ArrayList<>();
    @Nonnull
    protected static final String DEFAULT_REPORT_DIR = "TestReports";

    /**
     * Custom report folder name or path.
     *
     * @since 2.19
     */
    private final String reportDir;

    /**
     * Instantiates a new {@link DownStreamPublisher}.
     *
     * @param workspace the downstream workspace
     * @param reportDir the report directory
     */
    @DataBoundConstructor
    public DownStreamPublisher(final String workspace, final String reportDir) {
        super();
        this.workspace = StringUtils.trimToEmpty(workspace);
        this.reportDir = StringUtils.defaultString(reportDir, getDefaultReportDir());
    }

    @Nonnull
    public String getWorkspace() {
        return workspace;
    }

    @Nonnull
    public List<AbstractReportPublisher> getPublishers() {
        return publishers;
    }

    public static String getDefaultReportDir() {
        return DEFAULT_REPORT_DIR;
    }

    public String getReportDir() {
        return reportDir;
    }

    @DataBoundSetter
    public void setPublishers(final List<AbstractReportPublisher> publishers) {
        this.publishers = publishers == null ? new ArrayList<>() : publishers;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing downstream reports...");
        for (final AbstractReportPublisher publisher : getPublishers()) {
            if (publisher != null) {
                publisher.setDownstream(true);
                publisher.setWorkspace(getWorkspace());
                publisher.setReportDir(getReportDir());
                publisher.perform(run, workspace, launcher, listener);
            }
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public AbstractReportDescriptor getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * DescriptorImpl for {@link DownStreamPublisher}.
     */
    @Symbol("downstreamPublisher")
    @Extension(ordinal = 10000)
    public static final class DescriptorImpl extends AbstractReportDescriptor {

        /**
         * Validator to check form fields.
         */
        private final TestValidator testValidator = new TestValidator();

        /**
         * Gets the applicable publishers.
         *
         * @return the applicable publishers
         */
        public List<Descriptor<? extends Publisher>> getApplicablePublishers() {
            final List<Descriptor<? extends Publisher>> list = new ArrayList<>();
            final DescriptorExtensionList<Publisher, Descriptor<Publisher>> publishers = AbstractReportPublisher.all();
            if (publishers != null) {
                for (final Descriptor<Publisher> publisher : publishers) {
                    if (publisher instanceof AbstractReportDescriptor
                            && !(publisher instanceof DownStreamPublisher.DescriptorImpl)) {
                        list.add(publisher);
                    }
                }
            }
            return list;
        }

        public static String getDefaultReportDir() {
            return DEFAULT_REPORT_DIR;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.DownStreamPublisher_DisplayName();
        }
    }
}
