/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

/**
 * Common base descriptor class for all test related task build descriptors implemented in this plugin.
 */
public abstract class AbstractTestDescriptor extends BuildStepDescriptor<Builder> {

    /**
     * Validator to check form fields.
     */
    protected final transient TestValidator testValidator;

    /**
     * Instantiates a {@link AbstractTestDescriptor}.
     *
     * @param clazz the {@link AbstractTestBuilder} class name
     */
    public AbstractTestDescriptor(final Class<? extends AbstractTestBuilder> clazz) {
        super(clazz);
        testValidator = new TestValidator();
    }

    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }

    /**
     * Validates the test file.
     *
     * @param value the test file
     * @return the form validation
     */
    public abstract FormValidation doCheckTestFile(@QueryParameter String value);

    @Override
    @SuppressWarnings("deprecation")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
        justification = "False positive")
    public Builder newInstance(final StaplerRequest req, @Nonnull final JSONObject json) {
        final JSONObject testConfig = json.optJSONObject("testConfig");
        if (testConfig != null) {
            // Flip value due to inverted UI behavior
            final boolean keepConfig = testConfig.optBoolean("keepConfig");
            testConfig.put("keepConfig", !keepConfig);
        }
        return req.bindJSON(clazz, json);
    }
}
