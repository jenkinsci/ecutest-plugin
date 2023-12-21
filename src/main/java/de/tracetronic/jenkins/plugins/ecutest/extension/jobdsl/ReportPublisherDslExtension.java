/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import com.google.common.base.Preconditions;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.tms.TMSPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ReportGeneratorValidator;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TMSValidator;
import hudson.Extension;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing report related DSL extensions.
 */
@Extension(optional = true)
public class ReportPublisherDslExtension extends AbstractReportPublisherDslExtension {

    private static final String OPT_ATX_NAME = "atxName";
    private static final String OPT_CREDENTIALS_ID = "credentialsId";

    /**
     * {@link DslExtensionMethod} for publishing ATX reports.
     *
     * @param atxName the tool name identifying the {@link ATXInstallation} to be used
     * @param closure the nested Groovy closure
     * @return the instance of a {@link ATXPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishATX(final CharSequence atxName, final Runnable closure) {
        Preconditions.checkNotNull(atxName, NOT_NULL_MSG, OPT_ATX_NAME);

        final PublishATXContext context = new PublishATXContext();
        executeInContext(closure, context);

        final ATXPublisher publisher = new ATXPublisher(atxName.toString());
        publisher.setAllowMissing(context.allowMissing);
        publisher.setRunOnFailed(context.runOnFailed);
        publisher.setArchiving(context.archiving);
        publisher.setKeepAll(context.keepAll);
        checkATXInstallation(atxName.toString(), publisher);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing ATX reports with default settings.
     *
     * @param atxName the tool name identifying the {@link ATXInstallation} to be used
     * @return the instance of a {@link ATXPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishATX(final CharSequence atxName) {
        return publishATX(atxName, null);
    }

    /**
     * Checks whether the ATX installation identified by given name exists.
     *
     * @param atxName   the ATX tool name
     * @param publisher the publisher
     */
    private void checkATXInstallation(final String atxName, final ATXPublisher publisher) {
        if (StringUtils.containsNone(atxName, "$")) {
            Preconditions.checkNotNull(publisher.getInstallation(), NO_INSTALL_MSG, atxName);
        }
    }

    /**
     * {@link DslExtensionMethod} for publishing UNIT reports.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @param closure  the nested Groovy closure
     * @return the instance of a {@link JUnitPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishUNIT(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final PublishUNITContext context = new PublishUNITContext();
        executeInContext(closure, context);

        final JUnitPublisher publisher = new JUnitPublisher(toolName.toString());
        publisher.setUnstableThreshold(context.unstableThreshold);
        publisher.setFailedThreshold(context.failedThreshold);
        publisher.setAllowMissing(context.allowMissing);
        publisher.setRunOnFailed(context.runOnFailed);
        publisher.setArchiving(context.archiving);
        publisher.setKeepAll(context.keepAll);
        checkToolInstallation(toolName.toString(), publisher);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing UNIT reports with default settings.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link JUnitPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishUNIT(final CharSequence toolName) {
        return publishUNIT(toolName, null);
    }

    /**
     * {@link DslExtensionMethod} for publishing TRF reports.
     *
     * @param closure the nested Groovy closure
     * @return the instance of a {@link TRFPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishTRF(final Runnable closure) {
        final PublishTRFContext context = new PublishTRFContext();
        executeInContext(closure, context);
        final TRFPublisher publisher = new TRFPublisher();
        publisher.setAllowMissing(context.allowMissing);
        publisher.setRunOnFailed(context.runOnFailed);
        publisher.setArchiving(context.archiving);
        publisher.setKeepAll(context.keepAll);
        return publisher;
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
     * {@link DslExtensionMethod} for publishing ecu.test logs.
     *
     * @param closure the nested Groovy closure
     * @return the instance of a {@link ETLogPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishETLogs(final Runnable closure) {
        final PublishETLogContext context = new PublishETLogContext();
        executeInContext(closure, context);
        final ETLogPublisher publisher = new ETLogPublisher();
        publisher.setUnstableOnWarning(context.unstableOnWarning);
        publisher.setFailedOnError(context.failedOnError);
        publisher.setTestSpecific(context.testSpecific);
        publisher.setAllowMissing(context.allowMissing);
        publisher.setRunOnFailed(context.runOnFailed);
        publisher.setArchiving(context.archiving);
        publisher.setKeepAll(context.keepAll);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing ecu.test logs with default settings.
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
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @param closure  the nested Groovy closure
     * @return the instance of a {@link ReportGeneratorPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishGenerators(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final PublishGeneratorsContext context = new PublishGeneratorsContext();
        executeInContext(closure, context);

        final ReportGeneratorPublisher publisher = new ReportGeneratorPublisher(toolName.toString());
        publisher.setGenerators(context.generators);
        publisher.setCustomGenerators(context.customGenerators);
        publisher.setAllowMissing(context.allowMissing);
        publisher.setRunOnFailed(context.runOnFailed);
        publisher.setArchiving(context.archiving);
        publisher.setKeepAll(context.keepAll);
        checkToolInstallation(toolName.toString(), publisher);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing generated reports with default settings.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link ReportGeneratorPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishGenerators(final CharSequence toolName) {
        return publishGenerators(toolName, null);
    }

    /**
     * {@link DslExtensionMethod} for publishing report to test management system.
     *
     * @param toolName      the tool name identifying the {@link ETInstallation} to be used
     * @param credentialsId the credentials id
     * @param closure       the nested Groovy closure
     * @return the instance of a {@link TMSPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishTMS(final CharSequence toolName, final CharSequence credentialsId, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);
        Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);

        final PublishTMSContext context = new PublishTMSContext();
        executeInContext(closure, context);

        final TMSPublisher publisher = new TMSPublisher(toolName.toString(), credentialsId.toString());
        publisher.setTimeout(context.timeout);
        publisher.setAllowMissing(context.allowMissing);
        publisher.setRunOnFailed(context.runOnFailed);
        publisher.setArchiving(context.archiving);
        publisher.setKeepAll(context.keepAll);
        checkToolInstallation(toolName.toString(), publisher);
        return publisher;
    }

    /**
     * {@link DslExtensionMethod} for publishing report to test management system with default settings.
     *
     * @param toolName      the tool name identifying the {@link ETInstallation} to be used
     * @param credentialsId the credentials id
     * @return the instance of a {@link ReportGeneratorPublisher}
     */
    @DslExtensionMethod(context = PublisherContext.class)
    public Object publishTMS(final CharSequence toolName, final CharSequence credentialsId) {
        return publishTMS(toolName, credentialsId, null);
    }

    /**
     * {@link Context} class providing ATX publisher methods for the nested DSL context.
     */
    public static class PublishATXContext extends AbstractReportContext {
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
         * @param value the value
         */
        public void unstableThreshold(final double value) {
            final FormValidation validation = validator.validateUnstableThreshold(String.valueOf(value));
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            unstableThreshold = value;
        }

        /**
         * Option defining the failed threshold.
         *
         * @param value the value
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
    public static class PublishTRFContext extends AbstractReportContext {
    }

    /**
     * {@link Context} class providing ecu.test log publisher methods for the nested DSL context.
     */
    public static class PublishETLogContext extends AbstractReportContext {

        private boolean unstableOnWarning;
        private boolean failedOnError;
        private boolean testSpecific;

        /**
         * Option defining whether to mark the build as unstable if warnings found.
         *
         * @param value the value
         */
        public void unstableOnWarning(final boolean value) {
            unstableOnWarning = value;
        }

        /**
         * Option defining whether to mark the build as failed if errors found.
         *
         * @param value the value
         */
        public void failedOnError(final boolean value) {
            failedOnError = value;
        }

        /**
         * Option defining whether to parse the test-specific log files.
         *
         * @param value the value
         */
        public void testSpecific(final boolean value) {
            testSpecific = value;
        }
    }

    /**
     * {@link Context} class providing report generator publisher methods for the nested DSL context.
     */
    public static class PublishGeneratorsContext extends AbstractReportContext {

        private List<ReportGeneratorConfig> generators;
        private List<ReportGeneratorConfig> customGenerators;

        /**
         * Option defining the default report generators.
         *
         * @param closure the nested Groovy closure
         */
        public void generators(final Runnable closure) {
            final ReportGeneratorContext context = new ReportGeneratorContext();
            executeInContext(closure, context);
            generators = context.generators;
        }

        /**
         * Option defining the custom report generators.
         *
         * @param closure the nested Groovy closure
         */
        public void customGenerators(final Runnable closure) {
            final ReportGeneratorContext context = new ReportGeneratorContext();
            executeInContext(closure, context);
            customGenerators = context.generators;
        }

        /**
         * {@link Context} class providing the report generator configuration methods for the nested DSL context.
         */
        public static class ReportGeneratorContext implements Context {

            private static final String OPT_GENERATOR_NAME = "generator name";
            /**
             * Validator to check report generated related DSL options.
             */
            protected final ReportGeneratorValidator reportValidator = new ReportGeneratorValidator();
            private final List<ReportGeneratorConfig> generators = new ArrayList<>();

            /**
             * Option defining the report generator.
             *
             * @param name the generator name
             */
            public void generator(final CharSequence name) {
                Preconditions.checkNotNull(name, NOT_NULL_MSG, OPT_GENERATOR_NAME);

                final FormValidation validation = reportValidator.validateGeneratorName(name.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                generators.add(new ReportGeneratorConfig(name.toString(), null, false));
            }

            /**
             * Option defining the report generator.
             *
             * @param name    the generator name
             * @param closure the nested Groovy closure
             */
            public void generator(final CharSequence name, final Runnable closure) {
                final ReportGeneratorConfigContext context = new ReportGeneratorConfigContext();
                executeInContext(closure, context);
                generators.add(new ReportGeneratorConfig(name.toString(), context.settings,
                    context.usePersistedSettings));
            }

            /**
             * Option defining the custom report generator.
             *
             * @param name the generator name
             */
            public void customGenerator(final CharSequence name) {
                generator(name);
            }

            /**
             * Option defining the custom report generator.
             *
             * @param name    the generator name
             * @param closure the nested Groovy closure
             */
            public void customGenerator(final CharSequence name, final Runnable closure) {
                generator(name, closure);
            }

            /**
             * {@link Context} class providing the report generator methods for the nested DSL context.
             */
            public class ReportGeneratorConfigContext implements Context {

                private List<ReportGeneratorSetting> settings;
                private boolean usePersistedSettings;

                /**
                 * Option defining the report generator settings.
                 *
                 * @param closure the nested Groovy closure
                 */
                public void settings(final Runnable closure) {
                    final ReportGeneratorSettingsContext context = new ReportGeneratorSettingsContext();
                    executeInContext(closure, context);
                    settings = context.settings;
                }

                /**
                 * Option defining whether to use report generator settings from persisted configurations files (XML).
                 *
                 * @param value the value
                 */
                public void usePersistedSettings(final boolean value) {
                    usePersistedSettings = value;
                }
            }

            /**
             * {@link Context} class providing the report generator settings methods for the nested DSL context.
             */
            public class ReportGeneratorSettingsContext implements Context {

                private static final String OPT_SETTING_NAME = "setting name";
                private static final String OPT_SETTING_VALUE = "setting value";

                private final List<ReportGeneratorSetting> settings = new ArrayList<>();

                /**
                 * Option defining the report generator setting.
                 *
                 * @param name  the setting name
                 * @param value the setting value
                 */
                public void setting(final CharSequence name, final CharSequence value) {
                    Preconditions.checkNotNull(name, NOT_NULL_MSG, OPT_SETTING_NAME);
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
                 * @param closure the nested Groovy closure
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
                     * @param value the value
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
                     * @param value the value
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

    /**
     * {@link Context} class providing TMS publisher methods for the nested DSL context.
     */
    public static class PublishTMSContext extends AbstractReportContext {

        /**
         * Validator to check report project import related DSL options.
         */
        protected final TMSValidator tmsValidator = new TMSValidator();

        private String timeout;

        /**
         * Option defining the timeout.
         *
         * @param value the value
         */
        public void timeout(final String value) {
            final FormValidation validation = tmsValidator.validateTimeout(value, TMSPublisher.getDefaultTimeout());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            timeout = value;
        }
    }
}
