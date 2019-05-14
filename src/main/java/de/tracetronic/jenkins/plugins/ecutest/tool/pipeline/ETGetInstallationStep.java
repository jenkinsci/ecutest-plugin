/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collections;
import java.util.Set;

/**
 * Advanced pipeline step that returns a pre-configured {@link ETInstance} instance by name.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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

    /**
     * @return the tool name
     */
    public String getToolName() {
        return toolName;
    }

    @Override
    public StepExecution start(final StepContext context) throws Exception {
        return new Execution(this, context);
    }

    /**
     * Synchronous pipeline step execution that returns a pre-configured {@link ETInstance} instance by name.
     */
    private static class Execution extends SynchronousStepExecution<ETInstance> {

        private static final long serialVersionUID = 1L;

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
            return new ETInstance(ETInstallation.get(step.toolName));
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
            return "Get ECU-TEST installation by name";
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.emptySet();
        }
    }
}
