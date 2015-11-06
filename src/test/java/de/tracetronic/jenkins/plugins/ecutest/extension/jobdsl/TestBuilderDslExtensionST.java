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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.util.DescribableList;

import java.util.List;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.test.TestFolderBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestPackageBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.TestProjectBuilder;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.GlobalConstant;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.PackageParameter;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;

/**
 * System tests for {@link TestBuilderDslExtension}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestBuilderDslExtensionST extends AbstractDslExtensionST {

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
        assertThat("Test related build steps should exist", builders, hasSize(3));
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

    private void testConfigWithDsl(final TestConfig config) throws Exception {
        assertNotNull("Test configuration should exist", config);
        assertThat(config.getTbcFile(), is("test.tbc"));
        assertThat(config.getTcfFile(), is("test.tcf"));
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
        assertThat(config.getTimeout(), is(600));
        assertFalse(config.isStopOnError());
    }

    private void testProjectConfigWithDsl(final ProjectConfig config) throws Exception {
        assertNotNull("Project configuration should exist", config);
        assertFalse(config.isExecInCurrentPkgDir());
        assertThat(config.getFilterExpression(), is("Name='TestCase'"));
        assertThat(config.getJobExecMode(), is(JobExecutionMode.PARALLEL_EXECUTION));
    }
}
