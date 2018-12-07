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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.remoting.Callable;

import java.io.IOException;
import java.util.Set;

import jenkins.security.MasterToSlaveCallable;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.ImmutableSet;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;

/**
 * Advanced pipeline step that publishes ATX reports to given {@link ATXInstallation} instance.
 * 
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXPublishStep extends Step {

    private final ATXInstallation installation;
    private final boolean allowMissing;
    private final boolean runOnFailed;
    private final boolean archiving;
    private final boolean keepAll;

    /**
     * Instantiates a new {@link ATXPublishStep}.
     *
     * @param installation
     *            the ATX installation
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
    public ATXPublishStep(final ATXInstallation installation,
            final boolean allowMissing, final boolean runOnFailed,
            final boolean archiving, final boolean keepAll) {
        this.installation = installation;
        this.allowMissing = allowMissing;
        this.runOnFailed = runOnFailed;
        this.archiving = archiving;
        this.keepAll = keepAll;
    }

    /**
     * Returns whether missing reports are allowed.
     *
     * @return {@code true} if missing reports are allowed, {@code false} otherwise
     */
    public boolean isAllowMissing() {
        return allowMissing;
    }

    /**
     * Returns whether this publisher can run for failed builds, too.
     *
     * @return {@code true} if this publisher can run for failed builds, {@code false} otherwise
     */
    public boolean isRunOnFailed() {
        return runOnFailed;
    }

    /**
     * Returns whether archiving artifacts is enabled.
     *
     * @return {@code true} if archiving artifacts is enabled, {@code false} otherwise
     */
    public boolean isArchiving() {
        return archiving;
    }

    /**
     * Returns whether artifacts are archived for all successful builds, otherwise only the most recent.
     *
     * @return {@code true} if artifacts should be archived for all successful builds, {@code false} otherwise
     */
    public boolean isKeepAll() {
        return keepAll;
    }

    @Override
    public StepExecution start(final StepContext context) throws Exception {
        return new Execution(this, context);
    }

    /**
     * Asynchronous pipeline step execution that publishes ATX reports to given {@link ATXInstallation} instance.
     */
    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        private final transient ATXPublishStep step;

        /**
         * Instantiates a new {@link Execution}.
         *
         * @param step
         *            the step
         * @param context
         *            the context
         */
        Execution(final ATXPublishStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            final Run<?, ?> run = getContext().get(Run.class);
            final FilePath workspace = getContext().get(FilePath.class);
            final Launcher launcher = getContext().get(Launcher.class);
            final TaskListener listener = getContext().get(TaskListener.class);
            return launcher.getChannel().call(
                    new ExecutionCallable(step.installation,
                            step.allowMissing, step.runOnFailed, step.archiving, step.keepAll,
                            run, workspace, launcher, listener));
        }
    }

    /**
     * {@link Callable} publishing ATX reports remotely.
     */
    private static final class ExecutionCallable extends MasterToSlaveCallable<Void, Exception> {

        private static final long serialVersionUID = 1L;

        private final ATXInstallation installation;
        private final boolean allowMissing;
        private final boolean runOnFailed;
        private final boolean archiving;
        private final boolean keepAll;
        private final Run<?, ?> run;
        private final FilePath workspace;
        private final Launcher launcher;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ExecutionCallable}.
         *
         * @param installation
         *            the ATX installation
         * @param allowMissing
         *            specifies whether missing reports are allowed
         * @param runOnFailed
         *            specifies whether this publisher even runs on a failed build
         * @param archiving
         *            specifies whether archiving artifacts is enabled
         * @param keepAll
         *            specifies whether artifacts are archived for all successful builds,
         *            otherwise only the most recent
         * @param run
         *            the run
         * @param workspace
         *            the workspace
         * @param launcher
         *            the launcher
         * @param listener
         *            the listener
         */
        ExecutionCallable(final ATXInstallation installation,
                final boolean allowMissing, final boolean runOnFailed,
                final boolean archiving, final boolean keepAll,
                final Run<?, ?> run, final FilePath workspace,
                final Launcher launcher, final TaskListener listener) {
            super();
            this.installation = installation;
            this.allowMissing = allowMissing;
            this.runOnFailed = runOnFailed;
            this.archiving = archiving;
            this.keepAll = keepAll;
            this.run = run;
            this.workspace = workspace;
            this.launcher = launcher;
            this.listener = listener;
        }

        @Override
        public Void call() throws IOException, InterruptedException, ETPluginException {
            final ATXPublisher publisher = new ATXPublisher(installation);
            publisher.setAllowMissing(allowMissing);
            publisher.setRunOnFailed(runOnFailed);
            publisher.setArchiving(archiving);
            publisher.setKeepAll(keepAll);
            publisher.perform(run, workspace, launcher, listener);
            return null;
        }
    }

    /**
     * DescriptorImpl for {@link ATXPublishStep}.
     */
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "publishATXReports";
        }

        @Override
        public String getDisplayName() {
            return "Publish ATX reports";
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }
    }
}
