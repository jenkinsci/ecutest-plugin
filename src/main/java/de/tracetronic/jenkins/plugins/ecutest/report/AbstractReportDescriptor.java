/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tools.ToolInstallation;

/**
 * Common base descriptor class for all report related publisher descriptors implemented in this plugin.
 */
public abstract class AbstractReportDescriptor extends BuildStepDescriptor<Publisher> {

    /**
     * Gets the tool installations.
     *
     * @return the installations
     */
    public ETInstallation[] getToolInstallations() {
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
}
