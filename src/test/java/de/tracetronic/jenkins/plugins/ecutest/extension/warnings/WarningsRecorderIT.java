/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
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
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.Objects;

/**
 * Integration tests for {@link WarningsRecorder}.
 */
public class WarningsRecorderIT extends IntegrationTestBase {

    @Test
    public void recordWarnings() throws Exception {
        final String issueFileName = "issues.json";
        final String issues = loadTestResource(issueFileName);

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

        final FreeStyleBuild build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains("found 1 file", build);
        jenkins.assertLogContains("found 3 issues", build);
    }
}
