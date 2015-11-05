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
package de.tracetronic.jenkins.plugins.ecutest.report.junit;

import hudson.AbortException;
import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.IOException;
import java.math.BigDecimal;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.JUnitValidator;

/**
 * Publisher providing the generation of JUnit reports and adds a {@link TestResultAction} by invoking the
 * {@link JUnitTestResultParser}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class JUnitPublisher extends AbstractReportPublisher implements MatrixAggregatable {

    private final String toolName;
    private final double unstableThreshold;
    private final double failedThreshold;

    /**
     * Instantiates a new {@link JUnitPublisher}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param unstableThreshold
     *            the threshold which sets the build status to unstable if exceeded
     * @param failedThreshold
     *            the threshold which sets the build status to failed if exceeded
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     */
    @DataBoundConstructor
    public JUnitPublisher(final String toolName, final double unstableThreshold,
            final double failedThreshold, final boolean allowMissing, final boolean runOnFailed) {
        super(allowMissing, runOnFailed);
        this.toolName = toolName;
        this.unstableThreshold = convertToPercentage(unstableThreshold);
        this.failedThreshold = convertToPercentage(failedThreshold);
    }

    /**
     * @return the {@link ETInstallation} name
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the unstable threshold
     */
    public double getUnstableThreshold() {
        return unstableThreshold;
    }

    /**
     * @return the failed threshold
     */
    public double getFailedThreshold() {
        return failedThreshold;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @return the tool installation
     */
    @CheckForNull
    public AbstractToolInstallation getToolInstallation() {
        for (final AbstractToolInstallation installation : getDescriptor().getInstallations()) {
            if (toolName != null && toolName.equals(installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        final TestResultProjectAction action = project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return new TestResultProjectAction(project);
        } else {
            return action;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
            final BuildListener listener) {
        return new TestResultAggregator(build, launcher, listener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        // Check OS running this build
        if (!ProcessUtil.checkOS(launcher, listener)) {
            return false;
        }

        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Publishing UNIT reports...");

        final Result buildResult = build.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            logger.logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return true;
        }

        // Get selected ECU-TEST installation
        final AbstractToolInstallation installation = configureToolInstallation(toolName, listener,
                build.getEnvironment(listener));

        // Generate JUnit reports
        final JUnitReportGenerator generator = new JUnitReportGenerator();
        if (!generator.generate(installation, build, launcher, listener)) {
            build.setResult(Result.FAILURE);
            return true;
        }

        // Parse generated JUnit reports
        final JUnitTestResultParser parser = new JUnitTestResultParser();
        final TestResult testResult = parser.parse(JUnitReportGenerator.UNIT_TEMPLATE_NAME, build, launcher,
                listener);

        // Add action for publishing JUnit results
        TestResultAction action;
        try {
            action = new TestResultAction(build, testResult, listener);
        } catch (final NullPointerException npe) {
            logger.logError(String.format("Parsing UNIT test results failed: %s", npe.getMessage()));
            throw new AbortException("Parsing failure!");
        }
        testResult.freeze(action);
        build.addAction(action);

        // Change build result if thresholds exceeded
        if (setBuildResult(build, listener, testResult)) {
            logger.logInfo("UNIT reports published successfully.");
        }
        return true;
    }

    /**
     * Sets the build result according to the test result.
     *
     * @param build
     *            the build
     * @param listener
     *            the listener
     * @param testResult
     *            the test result
     * @return {@code true} if test results exist and could be published
     */
    private boolean setBuildResult(final AbstractBuild<?, ?> build, final BuildListener listener,
            final TestResult testResult) {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        if (testResult.getPassCount() == 0 && testResult.getFailCount() == 0) {
            logger.logInfo("-> No UNIT test results found.");
            if (!isAllowMissing()) {
                logger.logError("Empty test results are not allowed, setting build status to FAILURE!");
                build.setResult(Result.FAILURE);
                return false;
            }
        } else {
            logger.logInfo(String.format(
                    "-> Found %d test result(s) in total: #Passed: %d, #Failed: %d, #Skipped: %d",
                    testResult.getTotalCount(), testResult.getPassCount(), testResult.getFailCount(),
                    testResult.getSkipCount()));
        }

        final double failedPercentage = getFailedPercentage(testResult.getFailCount(),
                testResult.getTotalCount());
        if (failedPercentage > failedThreshold) {
            logger.logInfo(String.format(
                    "-> %.1f%% of failed test results exceed failed threshold of %.1f%%, "
                            + "setting build status to FAILURE!", failedPercentage, failedThreshold));
            build.setResult(Result.FAILURE);
        } else if (failedPercentage > unstableThreshold) {
            logger.logInfo(String.format(
                    "-> %.1f%% of failed test results exceed unstable threshold of %.1f%%, "
                            + "setting build status to UNSTABLE!", failedPercentage, unstableThreshold));
            build.setResult(Result.UNSTABLE);
        }
        return true;
    }

    /**
     * Gets the failed percentage.
     *
     * @param failedCount
     *            the failed count
     * @param totalCount
     *            the total count
     * @return the unstable percentage
     */
    public static double getFailedPercentage(final int failedCount, final int totalCount) {
        if (totalCount == 0) {
            return 0;
        }

        final double percentage = (double) failedCount / (double) totalCount * 100;
        return roundToDecimals(percentage, 1);
    }

    /**
     * Round to decimals.
     *
     * @param value
     *            the value to round
     * @param decimals
     *            the number of decimals
     * @return the rounded value
     */
    private static double roundToDecimals(final double value, final int decimals) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(decimals, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Converts to percentage value.
     *
     * @param value
     *            the value to convert
     * @return the percentage value
     */
    private static double convertToPercentage(final double value) {
        if (value < 0.0) {
            return 0.0;
        } else if (value > 100.0) {
            return 100.0;
        } else {
            return value;
        }
    }

    /**
     * DescriptorImpl for {@link JUnitPublisher}.
     */
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @CopyOnWrite
        private ETInstallation[] installations = new ETInstallation[0];

        private final JUnitValidator unitValidator;

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
            load();
            unitValidator = new JUnitValidator();
        }

        /**
         * Gets the tool descriptor.
         *
         * @return the tool descriptor
         */
        public ETInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(ETInstallation.DescriptorImpl.class);
        }

        /**
         * @return the list of ECU-TEST installations
         */
        public ETInstallation[] getInstallations() {
            return installations.clone();
        }

        /**
         * Sets the installations.
         *
         * @param installations
         *            the new installations
         */
        public void setInstallations(final ETInstallation... installations) {
            this.installations = installations;
            save();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.JUnitPublisher_DisplayName();
        }

        /**
         * Validates the unstable threshold.
         *
         * @param value
         *            the threshold
         * @return the form validation
         */
        public FormValidation doCheckUnstableThreshold(@QueryParameter final String value) {
            return unitValidator.validateUnstableThreshold(value);
        }

        /**
         * Validates the failed threshold.
         *
         * @param value
         *            the threshold
         * @return the form validation
         */
        public FormValidation doCheckFailedThreshold(@QueryParameter final String value) {
            return unitValidator.validateFailedThreshold(value);
        }
    }
}
