/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;

/**
 * Class holding the project configuration.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ProjectConfig extends AbstractDescribableImpl<ProjectConfig> implements Serializable,
        ExpandableConfig {

    private static final long serialVersionUID = 1L;

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
         * @param value
         *            the value
         */
        JobExecutionMode(final int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the job execution mode by Integer value.
         *
         * @param value
         *            the value
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
    }

    private final boolean execInCurrentPkgDir;
    private final String filterExpression;

    /**
     * Holds the analysis job execution mode.<br/>
     * Backward compatibility will be retained by {@link #readResolve()}.
     *
     * @since 1.2
     */
    private final JobExecutionMode jobExecMode;

    /**
     * @deprecated since 1.2
     */
    @Deprecated
    private transient int jobExecutionMode;

    /**
     * Instantiates a new {@link ProjectConfig}.
     *
     * @param execInCurrentPkgDir
     *            specifies whether to search the references in the current package directory
     * @param filterExpression
     *            the filter expression to filter the package and project references
     * @param jobExecMode
     *            the analysis job execution mode
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
     * Instantiates a new {@link ProjectConfig}.
     *
     * @param execInCurrentPkgDir
     *            specifies whether to search the references in the current package directory
     * @param filterExpression
     *            the filter expression to filter the package and project references
     * @param jobExecutionMode
     *            the analysis job execution mode
     * @deprecated since 1.2
     */
    @Deprecated
    public ProjectConfig(final boolean execInCurrentPkgDir, final String filterExpression,
            final int jobExecutionMode) {
        this(execInCurrentPkgDir, filterExpression, JobExecutionMode.fromValue(jobExecutionMode));
    }

    /**
     * Convert legacy configuration into the new class structure.
     *
     * @return an instance of this class with all the new fields transferred from the old structure to the new one
     */
    public final Object readResolve() {
        if (jobExecMode == null) {
            return new ProjectConfig(execInCurrentPkgDir, filterExpression,
                    JobExecutionMode.fromValue(jobExecutionMode));
        }
        return this;
    }

    /**
     * @return the execInCurrentPkgDir
     */
    public boolean isExecInCurrentPkgDir() {
        return execInCurrentPkgDir;
    }

    /**
     * @return the filterExpression
     */
    public String getFilterExpression() {
        return filterExpression;
    }

    /**
     * @return the jobExecutionMode
     */
    public JobExecutionMode getJobExecMode() {
        return jobExecMode;
    }

    @Override
    public ProjectConfig expand(final EnvVars envVars) {
        final String expFilterExpression = envVars.expand(getFilterExpression());
        return new ProjectConfig(isExecInCurrentPkgDir(), expFilterExpression, getJobExecMode());
    }

    @Override
    public final boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof ProjectConfig)) {
            return false;
        }
        final ProjectConfig other = (ProjectConfig) that;
        if ((filterExpression == null ? other.filterExpression != null : !filterExpression
                .equals(other.filterExpression))
                || execInCurrentPkgDir != other.execInCurrentPkgDir
                || jobExecMode != other.jobExecMode) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(execInCurrentPkgDir).append(filterExpression)
                .append(jobExecMode).toHashCode();
    }

    /**
     * @return the instance of a {@link ProjectConfig}.
     */
    public static ProjectConfig newInstance() {
        return new ProjectConfig(false, null, JobExecutionMode.SEQUENTIAL_EXECUTION);
    }

    /**
     * DescriptorImpl for {@link ProjectConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ProjectConfig> {

        private final TestValidator testValidator = new TestValidator();

        @Override
        public String getDisplayName() {
            return "Project Configuration";
        }

        /**
         * @return the default scan mode
         */
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
         * @param value
         *            the filter expression
         * @return the form validation
         */
        public FormValidation doCheckFilterExpression(@QueryParameter final String value) {
            return testValidator.validateFilterExpression(value);
        }
    }
}
