/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import com.google.common.collect.ImmutableSet;
import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Advanced pipeline step that returns a pre-configured {@link ATXServer} instance by name.
 */
public class ATXGetServerStep extends Step {

    @Nonnull
    private final String atxName;

    /**
     * Instantiates a new {@link ATXGetServerStep}.
     *
     * @param atxName the ATX name
     */
    @DataBoundConstructor
    public ATXGetServerStep(@Nonnull final String atxName) {
        super();
        this.atxName = atxName;
    }

    @Nonnull
    public String getAtxName() {
        return atxName;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(this, context);
    }

    /**
     * Synchronous pipeline step execution that returns a pre-configured {@link ATXServer} instance by name.
     */
    private static class Execution extends SynchronousStepExecution<ATXServer> {

        private static final long serialVersionUID = 1L;

        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private final transient ATXGetServerStep step;

        /**
         * Instantiates a new {@link Execution}.
         *
         * @param step    the step
         * @param context the context
         */
        Execution(final ATXGetServerStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ATXServer run() throws Exception {
            final ATXInstallation installation = ATXInstallation.get(step.atxName);
            if (installation == null) {
                final TaskListener listener = getContext().get(TaskListener.class);
                final String message = String.format("test.guide installation with name '%s' is not configured!",
                    step.atxName);
                throw new ETPluginException(message, listener);
            }
            return new ATXServer(ATXInstallation.get(step.atxName));
        }
    }

    /**
     * DescriptorImpl for {@link ATXGetServerStep}.
     */
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "getATXServer";
        }

        @Override
        public String getDisplayName() {
            return "Get test.guide server by name";
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class);
        }
    }
}
