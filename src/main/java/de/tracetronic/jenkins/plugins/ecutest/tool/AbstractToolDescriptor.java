/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

/**
 * Common base descriptor class for all test related task build descriptors implemented in this plugin.
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
     * Gets the default timeout.
     *
     * @return the default timeout
     */
    public abstract int getDefaultTimeout();

    /**
     * Validates the timeout.
     *
     * @param value the timeout
     * @return the form validation
     */
    public FormValidation doCheckTimeout(@QueryParameter final String value) {
        return toolValidator.validateTimeout(value, getDefaultTimeout());
    }
}
