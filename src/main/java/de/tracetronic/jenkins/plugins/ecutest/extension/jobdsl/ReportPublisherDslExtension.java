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
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import hudson.Extension;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import com.google.common.base.Preconditions;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.JUnitValidator;

/**
 * Class providing report-related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@Extension(optional = true)
public class ReportPublisherDslExtension extends AbstractDslExtension {

    /**
     * Option name for the ATX installation name.
     */
    protected static final String OPT_ATX_NAME = "atxName";

    /**
     * Validator to check UNIT-related DSL options.
     */
    private final JUnitValidator validator = new JUnitValidator();

    /**
     * {@link DslExtensionMethod} for publishing ATX reports.
     *
     * @param atxName
     *            the tool name identifying the {@link ATXInstallation} to be used
     * @return the instance of a {@link ATXPublisher}
     * @see ATXPublisher#ATXPublisher(String, boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishATX(final String atxName) {
        Preconditions.checkNotNull(atxName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final ATXPublisher publisher = new ATXPublisher(atxName, false, false);
        Preconditions.checkNotNull(publisher.getInstallation(), NO_INSTALL_MSG, atxName);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing ATX reports.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link ATXPublisher}
     * @see ATXPublisher#ATXPublisher(String, boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishATX(final Runnable closure) {
        final PublishATXContext context = new PublishATXContext();
        executeInContext(closure, context);
        return new ATXPublisher(context.atxName, context.allowMissing, context.runOnFailed);
    }

    /**
     * {@link DslExtensionMethod} for publishing UNIT reports.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param unstableThreshold
     *            the unstable threshold
     * @param failedThreshold
     *            the failed threshold
     * @return the instance of a {@link JUnitPublisher}
     * @see JUnitPublisher#JUnitPublisher(String, double, double, boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishUNIT(final String toolName, final double unstableThreshold, final double failedThreshold) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);
        FormValidation validation = validator.validateUnstableThreshold(String.valueOf(unstableThreshold));
        Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
        validation = validator.validateFailedThreshold(String.valueOf(failedThreshold));
        Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());

        final JUnitPublisher publisher = new JUnitPublisher(toolName, unstableThreshold, failedThreshold, false, false);
        // TODO: validate installation
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing UNIT reports.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link JUnitPublisher}
     * @see JUnitPublisher#JUnitPublisher(String, double, double, boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishUNIT(final Runnable closure) {
        final JUnitPublisherContext context = new JUnitPublisherContext();
        executeInContext(closure, context);
        return new JUnitPublisher(context.toolName, context.unstableThreshold, context.failedThreshold,
                context.allowMissing, context.runOnFailed);
    }

    /**
     * {@link DslExtensionMethod} for publishing TRF reports.
     *
     * @return the instance of a {@link TRFPublisher}
     * @see TRFPublisher#TRFPublisher(boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishTRF() {
        return new TRFPublisher(false, false);
    }

    /**
     * {@link DslExtensionMethod} for publishing ATX reports.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link TRFPublisher}
     * @see TRFPublisher#TRFPublisher(boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishTRF(final Runnable closure) {
        final PublishTRFContext context = new PublishTRFContext();
        executeInContext(closure, context);
        return new TRFPublisher(context.allowMissing, context.runOnFailed);
    }

    /**
     * {@link DslExtensionMethod} for publishing ECU-TEST logs.
     *
     * @param unstableOnWarning
     *            specifies whether to mark the build as unstable if warnings found
     * @param failedOnError
     *            specifies whether to mark the build as failed if errors found
     * @return the instance of a {@link ETLogPublisher}
     * @see ETLogPublisher#ETLogPublisher(boolean, boolean, boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishLogs(final boolean unstableOnWarning, final boolean failedOnError) {
        return new ETLogPublisher(unstableOnWarning, failedOnError, false, false);
    }

    /**
     * {@link DslExtensionMethod} for publishing ECU-TEST logs.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link ETLogPublisher}
     * @see ETLogPublisher#ETLogPublisher(boolean, boolean, boolean, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object publishLogs(final Runnable closure) {
        final PublishLogContext context = new PublishLogContext();
        executeInContext(closure, context);
        return new ETLogPublisher(context.allowMissing, context.runOnFailed, context.unstableOnWarning,
                context.failedOnError);
    }

    /**
     * Common base class providing report-related DSL extensions.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public abstract class AbstractReportContext implements Context {

        /**
         * The allow missing reports setting.
         */
        protected boolean allowMissing = false;

        /**
         * The run on failed setting.
         */
        protected boolean runOnFailed = false;

        /**
         * Option defining whether missing reports are allowed.
         *
         * @param value
         *            the value
         */
        public void allowMissing(final boolean value) {
            allowMissing = value;
        }

        /**
         * Option defining whether this publisher even runs on a failed build.
         *
         * @param value
         *            the value
         */
        public void runOnFailed(final boolean value) {
            runOnFailed = value;
        }
    }

    /**
     * {@link Context} class providing ATX publisher methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class PublishATXContext extends AbstractReportContext {

        private String atxName;

        /**
         * Option identifying the {@link ATXInstallation} to be used.
         *
         * @param value
         *            the value
         */
        public void atxName(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_ATX_NAME);
            atxName = value;
        }
    }

    /**
     * {@link Context} class providing UNIT publisher methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class JUnitPublisherContext extends AbstractReportContext {

        private String toolName;
        private double unstableThreshold;
        private double failedThreshold;

        /**
         * Option identifying the {@link ETInstallation} to be used.
         *
         * @param value
         *            the value
         */
        public void toolName(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TOOL_NAME);
            toolName = value;
        }

        /**
         * Option defining the unstable threshold.
         *
         * @param value
         *            the value
         */
        public void unstableThreshold(final double value) {
            final FormValidation validation = validator.validateUnstableThreshold(String.valueOf(value));
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            unstableThreshold = value;
        }

        /**
         * Option defining the failed threshold.
         *
         * @param value
         *            the value
         */
        public void failedThreshold(final double value) {
            final FormValidation validation = validator.validateFailedThreshold(String.valueOf(value));
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            failedThreshold = value;
        }
    }

    /**
     * {@link Context} class providing TRF publisher methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class PublishTRFContext extends AbstractReportContext {
    }

    /**
     * {@link Context} class providing ECU-TEST log publisher methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class PublishLogContext extends AbstractReportContext {

        private boolean unstableOnWarning;
        private boolean failedOnError;

        /**
         * Option defining whether to mark the build as unstable if warnings found.
         *
         * @param value
         *            the value
         */
        public void unstableOnWarning(final boolean value) {
            unstableOnWarning = value;
        }

        /**
         * Option defining whether to mark the build as failed if errors found.
         *
         * @param value
         *            the value
         */
        public void failedOnError(final boolean value) {
            failedOnError = value;
        }
    }
}
