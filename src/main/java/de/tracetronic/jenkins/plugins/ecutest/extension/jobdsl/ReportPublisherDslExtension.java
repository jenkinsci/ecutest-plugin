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
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import com.google.common.base.Preconditions;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * Class providing report related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@Extension(optional = true)
public class ReportPublisherDslExtension extends AbstractReportPublisherDslExtension {

    private static final String OPT_ATX_NAME = "atxName";

    /**
     * {@link DslExtensionMethod} for publishing ATX reports.
     *
     * @param atxName
     *            the tool name identifying the {@link ATXInstallation} to be used
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link ATXPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishATX(final String atxName, final Runnable closure) {
        Preconditions.checkNotNull(atxName, NOT_NULL_MSG, OPT_ATX_NAME);

        final PublishATXContext context = new PublishATXContext();
        executeInContext(closure, context);

        final ATXPublisher publisher = new ATXPublisher(atxName, context.allowMissing, context.runOnFailed);
        Preconditions.checkNotNull(publisher.getInstallation(), NO_INSTALL_MSG, atxName);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing UNIT reports.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link JUnitPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishUNIT(final String toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final JUnitPublisherContext context = new JUnitPublisherContext();
        executeInContext(closure, context);

        final JUnitPublisher publisher = new JUnitPublisher(toolName, context.unstableThreshold,
                context.failedThreshold, context.allowMissing, context.runOnFailed);
        Preconditions.checkNotNull(publisher.getToolInstallation(), NO_INSTALL_MSG, toolName);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing TRF reports.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link TRFPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishTRF(final Runnable closure) {
        final PublishTRFContext context = new PublishTRFContext();
        executeInContext(closure, context);
        return new TRFPublisher(context.allowMissing, context.runOnFailed);
    }

    /**
     * {@link DslExtensionMethod} for publishing ECU-TEST logs.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link ETLogPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishETLogs(final Runnable closure) {
        final PublishETLogContext context = new PublishETLogContext();
        executeInContext(closure, context);
        return new ETLogPublisher(context.allowMissing, context.runOnFailed, context.unstableOnWarning,
                context.failedOnError);
    }

    /**
     * {@link Context} class providing ATX publisher methods for the nested DSL context.
     */
    public class PublishATXContext extends AbstractReportContext {
    }

    /**
     * {@link Context} class providing UNIT publisher methods for the nested DSL context.
     */
    public class JUnitPublisherContext extends AbstractReportContext {

        private double unstableThreshold;
        private double failedThreshold;

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
     */
    public class PublishTRFContext extends AbstractReportContext {
    }

    /**
     * {@link Context} class providing ECU-TEST log publisher methods for the nested DSL context.
     */
    public class PublishETLogContext extends AbstractReportContext {

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
