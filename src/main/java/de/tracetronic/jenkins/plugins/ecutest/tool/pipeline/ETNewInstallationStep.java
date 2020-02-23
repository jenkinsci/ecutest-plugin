/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETToolProperty;
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
 * Advanced pipeline step that returns a new {@link ETInstance} instance.
 */
public class ETNewInstallationStep extends Step {

    @Nonnull
    private final String toolName;
    @Nonnull
    private final String installPath;
    @CheckForNull
    private final ETToolProperty property;

    /**
     * Instantiates a new {@link ETNewInstallationStep}.
     *
     * @param toolName    the tool name
     * @param installPath the install path
     * @param property    the tool property
     */
    @DataBoundConstructor
    public ETNewInstallationStep(@Nonnull final String toolName,
                                 @Nonnull final String installPath,
                                 final ETToolProperty property) {
        this.toolName = toolName;
        this.installPath = installPath;
        this.property = property != null ? property : new ETToolProperty(null, 0, false);
    }

    @Nonnull
    public String getToolName() {
        return toolName;
    }

    @Nonnull
    public String getInstallPath() {
        return installPath;
    }

    @CheckForNull
    public ETToolProperty getProperty() {
        return property;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(this, context);
    }

    /**
     * Synchronous pipeline step execution that returns a new {@link ETInstance} instance.
     */
    private static class Execution extends SynchronousStepExecution<ETInstance> {

        private static final long serialVersionUID = 1L;

        private final transient ETNewInstallationStep step;

        /**
         * Instantiates a new {@link Execution}.
         *
         * @param step    the step
         * @param context the context
         */
        Execution(final ETNewInstallationStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected ETInstance run() {
            final ETInstallation installation = new ETInstallation(step.toolName, step.installPath,
                Collections.singletonList(step.property));
            return new ETInstance(installation);
        }
    }

    /**
     * DescriptorImpl for {@link ETNewInstallationStep}.
     */
    @Extension
    public static final class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "newETInstallation";
        }

        @Override
        public String getDisplayName() {
            return "Return new ECU-TEST installation";
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
