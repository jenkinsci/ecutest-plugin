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

import java.util.Collections;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;

/**
 * Advanced pipeline step that returns a new {@link ATXServer} instance.
 * 
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
     * @param atxName
     *            the ATX name
     * @param toolName
     *            the tool name
     * @param config
     *            the ATX configuration
     */
    @DataBoundConstructor
    public ATXNewServerStep(final String atxName, final String toolName, final ATXConfig config) {
        this.atxName = atxName;
        this.toolName = toolName;
        this.config = config != null ? config : new ATXConfig();
    }

    /**
     * @return the ATX name
     */
    public String getAtxName() {
        return atxName;
    }

    /**
     * @return the tool name
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the ATX configuration
     */
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
         * @param step
         *            the step
         * @param context
         *            the context
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
            return "Return new TEST-GUIDE installation";
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
