/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import com.google.common.collect.ImmutableSet;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Objects;
import java.util.Set;

/**
 * Advanced pipeline step that checks whether the currently selected configurations are started.
 */
public class ETConfigStartedStep extends Step {

    private final String toolName;

    /**
     * Instantiates a new {@link ETConfigStartedStep}.
     *
     * @param toolName the tool name
     */
    @DataBoundConstructor
    public ETConfigStartedStep(final String toolName) {
        super();
        this.toolName = toolName;
    }

    public String getToolName() {
        return toolName;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(this, context);
    }

    /**
     * Synchronous pipeline step execution that checks whether the currently selected configurations are started.
     */
    private static class Execution extends SynchronousNonBlockingStepExecution<Boolean> {

        private static final long serialVersionUID = 1L;

        private final transient ETConfigStartedStep step;

        /**
         * Instantiates a new {@link Execution}.
         *
         * @param step    the step
         * @param context the context
         */
        Execution(final ETConfigStartedStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Boolean run() throws Exception {
            final ETClient client = new ETClient(step.toolName, ETComProperty.DEFAULT_TIMEOUT);
            return client.checkConfigStatus(Objects.requireNonNull(getContext().get(Launcher.class)),
                getContext().get(TaskListener.class));
        }
    }

    /**
     * DescriptorImpl for {@link ETConfigStartedStep}.
     */
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "isConfigStarted";
        }

        @Override
        public String getDisplayName() {
            return "Check ECU-TEST configuration status";
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Launcher.class, TaskListener.class);
        }
    }
}
