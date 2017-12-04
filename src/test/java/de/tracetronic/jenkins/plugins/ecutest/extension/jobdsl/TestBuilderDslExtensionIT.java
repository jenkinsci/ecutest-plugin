/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

import java.util.List;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.test.ExportPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.ExportProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.ImportPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.ImportProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestFolderBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.GlobalConstant;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectDirConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * Integration tests for {@link TestBuilderDslExtension}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestBuilderDslExtensionIT extends AbstractDslExtensionIT {

    public static final String JOB_NAME = "testBuilder";
    public static final String SCRIPT_NAME = "testBuilder.groovy";

    @Override
    protected String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected String getDslScript() {
        return SCRIPT_NAME;
    }

    @Test
    public void testBuildersWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final List<Builder> builders = project.getBuilders();
        assertThat("Test related build steps should exist", builders, hasSize(7));
    }

    @Test
    public void testPackageWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final TestPackageBuilder builder = builders.get(TestPackageBuilder.class);
        assertNotNull("Test package builder should exist", builder);
        assertThat(builder.getTestFile(), is("test.pkg"));
        testConfigWithDsl(builder.getTestConfig());
        testPackageConfigWithDsl(builder.getPackageConfig());
        testExecutionConfigWithDsl(builder.getExecutionConfig());
    }

    @Test
    public void testProjectWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final TestProjectBuilder builder = builders.get(TestProjectBuilder.class);
        assertNotNull("Test project builder should exist", builder);
        assertThat(builder.getTestFile(), is("test.prj"));
        testConfigWithDsl(builder.getTestConfig());
        testProjectConfigWithDsl(builder.getProjectConfig());
        testExecutionConfigWithDsl(builder.getExecutionConfig());
    }

    @Test
    public void testFolderWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final TestFolderBuilder builder = builders.get(TestFolderBuilder.class);
        assertNotNull("Test folder builder should exist", builder);
        assertThat(builder.getTestFile(), is("test"));
        testConfigWithDsl(builder.getTestConfig());
        testPackageConfigWithDsl(builder.getPackageConfig());
        testProjectConfigWithDsl(builder.getProjectConfig());
        testExecutionConfigWithDsl(builder.getExecutionConfig());
    }

    @Test
    public void testImportPackageWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final ImportPackageBuilder builder = builders.get(ImportPackageBuilder.class);
        assertNotNull("Import package builder should exist", builder);
        testImportPackageConfigsWithDsl(builder.getImportConfigs());
    }

    @Test
    public void testImportProjectWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final ImportProjectBuilder builder = builders.get(ImportProjectBuilder.class);
        assertNotNull("Import project builder should exist", builder);
        testImportProjectConfigsWithDsl(builder.getImportConfigs());
    }

    @Test
    public void testExportPackageWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final ExportPackageBuilder builder = builders.get(ExportPackageBuilder.class);
        assertNotNull("Export package builder should exist", builder);
        testExportPackageConfigsWithDsl(builder.getExportConfigs());
    }

    @Test
    public void testExportProjectWithDsl() throws Exception {
        final FreeStyleProject project = createTestJob();

        final DescribableList<Builder, Descriptor<Builder>> builders = project.getBuildersList();
        final ExportProjectBuilder builder = builders.get(ExportProjectBuilder.class);
        assertNotNull("Export project builder should exist", builder);
        testExportProjectConfigsWithDsl(builder.getExportConfigs());
    }

    private void testConfigWithDsl(final TestConfig config) throws Exception {
        assertNotNull("Test configuration should exist", config);
        assertThat(config.getTbcFile(), is("test.tbc"));
        assertThat(config.getTcfFile(), is("test.tcf"));
        assertTrue(config.isForceReload());
        assertTrue(config.isLoadOnly());
        assertTrue(config.isKeepConfig());
        testGlobalConstantsWithDsl(config.getConstants());
    }

    private void testGlobalConstantsWithDsl(final List<GlobalConstant> constants) throws Exception {
        assertThat("Global constants should exist", constants, hasSize(2));
        assertThat(constants.get(0).getName(), is("test"));
        assertThat(constants.get(0).getValue(), is("123"));
        assertThat(constants.get(1).getName(), is("test2"));
        assertThat(constants.get(1).getValue(), is("456"));
    }

    private void testPackageConfigWithDsl(final PackageConfig config) throws Exception {
        assertNotNull("Package configuration should exist", config);
        assertFalse(config.isRunTest());
        assertFalse(config.isRunTraceAnalysis());
        testPackageParametersWithDsl(config.getParameters());
    }

    private void testPackageParametersWithDsl(final List<PackageParameter> list) throws Exception {
        assertThat("Package parameters should exist", list, hasSize(2));
        assertThat(list.get(0).getName(), is("param"));
        assertThat(list.get(0).getValue(), is("123"));
        assertThat(list.get(1).getName(), is("param2"));
        assertThat(list.get(1).getValue(), is("456"));
    }

    private void testExecutionConfigWithDsl(final ExecutionConfig config) throws Exception {
        assertNotNull("Execution configuration should exist", config);
        assertThat(config.getParsedTimeout(), is(600));
        assertFalse(config.isStopOnError());
        assertFalse(config.isCheckTestFile());
    }

    private void testProjectConfigWithDsl(final ProjectConfig config) throws Exception {
        assertNotNull("Project configuration should exist", config);
        assertFalse(config.isExecInCurrentPkgDir());
        assertThat(config.getFilterExpression(), is("Name='TestCase'"));
        assertThat(config.getJobExecMode(), is(JobExecutionMode.PARALLEL_EXECUTION));
    }

    private void testImportPackageConfigsWithDsl(final List<TMSConfig> list) throws Exception {
        assertThat("Package import configurations should exist", list, hasSize(6));
        assertThat((ImportPackageConfig) list.get(0), isA(ImportPackageConfig.class));
        assertThat((ImportPackageConfig) list.get(1), isA(ImportPackageConfig.class));
        assertThat((ImportPackageDirConfig) list.get(2), isA(ImportPackageDirConfig.class));
        assertThat((ImportPackageDirConfig) list.get(3), isA(ImportPackageDirConfig.class));
        assertThat((ImportPackageAttributeConfig) list.get(4), isA(ImportPackageAttributeConfig.class));
        assertThat((ImportPackageAttributeConfig) list.get(5), isA(ImportPackageAttributeConfig.class));
        testImportPackageConfigWithDsl((ImportPackageConfig) list.get(0));
        testImportPackageConfigWithDsl((ImportPackageConfig) list.get(1));
        testImportPackageDirConfigWithDsl((ImportPackageDirConfig) list.get(2));
        testImportPackageDirConfigWithDsl((ImportPackageDirConfig) list.get(3));
        testImportPackageAttributeConfigWithDsl((ImportPackageAttributeConfig) list.get(4));
        testImportPackageAttributeConfigWithDsl((ImportPackageAttributeConfig) list.get(5));
    }

    private void testImportPackageConfigWithDsl(final ImportPackageConfig config) throws Exception {
        assertThat(config.getTmsPath(), is("Subject/Test"));
        assertThat(config.getImportPath(), is("import"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testImportPackageDirConfigWithDsl(final ImportPackageDirConfig config) throws Exception {
        assertThat(config.getTmsPath(), is("Subject/TestDir"));
        assertThat(config.getImportPath(), is("import"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testImportPackageAttributeConfigWithDsl(final ImportPackageAttributeConfig config) throws Exception {
        assertThat(config.getFilePath(), is("test.pkg"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testImportProjectConfigsWithDsl(final List<TMSConfig> list) throws Exception {
        assertThat("Project import configurations should exist", list, hasSize(8));
        assertThat((ImportProjectArchiveConfig) list.get(0), isA(ImportProjectArchiveConfig.class));
        assertThat((ImportProjectArchiveConfig) list.get(1), isA(ImportProjectArchiveConfig.class));
        assertThat((ImportProjectConfig) list.get(2), isA(ImportProjectConfig.class));
        assertThat((ImportProjectConfig) list.get(3), isA(ImportProjectConfig.class));
        assertThat((ImportProjectDirConfig) list.get(4), isA(ImportProjectDirConfig.class));
        assertThat((ImportProjectDirConfig) list.get(5), isA(ImportProjectDirConfig.class));
        testImportProjectArchiveConfigWithDsl((ImportProjectArchiveConfig) list.get(0));
        testImportProjectArchiveConfigWithDsl((ImportProjectArchiveConfig) list.get(1));
        testImportProjectConfigWithDsl((ImportProjectConfig) list.get(2));
        testImportProjectConfigWithDsl((ImportProjectConfig) list.get(3));
        testImportProjectDirConfigWithDsl((ImportProjectDirConfig) list.get(4));
        testImportProjectDirConfigWithDsl((ImportProjectDirConfig) list.get(5));
        testImportProjectAttributeConfigWithDsl((ImportProjectAttributeConfig) list.get(6));
        testImportProjectAttributeConfigWithDsl((ImportProjectAttributeConfig) list.get(7));
    }

    private void testImportProjectArchiveConfigWithDsl(final ImportProjectArchiveConfig config) throws Exception {
        assertThat(config.getTmsPath(), is("test.prz"));
        assertThat(config.getImportPath(), is("import"));
        assertThat(config.getImportConfigPath(), is("import"));
        assertFalse(config.isReplaceFiles());
    }

    private void testImportProjectConfigWithDsl(final ImportProjectConfig config) throws Exception {
        assertThat(config.getTmsPath(), is("Root/Test"));
        assertThat(config.getImportPath(), is("import"));
        assertTrue(config.isImportMissingPackages());
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testImportProjectDirConfigWithDsl(final ImportProjectDirConfig config) throws Exception {
        assertThat(config.getTmsPath(), is("Root/TestDir"));
        assertThat(config.getImportPath(), is("import"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testImportProjectAttributeConfigWithDsl(final ImportProjectAttributeConfig config) throws Exception {
        assertThat(config.getFilePath(), is("test.prj"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testExportPackageConfigsWithDsl(final List<TMSConfig> list) throws Exception {
        assertThat("Package export configurations should exist", list, hasSize(4));
        assertThat((ExportPackageConfig) list.get(0), isA(ExportPackageConfig.class));
        assertThat((ExportPackageConfig) list.get(1), isA(ExportPackageConfig.class));
        assertThat((ExportPackageAttributeConfig) list.get(2), isA(ExportPackageAttributeConfig.class));
        assertThat((ExportPackageAttributeConfig) list.get(3), isA(ExportPackageAttributeConfig.class));
        testExportPackageConfigWithDsl((ExportPackageConfig) list.get(0));
        testExportPackageConfigWithDsl((ExportPackageConfig) list.get(1));
        testExportPackageAttributeConfigWithDsl((ExportPackageAttributeConfig) list.get(2));
        testExportPackageAttributeConfigWithDsl((ExportPackageAttributeConfig) list.get(3));
    }

    private void testExportPackageConfigWithDsl(final ExportPackageConfig config) throws Exception {
        assertThat(config.getFilePath(), is("test.pkg"));
        assertThat(config.getExportPath(), is("Subject/Test"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testExportPackageAttributeConfigWithDsl(final ExportPackageAttributeConfig config) throws Exception {
        assertThat(config.getFilePath(), is("test.pkg"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testExportProjectConfigsWithDsl(final List<TMSConfig> list) throws Exception {
        assertThat("Project export configurations should exist", list, hasSize(4));
        assertThat((ExportProjectConfig) list.get(0), isA(ExportProjectConfig.class));
        assertThat((ExportProjectConfig) list.get(1), isA(ExportProjectConfig.class));
        assertThat((ExportProjectAttributeConfig) list.get(2), isA(ExportProjectAttributeConfig.class));
        assertThat((ExportProjectAttributeConfig) list.get(3), isA(ExportProjectAttributeConfig.class));
        testExportProjectConfigWithDsl((ExportProjectConfig) list.get(0));
        testExportProjectConfigWithDsl((ExportProjectConfig) list.get(1));
        testExportProjectAttributeConfigWithDsl((ExportProjectAttributeConfig) list.get(2));
        testExportProjectAttributeConfigWithDsl((ExportProjectAttributeConfig) list.get(3));
    }

    private void testExportProjectConfigWithDsl(final ExportProjectConfig config) throws Exception {
        assertThat(config.getFilePath(), is("test.prj"));
        assertThat(config.getExportPath(), is("Root/Test"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }

    private void testExportProjectAttributeConfigWithDsl(final ExportProjectAttributeConfig config)
            throws Exception {
        assertThat(config.getFilePath(), is("test.prj"));
        assertThat(config.getCredentialsId(), is("credentialsId"));
        assertThat(config.getTimeout(), is("600"));
    }
}
