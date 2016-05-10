/**
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
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import hudson.Extension;
import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.List;

import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ReportGeneratorValidator;

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
    public Object publishATX(final CharSequence atxName, final Runnable closure) {
        Preconditions.checkNotNull(atxName, NOT_NULL_MSG, OPT_ATX_NAME);

        final PublishATXContext context = new PublishATXContext();
        executeInContext(closure, context);

        final ATXPublisher publisher = new ATXPublisher(atxName.toString(), context.allowMissing, context.runOnFailed,
                context.archiving, context.keepAll);
        checkATXInstallation(atxName.toString(), publisher);
        return publisher;
    }

    /**
     * Checks whether the ATX installation identified by given name exists.
     *
     * @param atxName
     *            the ATX tool name
     * @param publisher
     *            the publisher
     */
    private void checkATXInstallation(final String atxName, final ATXPublisher publisher) {
        if (StringUtils.containsNone(atxName, "$")) {
            Preconditions.checkNotNull(publisher.getInstallation(), NO_INSTALL_MSG, atxName);
        }
    }

    /**
     * {@link DslExtensionMethod} for publishing ATX reports with default settings.
     *
     * @param atxName
     *            the tool name identifying the {@link ATXInstallation} to be used
     * @return the instance of a {@link ATXPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishATX(final CharSequence atxName) {
        return publishATX(atxName, null);
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
    public Object publishUNIT(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final PublishUNITContext context = new PublishUNITContext();
        executeInContext(closure, context);

        final JUnitPublisher publisher = new JUnitPublisher(toolName.toString(), context.unstableThreshold,
                context.failedThreshold, context.allowMissing, context.runOnFailed, context.archiving, context.keepAll);
        checkToolInstallation(toolName.toString(), publisher);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing UNIT reports with default settings.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link JUnitPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishUNIT(final CharSequence toolName) {
        return publishUNIT(toolName, null);
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
        return new TRFPublisher(context.allowMissing, context.runOnFailed, context.archiving, context.keepAll);
    }

    /**
     * {@link DslExtensionMethod} for publishing TRF reports with default settings.
     *
     * @return the instance of a {@link TRFPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishTRF() {
        return publishTRF(null);
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
        return new ETLogPublisher(context.unstableOnWarning, context.failedOnError, context.testSpecific,
                context.allowMissing, context.runOnFailed, context.archiving, context.keepAll);
    }

    /**
     * {@link DslExtensionMethod} for publishing ECU-TEST logs with default settings.
     *
     * @return the instance of a {@link ETLogPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishETLogs() {
        return publishETLogs(null);
    }

    /**
     * {@link DslExtensionMethod} for publishing generated reports.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link ReportGeneratorPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishGenerators(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final PublishGeneratorsContext context = new PublishGeneratorsContext();
        executeInContext(closure, context);

        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher(toolName.toString(),
                context.generators, context.customGenerators, context.allowMissing, context.runOnFailed,
                context.archiving, context.keepAll);
        checkToolInstallation(toolName.toString(), publisher);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing generated reports with default settings.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link ReportGeneratorPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishGenerators(final CharSequence toolName) {
        return publishGenerators(toolName, null);
    }

    /**
     * {@link Context} class providing ATX publisher methods for the nested DSL context.
     */
    public class PublishATXContext extends AbstractReportContext {
    }

    /**
     * {@link Context} class providing UNIT publisher methods for the nested DSL context.
     */
    public class PublishUNITContext extends AbstractReportContext {

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
        private boolean testSpecific;

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

        /**
         * Option defining whether to parse the test specific log files.
         *
         * @param value
         *            the value
         */
        public void testSpecific(final boolean value) {
            testSpecific = value;
        }
    }

    /**
     * {@link Context} class providing report generator publisher methods for the nested DSL context.
     */
    public class PublishGeneratorsContext extends AbstractReportContext {

        private List<ReportGeneratorConfig> generators;
        private List<ReportGeneratorConfig> customGenerators;

        /**
         * Option defining the default report generators.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void generators(final Runnable closure) {
            final ReportGeneratorContext context = new ReportGeneratorContext();
            executeInContext(closure, context);
            generators = context.generators;
        }

        /**
         * Option defining the custom report generators.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void customGenerators(final Runnable closure) {
            final ReportGeneratorContext context = new ReportGeneratorContext();
            executeInContext(closure, context);
            customGenerators = context.generators;
        }

        /**
         * {@link Context} class providing the report generator configuration methods for the nested DSL context.
         */
        public class ReportGeneratorContext implements Context {

            private static final String OPT_GENERATOR_NAME = "generator name";

            private final List<ReportGeneratorConfig> generators = new ArrayList<ReportGeneratorConfig>();

            /**
             * Validator to check report generated related DSL options.
             */
            protected final ReportGeneratorValidator reportValidator = new ReportGeneratorValidator();

            /**
             * Option defining the report generator.
             *
             * @param name
             *            the generator name
             */
            public void generator(final CharSequence name) {
                Preconditions.checkNotNull(name, NOT_NULL_MSG, OPT_GENERATOR_NAME);

                final FormValidation validation = reportValidator.validateGeneratorName(name.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                generators.add(new ReportGeneratorConfig(name.toString(), null));
            }

            /**
             * Option defining the report generator.
             *
             * @param name
             *            the generator name
             * @param closure
             *            the nested Groovy closure
             */
            public void generator(final CharSequence name, final Runnable closure) {
                final ReportGeneratorConfigContext context = new ReportGeneratorConfigContext();
                executeInContext(closure, context);
                generators.add(new ReportGeneratorConfig(name.toString(), context.settings));
            }

            /**
             * Option defining the custom report generator.
             *
             * @param name
             *            the generator name
             */
            public void customGenerator(final CharSequence name) {
                generator(name);
            }

            /**
             * Option defining the custom report generator.
             *
             * @param name
             *            the generator name
             * @param closure
             *            the nested Groovy closure
             */
            public void customGenerator(final CharSequence name, final Runnable closure) {
                generator(name, closure);
            }

            /**
             * {@link Context} class providing the report generator methods for the nested DSL context.
             */
            public class ReportGeneratorConfigContext implements Context {

                private List<ReportGeneratorSetting> settings;

                /**
                 * Option defining the report generator settings.
                 *
                 * @param closure
                 *            the nested Groovy closure
                 */
                public void settings(final Runnable closure) {
                    final ReportGeneratorSettingsContext context = new ReportGeneratorSettingsContext();
                    executeInContext(closure, context);
                    settings = context.settings;
                }
            }

            /**
             * {@link Context} class providing the report generator settings methods for the nested DSL context.
             */
            public class ReportGeneratorSettingsContext implements Context {

                private static final String OPT_SETTING_NAME = "setting name";
                private static final String OPT_SETTING_VALUE = "setting value";

                private final List<ReportGeneratorSetting> settings = new ArrayList<ReportGeneratorSetting>();

                /**
                 * Option defining the report generator setting.
                 *
                 * @param name
                 *            the setting name
                 * @param value
                 *            the setting value
                 */
                public void setting(final CharSequence name, final CharSequence value) {
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_SETTING_NAME);
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_SETTING_VALUE);

                    FormValidation validation = reportValidator.validateSettingName(name.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                    validation = reportValidator.validateSettingValue(value.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());

                    settings.add(new ReportGeneratorSetting(name.toString(), value.toString()));
                }

                /**
                 * Option defining the report generator setting.
                 *
                 * @param closure
                 *            the nested Groovy closure
                 */
                public void setting(final Runnable closure) {
                    final ReportGeneratorSettingContext context = new ReportGeneratorSettingContext();
                    executeInContext(closure, context);
                    settings.add(new ReportGeneratorSetting(context.name, context.value));
                }

                /**
                 * {@link Context} class providing the report generator setting methods for the nested DSL context.
                 */
                public class ReportGeneratorSettingContext implements Context {

                    private String name;
                    private String value;

                    /**
                     * Option defining the settings name.
                     *
                     * @param value
                     *            the value
                     */
                    public void name(final CharSequence value) {
                        Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_SETTING_NAME);
                        final FormValidation validation = reportValidator.validateSettingName(value.toString());
                        Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                                validation.getMessage());
                        name = value.toString();
                    }

                    /**
                     * Option defining the settings value.
                     *
                     * @param value
                     *            the value
                     */
                    public void value(final CharSequence value) {
                        Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_SETTING_VALUE);
                        final FormValidation validation = reportValidator.validateSettingValue(value.toString());
                        Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                                validation.getMessage());
                        this.value = value.toString();
                    }
                }
            }
        }
    }
}
