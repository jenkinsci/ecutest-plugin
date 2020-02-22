/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class holding the project configuration.
 */
public class ProjectConfig extends AbstractDescribableImpl<ProjectConfig> implements Serializable,
    ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final boolean execInCurrentPkgDir;
    private final String filterExpression;
    /**
     * Holds the analysis job execution mode.<br/>
     * Backward compatibility will be retained by {@code readResolve()}.
     *
     * @since 1.2
     */
    private final JobExecutionMode jobExecMode;

    /**
     * Instantiates a new {@link ProjectConfig}.
     *
     * @param execInCurrentPkgDir specifies whether to search the references in the current package directory
     * @param filterExpression    the filter expression to filter the package and project references
     * @param jobExecMode         the analysis job execution mode
     */
    @DataBoundConstructor
    public ProjectConfig(final boolean execInCurrentPkgDir, final String filterExpression,
                         final JobExecutionMode jobExecMode) {
        super();
        this.execInCurrentPkgDir = execInCurrentPkgDir;
        this.filterExpression = StringUtils.defaultIfBlank(filterExpression, "");
        this.jobExecMode = jobExecMode;
    }

    /**
     * Instantiates a new {@link ProjectConfig} with default values.
     *
     * @return the default {@link ProjectConfig}
     */
    public static ProjectConfig newInstance() {
        return new ProjectConfig(false, null, JobExecutionMode.SEQUENTIAL_EXECUTION);
    }

    public boolean isExecInCurrentPkgDir() {
        return execInCurrentPkgDir;
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public JobExecutionMode getJobExecMode() {
        return jobExecMode;
    }

    @Override
    public ProjectConfig expand(final EnvVars envVars) {
        final String expFilterExpression = envVars.expand(getFilterExpression());
        return new ProjectConfig(isExecInCurrentPkgDir(), expFilterExpression, getJobExecMode());
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ProjectConfig) {
            final ProjectConfig that = (ProjectConfig) other;
            result = Objects.equals(filterExpression, that.filterExpression)
                && execInCurrentPkgDir == that.execInCurrentPkgDir
                && jobExecMode == that.jobExecMode;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(execInCurrentPkgDir).append(filterExpression)
            .append(jobExecMode).toHashCode();
    }

    /**
     * Defines the analysis job execution modes.
     *
     * @since 1.2
     */
    public enum JobExecutionMode {

        /**
         * No analysis job execution.
         */
        NO_EXECUTION(0),

        /**
         * Sequential analysis job execution (default).
         */
        SEQUENTIAL_EXECUTION(1),

        /**
         * Parallel analysis job execution.
         */
        PARALLEL_EXECUTION(2),

        /**
         * Sequential analysis job execution with separate test report.
         */
        SEPARATE_SEQUENTIAL_EXECUTION(5),

        /**
         * Parallel analysis job execution with separate test report.
         */
        SEPARATE_PARALLEL_EXECUTION(6),

        /**
         * Analysis job execution without running the test case part.
         */
        NO_TESTCASE_EXECUTION(9);

        private final int value;

        /**
         * Instantiates a new {@link JobExecutionMode} by value.
         *
         * @param value the value
         */
        JobExecutionMode(final int value) {
            this.value = value;
        }

        /**
         * Gets the job execution mode by Integer value.
         *
         * @param value the value
         * @return the related {@code JobExecutionMode}
         */
        public static JobExecutionMode fromValue(final Integer value) {
            JobExecutionMode execMode = JobExecutionMode.SEQUENTIAL_EXECUTION;
            for (final JobExecutionMode mode : JobExecutionMode.values()) {
                if (mode.getValue() == value) {
                    execMode = mode;
                    break;
                }
            }
            return execMode;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * DescriptorImpl for {@link ProjectConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ProjectConfig> {

        private final TestValidator testValidator = new TestValidator();

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Project Configuration";
        }

        public JobExecutionMode getDefaultJobExecMode() {
            return JobExecutionMode.SEQUENTIAL_EXECUTION;
        }

        /**
         * Fills the jobExecutionMode drop-down menu.
         *
         * @return the jobExecutionMode items
         */
        public ListBoxModel doFillJobExecModeItems() {
            final ListBoxModel items = new ListBoxModel();
            items.add(Messages.TestProjectBuilder_JobExecutionMode_0(),
                JobExecutionMode.NO_EXECUTION.toString());
            items.add(Messages.TestProjectBuilder_JobExecutionMode_1(),
                JobExecutionMode.SEQUENTIAL_EXECUTION.toString());
            items.add(Messages.TestProjectBuilder_JobExecutionMode_2(),
                JobExecutionMode.PARALLEL_EXECUTION.toString());
            items.add(Messages.TestProjectBuilder_JobExecutionMode_5(),
                JobExecutionMode.SEPARATE_SEQUENTIAL_EXECUTION.toString());
            items.add(Messages.TestProjectBuilder_JobExecutionMode_6(),
                JobExecutionMode.SEPARATE_PARALLEL_EXECUTION.toString());
            items.add(Messages.TestProjectBuilder_JobExecutionMode_9(),
                JobExecutionMode.NO_TESTCASE_EXECUTION.toString());
            return items;
        }

        /**
         * Validates the filter expression.
         *
         * @param value the filter expression
         * @return the form validation
         */
        public FormValidation doCheckFilterExpression(@QueryParameter final String value) {
            return testValidator.validateFilterExpression(value);
        }
    }
}
