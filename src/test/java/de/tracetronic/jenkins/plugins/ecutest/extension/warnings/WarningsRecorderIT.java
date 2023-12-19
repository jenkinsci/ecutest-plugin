/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.warnings;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Integration tests for {@link WarningsRecorder}.
 */
public class WarningsRecorderIT extends IntegrationTestBase {

    @Test
    public void recordIssues() throws Exception {
        final String issueFileName = "issues.json";
        final String issues = loadTestResource(issueFileName);

        final FreeStyleProject project = createProject(issueFileName, issues);
        final FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.SUCCESS, project);
        jenkins.assertLogContains("found 1 file", build);
        jenkins.assertLogContains("found 3 issues", build);

        assertThat(build.getAction(ResultAction.class).getResult().getIssues().size(), is(3));
    }

    @Test
    public void recordNoIssues() throws Exception {
        final String issueFileName = "no-issues.json";
        final String issues = loadTestResource(issueFileName);

        final FreeStyleProject project = createProject(issueFileName, issues);
        final FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.SUCCESS, project);
        jenkins.assertLogContains("found 1 file", build);
        jenkins.assertLogContains("found 0 issues", build);
        jenkins.assertLogContains("No issues found, empty result will be removed from build!", build);

        assertThat(build.getAction(ResultAction.class), is(nullValue()));
    }

    @Test
    public void recordError() throws Exception {
        final String issueFileName = "error.json";
        final String issues = loadTestResource(issueFileName);

        final FreeStyleProject project = createProject(issueFileName, issues);
        final FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains("found 1 file", build);
        jenkins.assertLogContains("found 1 issue", build);

        assertThat(build.getAction(ResultAction.class).getResult().getTotalErrorsSize(), is(1));
    }

    private FreeStyleProject createProject(final String issueFileName, final String issues) throws IOException {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) throws InterruptedException, IOException {

                Objects.requireNonNull(build.getWorkspace()).child(issueFileName).write(issues, "UTF-8");
                WarningsRecorder recorder = new WarningsRecorder("Test", "test", issueFileName);
                return !recorder.record(build, build.getWorkspace(), launcher, listener);
            }
        });
        return project;
    }
}
