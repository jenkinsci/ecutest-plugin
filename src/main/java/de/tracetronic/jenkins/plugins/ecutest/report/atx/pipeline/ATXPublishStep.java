/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import com.google.common.collect.ImmutableSet;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Set;

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
     * @param installation the ATX installation
     * @param allowMissing specifies whether missing reports are allowed
     * @param runOnFailed  specifies whether this publisher even runs on a failed build
     * @param archiving    specifies whether archiving artifacts is enabled
     * @param keepAll      specifies whether artifacts are archived for all successful builds,
     *                     otherwise only the most recent
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
         * @param step    the step
         * @param context the context
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
        private final transient Run<?, ?> run;
        private final FilePath workspace;
        private final transient Launcher launcher;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link ExecutionCallable}.
         *
         * @param installation the ATX installation
         * @param allowMissing specifies whether missing reports are allowed
         * @param runOnFailed  specifies whether this publisher even runs on a failed build
         * @param archiving    specifies whether archiving artifacts is enabled
         * @param keepAll      specifies whether artifacts are archived for all successful builds,
         *                     otherwise only the most recent
         * @param run          the run
         * @param workspace    the workspace
         * @param launcher     the launcher
         * @param listener     the listener
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
        public Void call() throws IOException, InterruptedException {
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
