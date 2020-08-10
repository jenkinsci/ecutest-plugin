/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import com.google.common.base.Preconditions;
import de.tracetronic.jenkins.plugins.ecutest.test.ExportPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.ExportProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.ImportPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.ImportProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestFolderBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestFolderBuilder.ScanMode;
import de.tracetronic.jenkins.plugins.ecutest.test.TestPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageOutputParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import hudson.Extension;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing test related DSL extensions.
 */
@Extension(optional = true)
public class TestBuilderDslExtension extends AbstractTestBuilderDslExtension {

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST package.
     *
     * @param pkgFile the package file
     * @param closure the nested Groovy closure
     * @return the instance of a {@link TestPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testPackage(final CharSequence pkgFile, final Runnable closure) {
        Preconditions.checkNotNull(pkgFile, NOT_NULL_MSG, OPT_TEST_FILE);

        final TestPackageContext context = new TestPackageContext();
        executeInContext(closure, context);

        final TestPackageBuilder builder = new TestPackageBuilder(pkgFile.toString());
        builder.setTestConfig(context.testConfig);
        builder.setPackageConfig(context.packageConfig);
        builder.setExecutionConfig(context.executionConfig);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST package with default settings.
     *
     * @param pkgFile the package file
     * @return the instance of a {@link TestPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testPackage(final CharSequence pkgFile) {
        return testPackage(pkgFile, null);
    }

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST project.
     *
     * @param prjFile the project file
     * @param closure the nested Groovy closure
     * @return the instance of a {@link TestProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testProject(final CharSequence prjFile, final Runnable closure) {
        Preconditions.checkNotNull(prjFile, NOT_NULL_MSG, OPT_TEST_FILE);

        final TestProjectContext context = new TestProjectContext();
        executeInContext(closure, context);

        final TestProjectBuilder builder = new TestProjectBuilder(prjFile.toString());
        builder.setTestConfig(context.testConfig);
        builder.setProjectConfig(context.projectConfig);
        builder.setExecutionConfig(context.executionConfig);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the execution of an ECU-TEST project with default settings.
     *
     * @param prjFile the project file
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
     * @param testFolder the test folder
     * @param closure    the nested Groovy closure
     * @return the instance of a {@link TestFolderBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testFolder(final CharSequence testFolder, final Runnable closure) {
        Preconditions.checkNotNull(testFolder, NOT_NULL_MSG, OPT_TEST_FILE);

        final TestFolderContext context = new TestFolderContext();
        executeInContext(closure, context);

        final TestFolderBuilder builder = new TestFolderBuilder(testFolder.toString());
        builder.setScanMode(context.scanMode);
        builder.setRecursiveScan(context.recursiveScan);
        builder.setTestConfig(context.testConfig);
        builder.setPackageConfig(context.packageConfig);
        builder.setProjectConfig(context.projectConfig);
        builder.setExecutionConfig(context.executionConfig);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the execution of ECU-TEST
     * packages and projects inside of a test folder with default settings.
     *
     * @param testFolder the test folder
     * @return the instance of a {@link TestFolderBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object testFolder(final CharSequence testFolder) {
        return testFolder(testFolder, null);
    }

    /**
     * {@link DslExtensionMethod} providing the import of packages from test management system.
     *
     * @param closure the nested Groovy closure
     * @return the instance of a {@link ImportPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object importPackages(final Runnable closure) {
        final ImportPackageContext context = new ImportPackageContext();
        executeInContext(closure, context);

        return new ImportPackageBuilder(context.importConfigs);
    }

    /**
     * {@link DslExtensionMethod} providing the import of packages from test management system with default settings.
     *
     * @return the instance of a {@link ImportPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object importPackages() {
        return importPackages(null);
    }

    /**
     * {@link DslExtensionMethod} providing the import of projects from archive and test management system.
     *
     * @param closure the nested Groovy closure
     * @return the instance of a {@link ImportProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object importProjects(final Runnable closure) {
        final ImportProjectContext context = new ImportProjectContext();
        executeInContext(closure, context);

        return new ImportProjectBuilder(context.importConfigs);
    }

    /**
     * {@link DslExtensionMethod} providing the import of projects from archive
     * and test management system with default settings.
     *
     * @return the instance of a {@link ImportProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object importProjects() {
        return importProjects(null);
    }

    /**
     * {@link DslExtensionMethod} providing the export of packages to test management system.
     *
     * @param closure the nested Groovy closure
     * @return the instance of a {@link ExportPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object exportPackages(final Runnable closure) {
        final ExportPackageContext context = new ExportPackageContext();
        executeInContext(closure, context);

        return new ExportPackageBuilder(context.exportConfigs);
    }

    /**
     * {@link DslExtensionMethod} providing the export of packages to test management system with default settings.
     *
     * @return the instance of a {@link ExportPackageBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object exportPackages() {
        return exportPackages(null);
    }

    /**
     * {@link DslExtensionMethod} providing the export of projects to test management system.
     *
     * @param closure the nested Groovy closure
     * @return the instance of a {@link ExportProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object exportProjects(final Runnable closure) {
        final ExportProjectContext context = new ExportProjectContext();
        executeInContext(closure, context);

        return new ExportProjectBuilder(context.exportConfigs);
    }

    /**
     * {@link DslExtensionMethod} providing the export of projects to test management system with default settings.
     *
     * @return the instance of a {@link ExportProjectBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object exportProjects() {
        return exportProjects(null);
    }

    /**
     * {@link Context} class providing ECU-TEST package execution methods for the nested DSL context.
     */
    public class TestPackageContext extends AbstractTestContext {

        private PackageConfig packageConfig;

        /**
         * Option defining the package configuration.
         *
         * @param closure the nested Groovy closure
         */
        public void packageConfig(final Runnable closure) {
            final PackageConfigContext context = new PackageConfigContext();
            executeInContext(closure, context);
            packageConfig = new PackageConfig(context.runTest, context.runTraceAnalysis, context.parameters,
                    context.outputParameters);
        }
    }

    /**
     * {@link Context} class providing package configuration methods for the nested DSL context.
     */
    public class PackageConfigContext implements Context {

        private boolean runTest = true;
        private boolean runTraceAnalysis = true;
        private List<PackageParameter> parameters;
        private List<PackageOutputParameter> outputParameters;

        /**
         * Option defining whether to run the test.
         *
         * @param value the value
         */
        public void runTest(final boolean value) {
            runTest = value;
        }

        /**
         * Option defining whether to run the trace analysis.
         *
         * @param value the value
         */
        public void runTraceAnalysis(final boolean value) {
            runTraceAnalysis = value;
        }

        /**
         * Option defining the package parameters.
         *
         * @param closure the nested Groovy closure
         */
        public void parameters(final Runnable closure) {
            final PackageParametersContext context = new PackageParametersContext();
            executeInContext(closure, context);
            parameters = context.parameters;
        }

        /**
         * Option defining the package variables.
         *
         * @param closure the nested Groovy closure
         */
        public void outputParameters(final Runnable closure) {
            final PackageOutputParametersContext context = new PackageOutputParametersContext();
            executeInContext(closure, context);
            outputParameters = context.outputParameters;
        }

        /**
         * {@link Context} class providing the package parameters methods for the nested DSL context.
         */
        public class PackageParametersContext implements Context {

            private static final String OPT_PARAM_NAME = "parameter name";
            private static final String OPT_PARAM_VALUE = "parameter value";

            private final List<PackageParameter> parameters = new ArrayList<>();

            /**
             * Option defining the package parameter.
             *
             * @param name  the parameter name
             * @param value the parameter value
             */
            public void parameter(final CharSequence name, final CharSequence value) {
                Preconditions.checkNotNull(name, NOT_NULL_MSG, OPT_PARAM_NAME);
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
             * @param closure the nested Groovy closure
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
                 * @param value the value
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
                 * @param value the value
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

        /**
         * {@link Context} class providing the package output parameters methods for the nested DSL context.
         */
        public class PackageOutputParametersContext implements Context {

            private static final String OPT_OUT_PARAM_NAME = "output parameter name";

            private final List<PackageOutputParameter> outputParameters = new ArrayList<>();

            /**
             * Option defining the package output parameter.
             *
             * @param name the output parameter name
             */
            public void outputParameters(final CharSequence name) {
                Preconditions.checkNotNull(name, NOT_NULL_MSG, OPT_OUT_PARAM_NAME);

                final FormValidation validation = validator.validatePackageOutputParameterName(name.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());

                outputParameters.add(new PackageOutputParameter(name.toString()));
            }

            /**
             * Option defining the package variables.
             *
             * @param closure the nested Groovy closure
             */
            public void outputParameters(final Runnable closure) {
                final PackageOutputParameterContext context = new PackageOutputParameterContext();
                executeInContext(closure, context);
                outputParameters.add(new PackageOutputParameter(context.name));
            }

            /**
             * {@link Context} class providing the single package parameter methods for the nested DSL context.
             */
            public class PackageOutputParameterContext implements Context {

                private String name;

                /**
                 * Option defining the package parameter name.
                 *
                 * @param value the value
                 */
                public void name(final CharSequence value) {
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_OUT_PARAM_NAME);
                    final FormValidation validation = validator.validatePackageOutputParameterName(value.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                            validation.getMessage());
                    name = value.toString();
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
         * @param closure the nested Groovy closure
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
         * @param value the value
         */
        public void execInCurrentPkgDir(final boolean value) {
            execInCurrentPkgDir = value;
        }

        /**
         * Option defining the project filter expression.
         *
         * @param value the value
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
         * @param value the value as String
         */
        public void jobExecutionMode(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_JOB_EXEC_MODE);
            jobExecutionMode = JobExecutionMode.valueOf(value.toString());
        }

        /**
         * Option defining the job execution mode.
         *
         * @param value the value as Integer
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
         * @param value the value
         */
        public void scanMode(final CharSequence value) {
            scanMode = ScanMode.valueOf(value.toString());
        }

        /**
         * Option defining whether to scan recursively.
         *
         * @param value the value
         */
        public void recursiveScan(final boolean value) {
            recursiveScan = value;
        }

        /**
         * Option defining the package configuration.
         *
         * @param closure the nested Groovy closure
         */
        public void packageConfig(final Runnable closure) {
            final PackageConfigContext context = new PackageConfigContext();
            executeInContext(closure, context);
            packageConfig = new PackageConfig(context.runTest, context.runTraceAnalysis, context.parameters,
                context.outputParameters);
        }

        /**
         * Option defining the project configuration.
         *
         * @param closure the nested Groovy closure
         */
        public void projectConfig(final Runnable closure) {
            final ProjectConfigContext context = new ProjectConfigContext();
            executeInContext(closure, context);
            projectConfig = new ProjectConfig(context.execInCurrentPkgDir, context.filterExpression,
                context.jobExecutionMode);
        }
    }

    /**
     * {@link Context} class providing import package methods for the nested DSL context.
     */
    public class ImportPackageContext extends AbstractImportContext {

        private static final String OPT_PACKAGE_PATH = "packagePath";
        private static final String OPT_PACKAGE_DIR_PATH = "packageDirPath";

        /**
         * Option defining the import package from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param importPath    the import path
         * @param timeout       the import timeout
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                  final CharSequence importPath, final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            Preconditions.checkNotNull(importPath, NOT_NULL_MSG, OPT_IMPORT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            FormValidation validation = tmsValidator.validateTestPath(packagePath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateImportPath(importPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportPackageConfig(packagePath.toString(), importPath.toString(),
                credentialsId.toString(), timeout.toString()));
        }

        /**
         * Option defining the import package from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param importPath    the import path
         * @param timeout       the import timeout
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                  final CharSequence importPath, final int timeout) {
            importFromTMS(credentialsId, packagePath, importPath, String.valueOf(timeout));
        }

        /**
         * Option defining the import package from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param closure       the nested Groovy closure
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                  final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            final ImportTMSContext context = new ImportTMSContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportPackageConfig(packagePath.toString(), context.importPath,
                credentialsId.toString(), context.timeout));
        }

        /**
         * Option defining the import package directory from test management system configuration.
         *
         * @param credentialsId  the credentials id
         * @param packageDirPath the package directory path
         * @param importPath     the import path
         * @param timeout        the import timeout
         */
        public void importFromTMSDir(final CharSequence credentialsId, final CharSequence packageDirPath,
                                     final CharSequence importPath, final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packageDirPath, NOT_NULL_MSG, OPT_PACKAGE_DIR_PATH);
            Preconditions.checkNotNull(importPath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_PACKAGE_PATH);

            FormValidation validation = tmsValidator.validateTestPath(packageDirPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateImportPath(importPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportPackageDirConfig(packageDirPath.toString(), importPath.toString(),
                credentialsId.toString(), timeout.toString()));
        }

        /**
         * Option defining the import package directory from test management system configuration.
         *
         * @param credentialsId  the credentials id
         * @param packageDirPath the package directory path
         * @param importPath     the import path
         * @param timeout        the import timeout
         */
        public void importFromTMSDir(final CharSequence credentialsId, final CharSequence packageDirPath,
                                     final CharSequence importPath, final int timeout) {
            importFromTMSDir(credentialsId, packageDirPath, importPath, String.valueOf(timeout));
        }

        /**
         * Option defining the import package directory from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param closure       the nested Groovy closure
         */
        public void importFromTMSDir(final CharSequence credentialsId, final CharSequence packagePath,
                                     final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_DIR_PATH);
            final ImportTMSContext context = new ImportTMSContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportPackageDirConfig(packagePath.toString(), context.importPath,
                credentialsId.toString(), context.timeout));
        }

        /**
         * Option defining the import package attributes from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param timeout       the export timeout
         */
        public void importAttributesFromTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                            final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            final FormValidation validation = tmsValidator.validatePackageFile(packagePath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportPackageAttributeConfig(packagePath.toString(), credentialsId.toString(),
                timeout.toString()));
        }

        /**
         * Option defining the import package attributes from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param timeout       the export timeout
         */
        public void importAttributesFromTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                            final int timeout) {
            importAttributesFromTMS(credentialsId, packagePath, String.valueOf(timeout));
        }

        /**
         * Option defining the import package attributes from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param closure       the nested Groovy closure
         */
        public void importAttributesFromTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                            final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            final ImportTMSContext context = new ImportTMSContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportPackageAttributeConfig(packagePath.toString(), credentialsId.toString(),
                context.timeout));
        }
    }

    /**
     * {@link Context} class providing import project methods for the nested DSL context.
     */
    public class ImportProjectContext extends AbstractImportContext {

        private static final String OPT_ARCHIVE_PATH = "archivePath";
        private static final String OPT_PROJECT_PATH = "projectPath";
        private static final String OPT_PROJECT_DIR_PATH = "projectDirPath";
        private static final String OPT_IMPORT_CONFIG_PATH = "importConfigPath";

        /**
         * Option defining the import project from archive configuration.
         *
         * @param archivePath      the archive path
         * @param importPath       the import path
         * @param importConfigPath the import configuration path
         * @param replaceFiles     specifies whether to replace files
         */
        public void importFromArchive(final CharSequence archivePath, final CharSequence importPath,
                                      final CharSequence importConfigPath, final boolean replaceFiles) {
            Preconditions.checkNotNull(archivePath, NOT_NULL_MSG, OPT_ARCHIVE_PATH);
            Preconditions.checkNotNull(importPath, NOT_NULL_MSG, OPT_IMPORT_PATH);
            Preconditions.checkNotNull(importConfigPath, NOT_NULL_MSG, OPT_IMPORT_CONFIG_PATH);

            FormValidation validation = tmsValidator.validateArchivePath(archivePath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateImportPath(importPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateImportConfigPath(importConfigPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportProjectArchiveConfig(archivePath.toString(), importPath.toString(),
                importConfigPath.toString(), replaceFiles));
        }

        /**
         * Option defining the import project from archive configuration.
         *
         * @param archivePath the archive path
         * @param closure     the nested Groovy closure
         */
        public void importFromArchive(final CharSequence archivePath, final Runnable closure) {
            Preconditions.checkNotNull(archivePath, NOT_NULL_MSG, OPT_ARCHIVE_PATH);
            final ImportArchiveContext context = new ImportArchiveContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportProjectArchiveConfig(archivePath.toString(), context.importPath,
                context.importConfigPath, context.replaceFiles));
        }

        /**
         * Option defining the import project from test management system configuration.
         *
         * @param credentialsId         the credentials id
         * @param projectPath           the project path
         * @param importPath            the import path
         * @param importMissingPackages specifies whether to import missing packages
         * @param timeout               the import timeout
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                  final CharSequence importPath, final boolean importMissingPackages,
                                  final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            Preconditions.checkNotNull(importPath, NOT_NULL_MSG, OPT_IMPORT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            FormValidation validation = tmsValidator.validateTestPath(projectPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateImportPath(importPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportProjectConfig(projectPath.toString(), importPath.toString(),
                importMissingPackages, credentialsId.toString(), timeout.toString()));
        }

        /**
         * Option defining the import project from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param importPath    the import path
         * @param timeout       the import timeout
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                  final CharSequence importPath, final int timeout) {
            importFromTMS(credentialsId, projectPath, importPath, false, String.valueOf(timeout));
        }

        /**
         * Option defining the import project from test management system configuration.
         *
         * @param credentialsId         the credentials id
         * @param projectPath           the project path
         * @param importPath            the import path
         * @param importMissingPackages specifies whether to import missing packages
         * @param timeout               the import timeout
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                  final CharSequence importPath, final boolean importMissingPackages,
                                  final int timeout) {
            importFromTMS(credentialsId, projectPath, importPath, importMissingPackages, String.valueOf(timeout));
        }

        /**
         * Option defining the import project from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param closure       the nested Groovy closure
         */
        public void importFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                  final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            final ImportTMSContext context = new ImportTMSContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportProjectConfig(projectPath.toString(), context.importPath,
                context.importMissingPackages, credentialsId.toString(), context.timeout));
        }

        /**
         * Option defining the import project directory from test management system configuration.
         *
         * @param credentialsId  the credentials id
         * @param projectDirPath the project directory path
         * @param importPath     the import path
         * @param timeout        the import timeout
         */
        public void importFromTMSDir(final CharSequence credentialsId, final CharSequence projectDirPath,
                                     final CharSequence importPath, final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectDirPath, NOT_NULL_MSG, OPT_PROJECT_DIR_PATH);
            Preconditions.checkNotNull(importPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_PROJECT_PATH);

            FormValidation validation = tmsValidator.validateTestPath(projectDirPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateImportPath(importPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportProjectDirConfig(projectDirPath.toString(), importPath.toString(),
                credentialsId.toString(), timeout.toString()));
        }

        /**
         * Option defining the import project directory from test management system configuration.
         *
         * @param credentialsId  the credentials id
         * @param projectDirPath the project directory path
         * @param importPath     the import path
         * @param timeout        the import timeout
         */
        public void importFromTMSDir(final CharSequence credentialsId, final CharSequence projectDirPath,
                                     final CharSequence importPath, final int timeout) {
            importFromTMSDir(credentialsId, projectDirPath, importPath, String.valueOf(timeout));
        }

        /**
         * Option defining the import project directory from test management system configuration.
         *
         * @param credentialsId  the credentials id
         * @param projectDirPath the project path
         * @param closure        the nested Groovy closure
         */
        public void importFromTMSDir(final CharSequence credentialsId, final CharSequence projectDirPath,
                                     final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectDirPath, NOT_NULL_MSG, OPT_PROJECT_DIR_PATH);
            final ImportTMSContext context = new ImportTMSContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportProjectDirConfig(projectDirPath.toString(), context.importPath,
                credentialsId.toString(), context.timeout));
        }

        /**
         * Option defining the import project attributes from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param timeout       the export timeout
         */
        public void importAttributesFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                            final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            final FormValidation validation = tmsValidator.validateProjectFile(projectPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            importConfigs.add(new ImportProjectAttributeConfig(projectPath.toString(), credentialsId.toString(),
                timeout.toString()));
        }

        /**
         * Option defining the import project attributes from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param timeout       the export timeout
         */
        public void importAttributesFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                            final int timeout) {
            importAttributesFromTMS(credentialsId, projectPath, String.valueOf(timeout));
        }

        /**
         * Option defining the import project attributes from test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param closure       the nested Groovy closure
         */
        public void importAttributesFromTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                            final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            final ImportTMSContext context = new ImportTMSContext();
            executeInContext(closure, context);
            importConfigs.add(new ImportProjectAttributeConfig(projectPath.toString(), credentialsId.toString(),
                context.timeout));
        }

        /**
         * {@link Context} class providing the import from archive methods for the nested DSL context.
         */
        public class ImportArchiveContext extends AbstractImportContext {

            private String importConfigPath;
            private boolean replaceFiles = true;

            /**
             * Option defining the import configuration path.
             *
             * @param value the value
             */
            public void importConfigPath(final String value) {
                final FormValidation validation = tmsValidator.validateImportConfigPath(value);
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                importConfigPath = value;
            }

            /**
             * Option defining whether to replace files.
             *
             * @param value the value
             */
            public void replaceFiles(final boolean value) {
                replaceFiles = value;
            }
        }
    }

    /**
     * {@link Context} class providing export package methods for the nested DSL context.
     */
    public class ExportPackageContext extends AbstractExportContext {

        private static final String OPT_PACKAGE_PATH = "packagePath";

        /**
         * Option defining the export package to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param exportPath    the export path
         * @param createNewPath specifies whether missing export path will be created
         * @param timeout       the export timeout
         */
        public void exportToTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                final CharSequence exportPath, final boolean createNewPath,
                                final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            Preconditions.checkNotNull(exportPath, NOT_NULL_MSG, OPT_EXPORT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            FormValidation validation = tmsValidator.validatePackageFile(packagePath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateExportPath(exportPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            exportConfigs.add(new ExportPackageConfig(packagePath.toString(), exportPath.toString(), createNewPath,
                credentialsId.toString(), timeout.toString()));
        }

        /**
         * Option defining the export package to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param exportPath    the export path
         * @param timeout       the export timeout
         */
        public void exportToTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                final CharSequence exportPath, final int timeout) {
            exportToTMS(credentialsId, packagePath, exportPath, false, String.valueOf(timeout));
        }

        /**
         * Option defining the export package to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param closure       the nested Groovy closure
         */
        public void exportToTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            final ExportTMSContext context = new ExportTMSContext();
            executeInContext(closure, context);
            exportConfigs.add(new ExportPackageConfig(packagePath.toString(), context.exportPath,
                context.createNewPath, credentialsId.toString(), context.timeout));
        }

        /**
         * Option defining the export package attributes to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param timeout       the export timeout
         */
        public void exportAttributesToTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                          final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            final FormValidation validation = tmsValidator.validatePackageFile(packagePath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            exportConfigs.add(new ExportPackageAttributeConfig(packagePath.toString(), credentialsId.toString(),
                timeout.toString()));
        }

        /**
         * Option defining the export package attributes to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param timeout       the export timeout
         */
        public void exportAttributesToTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                          final int timeout) {
            exportAttributesToTMS(credentialsId, packagePath, String.valueOf(timeout));
        }

        /**
         * Option defining the export package attributes to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param packagePath   the package path
         * @param closure       the nested Groovy closure
         */
        public void exportAttributesToTMS(final CharSequence credentialsId, final CharSequence packagePath,
                                          final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(packagePath, NOT_NULL_MSG, OPT_PACKAGE_PATH);
            final ExportTMSContext context = new ExportTMSContext();
            executeInContext(closure, context);
            exportConfigs.add(new ExportPackageAttributeConfig(packagePath.toString(), credentialsId.toString(),
                context.timeout));
        }
    }

    /**
     * {@link Context} class providing export project methods for the nested DSL context.
     */
    public class ExportProjectContext extends AbstractExportContext {

        private static final String OPT_PROJECT_PATH = "projectPath";

        /**
         * Option defining the export project to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param exportPath    the export path
         * @param createNewPath specifies whether missing export path will be created
         * @param timeout       the export timeout
         */
        public void exportToTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                final CharSequence exportPath, final boolean createNewPath,
                                final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            Preconditions.checkNotNull(exportPath, NOT_NULL_MSG, OPT_EXPORT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            FormValidation validation = validator.validateProjectFile(projectPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            validation = tmsValidator.validateExportPath(exportPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            exportConfigs.add(new ExportProjectConfig(projectPath.toString(), exportPath.toString(), createNewPath,
                credentialsId.toString(), timeout.toString()));
        }

        /**
         * Option defining the export project to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param exportPath    the export path
         * @param timeout       the export timeout
         */
        public void exportToTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                final CharSequence exportPath, final int timeout) {
            exportToTMS(credentialsId, projectPath, exportPath, false, String.valueOf(timeout));
        }

        /**
         * Option defining the export package to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param closure       the nested Groovy closure
         */
        public void exportToTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            final ExportTMSContext context = new ExportTMSContext();
            executeInContext(closure, context);
            exportConfigs.add(new ExportProjectConfig(projectPath.toString(), context.exportPath,
                context.createNewPath, credentialsId.toString(), context.timeout));
        }

        /**
         * Option defining the export project attributes to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param timeout       the export timeout
         */
        public void exportAttributesToTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                          final CharSequence timeout) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            Preconditions.checkNotNull(timeout, NOT_NULL_MSG, OPT_TIMEOUT);

            final FormValidation validation = tmsValidator.validateProjectFile(projectPath.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            exportConfigs.add(new ExportProjectAttributeConfig(projectPath.toString(), credentialsId.toString(),
                timeout.toString()));
        }

        /**
         * Option defining the export project attributes to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param timeout       the export timeout
         */
        public void exportAttributesToTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                          final int timeout) {
            exportAttributesToTMS(credentialsId, projectPath, String.valueOf(timeout));
        }

        /**
         * Option defining the export project attributes to test management system configuration.
         *
         * @param credentialsId the credentials id
         * @param projectPath   the project path
         * @param closure       the nested Groovy closure
         */
        public void exportAttributesToTMS(final CharSequence credentialsId, final CharSequence projectPath,
                                          final Runnable closure) {
            Preconditions.checkNotNull(credentialsId, NOT_NULL_MSG, OPT_CREDENTIALS_ID);
            Preconditions.checkNotNull(projectPath, NOT_NULL_MSG, OPT_PROJECT_PATH);
            final ExportTMSContext context = new ExportTMSContext();
            executeInContext(closure, context);
            exportConfigs.add(new ExportProjectAttributeConfig(projectPath.toString(), credentialsId.toString(),
                context.timeout));
        }
    }
}
