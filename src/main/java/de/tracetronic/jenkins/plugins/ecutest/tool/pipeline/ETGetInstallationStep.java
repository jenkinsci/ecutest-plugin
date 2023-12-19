/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import com.google.common.collect.ImmutableSet;
import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Set;

/**
 * Advanced pipeline step that returns a pre-configured {@link ETInstance} instance by name.
 */
public class ETGetInstallationStep extends Step {

    private final String toolName;

    /**
     * Instantiates a new {@link ETGetInstallationStep}.
     *
     * @param toolName the tool name
     */
    @DataBoundConstructor
    public ETGetInstallationStep(final String toolName) {
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
     * Synchronous pipeline step execution that returns a pre-configured {@link ETInstance} instance by name.
     */
    private static class Execution extends SynchronousStepExecution<ETInstance> {

        private static final long serialVersionUID = 1L;

        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private final transient ETGetInstallationStep step;

        /**
         * Instantiates a new {@link Execution}.
         *
         * @param step    the step
         * @param context the context
         */
        Execution(final ETGetInstallationStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ETInstance run() throws Exception {
            final ETInstallation installation = ETInstallation.get(step.toolName);
            if (installation == null) {
                final TaskListener listener = getContext().get(TaskListener.class);
                final String message = String.format("ecu.test installation with name '%s' is not configured!",
                    step.toolName);
                throw new ETPluginException(message, listener);
            }
            return new ETInstance(installation);
        }
    }

    /**
     * DescriptorImpl for {@link ETGetInstallationStep}.
     */
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "getETInstallation";
        }

        @Override
        public String getDisplayName() {
            return "Get ecu.test installation by name";
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
