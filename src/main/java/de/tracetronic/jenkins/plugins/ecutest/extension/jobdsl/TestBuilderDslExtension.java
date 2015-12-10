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

import java.util.ArrayList;
import java.util.List;

import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import com.google.common.base.Preconditions;

import de.tracetronic.jenkins.plugins.ecutest.test.TestFolderBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestFolderBuilder.ScanMode;
import de.tracetronic.jenkins.plugins.ecutest.test.TestPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;

/**
 * Class providing test related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@Extension(optional = true)
public class TestBuilderDslExtension extends AbstractTestBuilderDslExtension {

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST package.
     *
     * @param pkgFile
     *            the package file
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link TestPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testPackage(final CharSequence pkgFile, final Runnable closure) {
        Preconditions.checkNotNull(pkgFile, NOT_NULL_MSG, OPT_TEST_FILE);

        final TestPackageContext context = new TestPackageContext();
        executeInContext(closure, context);

        return new TestPackageBuilder(pkgFile.toString(), context.testConfig, context.packageConfig,
                context.executionConfig);
    }

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST package with default settings.
     *
     * @param pkgFile
     *            the package file
     * @return the instance of a {@link TestPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testPackage(final CharSequence pkgFile) {
        return testPackage(pkgFile, null);
    }

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST project.
     *
     * @param prjFile
     *            the project file
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link TestProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testProject(final CharSequence prjFile, final Runnable closure) {
        Preconditions.checkNotNull(prjFile, NOT_NULL_MSG, OPT_TEST_FILE);

        final TestProjectContext context = new TestProjectContext();
        executeInContext(closure, context);

        return new TestProjectBuilder(prjFile.toString(), context.testConfig, context.projectConfig,
                context.executionConfig);
    }

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST project with default settings.
     *
     * @param prjFile
     *            the project file
     * @return the instance of a {@link TestProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testProject(final CharSequence prjFile) {
        return testProject(prjFile, null);
    }

    /**
     * {@link DslExtensionMethod} providing the execution of ECU-TEST
     * packages and projects inside of a test folder.
     *
     * @param testFolder
     *            the test folder
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link TestFolderBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testFolder(final CharSequence testFolder, final Runnable closure) {
        Preconditions.checkNotNull(testFolder, NOT_NULL_MSG, OPT_TEST_FILE);

        final TestFolderContext context = new TestFolderContext();
        executeInContext(closure, context);

        return new TestFolderBuilder(testFolder.toString(), context.scanMode, context.recursiveScan,
                context.testConfig, context.packageConfig, context.projectConfig, context.executionConfig);
    }

    /**
     * {@link DslExtensionMethod} providing the execution of ECU-TEST
     * packages and projects inside of a test folder with default settings.
     *
     * @param testFolder
     *            the test folder
     * @return the instance of a {@link TestFolderBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testFolder(final CharSequence testFolder) {
        return testFolder(testFolder, null);
    }

    /**
     * {@link Context} class providing ECU-TEST package execution methods for the nested DSL context.
     */
    public class TestPackageContext extends AbstractTestContext {

        private PackageConfig packageConfig;

        /**
         * Option defining the package configuration.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void packageConfig(final Runnable closure) {
            final PackageConfigContext context = new PackageConfigContext();
            executeInContext(closure, context);
            packageConfig = new PackageConfig(context.runTest, context.runTraceAnalysis, context.parameters);
        }
    }

    /**
     * {@link Context} class providing package configuration methods for the nested DSL context.
     */
    public class PackageConfigContext implements Context {

        private boolean runTest = true;
        private boolean runTraceAnalysis = true;
        private List<PackageParameter> parameters;

        /**
         * Option defining whether to run the test.
         *
         * @param value
         *            the value
         */
        public void runTest(final boolean value) {
            runTest = value;
        }

        /**
         * Option defining whether to run the trace analysis.
         *
         * @param value
         *            the value
         */
        public void runTraceAnalysis(final boolean value) {
            runTraceAnalysis = value;
        }

        /**
         * Option defining the package parameters.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void parameters(final Runnable closure) {
            final PackageParametersContext context = new PackageParametersContext();
            executeInContext(closure, context);
            parameters = context.parameters;
        }

        /**
         * {@link Context} class providing the package parameters methods for the nested DSL context.
         */
        public class PackageParametersContext implements Context {

            private static final String OPT_PARAM_NAME = "parameter name";
            private static final String OPT_PARAM_VALUE = "parameter value";

            private final List<PackageParameter> parameters = new ArrayList<PackageParameter>();

            /**
             * Option defining the package parameter.
             *
             * @param name
             *            the parameter name
             * @param value
             *            the parameter value
             */
            public void parameter(final CharSequence name, final CharSequence value) {
                Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_PARAM_NAME);
                Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_PARAM_VALUE);

                FormValidation validation = validator.validateParameterName(name.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                validation = validator.validateParameterValue(value.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());

                parameters.add(new PackageParameter(name.toString(), value.toString()));
            }

            /**
             * Option defining the package parameter.
             *
             * @param closure
             *            the nested Groovy closure
             */
            public void parameter(final Runnable closure) {
                final PackageParameterContext context = new PackageParameterContext();
                executeInContext(closure, context);
                parameters.add(new PackageParameter(context.name, context.value));
            }

            /**
             * {@link Context} class providing the single package parameter methods for the nested DSL context.
             */
            public class PackageParameterContext implements Context {

                private String name;
                private String value;

                /**
                 * Option defining the package parameter name.
                 *
                 * @param value
                 *            the value
                 */
                public void name(final CharSequence value) {
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_PARAM_NAME);
                    final FormValidation validation = validator.validateParameterName(value.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                            validation.getMessage());
                    name = value.toString();
                }

                /**
                 * Option defining the package parameter value.
                 *
                 * @param value
                 *            the value
                 */
                public void value(final CharSequence value) {
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_PARAM_VALUE);
                    final FormValidation validation = validator.validateParameterValue(value.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                            validation.getMessage());
                    this.value = value.toString();
                }
            }
        }
    }

    /**
     * {@link Context} class providing ECU-TEST project execution methods for the nested DSL context.
     */
    public class TestProjectContext extends AbstractTestContext {

        private ProjectConfig projectConfig;

        /**
         * Option defining the project configuration.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void projectConfig(final Runnable closure) {
            final ProjectConfigContext context = new ProjectConfigContext();
            executeInContext(closure, context);
            projectConfig = new ProjectConfig(context.execInCurrentPkgDir, context.filterExpression,
                    context.jobExecutionMode);
        }
    }

    /**
     * {@link Context} class providing project configuration methods for the nested DSL context.
     */
    public class ProjectConfigContext implements Context {

        private static final String OPT_FILTER_EXPR = "filterExpression";
        private static final String OPT_JOB_EXEC_MODE = "jobExecutionMode";

        private boolean execInCurrentPkgDir;
        private String filterExpression;
        private JobExecutionMode jobExecutionMode = JobExecutionMode.SEQUENTIAL_EXECUTION;

        /**
         * Option defining the package reference mode.
         *
         * @param value
         *            the value
         */
        public void execInCurrentPkgDir(final boolean value) {
            execInCurrentPkgDir = value;
        }

        /**
         * Option defining the project filter expression.
         *
         * @param value
         *            the value
         */
        public void filterExpression(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_FILTER_EXPR);
            final FormValidation validation = validator.validateFilterExpression(value.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            filterExpression = value.toString();
        }

        /**
         * Option defining the job execution mode.
         *
         * @param value
         *            the value as String
         */
        public void jobExecutionMode(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_JOB_EXEC_MODE);
            jobExecutionMode = JobExecutionMode.valueOf(value.toString());
        }

        /**
         * Option defining the job execution mode.
         *
         * @param value
         *            the value as Integer
         */
        public void jobExecutionMode(final int value) {
            jobExecutionMode = JobExecutionMode.fromValue(value);
        }
    }

    /**
     * {@link Context} class providing test folder execution methods for the nested DSL context.
     */
    public class TestFolderContext extends AbstractTestContext {

        private ScanMode scanMode = ScanMode.PACKAGES_AND_PROJECTS;
        private boolean recursiveScan;
        private PackageConfig packageConfig;
        private ProjectConfig projectConfig;

        /**
         * Option defining the scan mode.
         *
         * @param value
         *            the value
         */
        public void scanMode(final CharSequence value) {
            scanMode = ScanMode.valueOf(value.toString());
        }

        /**
         * Option defining whether to scan recursively.
         *
         * @param value
         *            the value
         */
        public void recursiveScan(final boolean value) {
            recursiveScan = value;
        }

        /**
         * Option defining the package configuration.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void packageConfig(final Runnable closure) {
            final PackageConfigContext context = new PackageConfigContext();
            executeInContext(closure, context);
            packageConfig = new PackageConfig(context.runTest, context.runTraceAnalysis, context.parameters);
        }

        /**
         * Option defining the project configuration.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void projectConfig(final Runnable closure) {
            final ProjectConfigContext context = new ProjectConfigContext();
            executeInContext(closure, context);
            projectConfig = new ProjectConfig(context.execInCurrentPkgDir, context.filterExpression,
                    context.jobExecutionMode);
        }
    }
}
