/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Advanced pipeline step that returns a new {@link ATXServer} instance.
 */
public class ATXNewServerStep extends Step {

    @Nonnull
    private final String atxName;
    @Nonnull
    private final String toolName;
    @CheckForNull
    private final ATXConfig config;

    /**
     * Instantiates a new {@link ATXNewServerStep}.
     *
     * @param atxName  the ATX name
     * @param toolName the tool name
     * @param config   the ATX configuration
     */
    @DataBoundConstructor
    public ATXNewServerStep(@Nonnull final String atxName, @Nonnull final String toolName, final ATXConfig config) {
        this.atxName = atxName;
        this.toolName = toolName;
        this.config = config != null ? config : new ATXConfig();
    }

    @Nonnull
    public String getAtxName() {
        return atxName;
    }

    @Nonnull
    public String getToolName() {
        return toolName;
    }

    public ATXConfig getConfig() {
        return config;
    }

    @Override
    public StepExecution start(final StepContext context) throws Exception {
        return new Execution(this, context);
    }

    /**
     * Synchronous pipeline step execution that returns a new {@link ATXServer} instance.
     */
    private static class Execution extends SynchronousStepExecution<ATXServer> {

        private static final long serialVersionUID = 1L;

        private final transient ATXNewServerStep step;

        /**
         * Instantiates a new {@link Execution}.
         *
         * @param step    the step
         * @param context the context
         */
        Execution(final ATXNewServerStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ATXServer run() throws Exception {
            final ATXInstallation installation = new ATXInstallation(step.atxName, step.toolName, step.config);
            return new ATXServer(installation);
        }
    }

    /**
     * DescriptorImpl for {@link ATXNewServerStep}.
     */
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "newATXServer";
        }

        @Override
        public String getDisplayName() {
            return "Return new TEST-GUIDE server";
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
