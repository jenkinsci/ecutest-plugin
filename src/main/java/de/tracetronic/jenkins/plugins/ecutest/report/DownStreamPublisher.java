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
package de.tracetronic.jenkins.plugins.ecutest.report;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;

/**
 * Class holding the downstream configuration.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class DownStreamPublisher extends Recorder implements SimpleBuildStep {

    @Nonnull
    private final String workspace;
    @Nonnull
    private List<AbstractReportPublisher> publishers = new ArrayList<AbstractReportPublisher>();

    /**
     * Instantiates a new {@link DownStreamPublisher}.
     *
     * @param workspace
     *            the downstream workspace
     */
    @DataBoundConstructor
    public DownStreamPublisher(final String workspace) {
        super();
        this.workspace = StringUtils.trimToEmpty(workspace);
    }

    /**
     * @return the downstream workspace
     */
    @Nonnull
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @return the configured publishers
     */
    public List<AbstractReportPublisher> getPublishers() {
        return publishers;
    }

    /**
     * @param publishers
     *            the report generators
     */
    @DataBoundSetter
    public void setPublishers(final List<AbstractReportPublisher> publishers) {
        this.publishers = publishers == null ? new ArrayList<AbstractReportPublisher>() : publishers;
    }

    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
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
                    if (publisher instanceof AbstractReportDescriptor &&
                            !(publisher instanceof DownStreamPublisher.DescriptorImpl)) {
                        list.add(publisher);
                    }
                }
            }
            return list;
        }

        @Override
        public String getDisplayName() {
            return Messages.DownStreamPublisher_DisplayName();
        }
    }
}
