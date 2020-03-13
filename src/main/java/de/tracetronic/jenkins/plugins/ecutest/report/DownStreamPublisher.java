/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
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
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

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

    /**
     * Instantiates a new {@link DownStreamPublisher}.
     *
     * @param workspace the downstream workspace
     */
    @DataBoundConstructor
    public DownStreamPublisher(final String workspace) {
        super();
        this.workspace = StringUtils.trimToEmpty(workspace);
    }

    @Nonnull
    public String getWorkspace() {
        return workspace;
    }

    @Nonnull
    public List<AbstractReportPublisher> getPublishers() {
        return publishers;
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.DownStreamPublisher_DisplayName();
        }
    }
}
