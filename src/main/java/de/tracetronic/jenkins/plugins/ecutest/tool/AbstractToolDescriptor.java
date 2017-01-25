/*
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.tool;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;

/**
 * Common base descriptor class for all test related task build descriptors implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractToolDescriptor extends BuildStepDescriptor<Builder> {

    /**
     * Validator to check form fields.
     */
    protected final transient ToolValidator toolValidator;

    /**
     * Instantiates a new {@link AbstractToolDescriptor}.
     */
    public AbstractToolDescriptor() {
        super();
        toolValidator = new ToolValidator();
    }

    /**
     * Gets the tool installations.
     *
     * @return the installations
     */
    public ETInstallation[] getInstallations() {
        return getToolDescriptor().getInstallations();
    }

    /**
     * Gets the tool descriptor holding the installations.
     *
     * @return the tool descriptor
     */
    public ETInstallation.DescriptorImpl getToolDescriptor() {
        return ToolInstallation.all().get(ETInstallation.DescriptorImpl.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }

    /**
     * @return the default timeout
     */
    public abstract int getDefaultTimeout();

    /**
     * Validates the timeout.
     *
     * @param value
     *            the timeout
     * @return the form validation
     */
    public FormValidation doCheckTimeout(@QueryParameter final String value) {
        return toolValidator.validateTimeout(value, getDefaultTimeout());
    }
}
