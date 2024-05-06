/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import org.htmlunit.WebAssert;
import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.model.FreeStyleProject;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.workflow.steps.CoreStep;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Integration tests for {@link DownStreamPublisher}.
 */
public class DownStreamPublisherIT extends IntegrationTestBase {

    @Test
    public void testDefaultConfigRoundTripStep() throws Exception {
        final DownStreamPublisher before = new DownStreamPublisher("", "");

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(DownStreamPublisher.class));

        final DownStreamPublisher after = (DownStreamPublisher) delegate;
        jenkins.assertEqualDataBoundBeans(before, after);
    }

    @Test
    public void testConfigRoundTripStep() throws Exception {
        final DownStreamPublisher before = new DownStreamPublisher(null, null);

        CoreStep step = new CoreStep(before);
        step = new StepConfigTester(jenkins).configRoundTrip(step);
        final SimpleBuildStep delegate = step.delegate;
        assertThat(delegate, instanceOf(DownStreamPublisher.class));

        final DownStreamPublisher after = (DownStreamPublisher) delegate;
        jenkins.assertEqualBeans(before, after, "workspace,reportDir");
    }

    @Test
    public void testConfigView() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        final DownStreamPublisher publisher = new DownStreamPublisher("W:\\ork\\space", "MyReportFolder");
        project.getPublishersList().add(publisher);

        final HtmlPage page = getWebClient().getPage(project, "configure");
        WebAssert.assertTextPresent(page, Messages.DownStreamPublisher_DisplayName());
        jenkins.assertXPath(page, "//input[@name='_.workspace' and @value='W:\\ork\\space']");
        jenkins.assertXPath(page, "//input[@name='_.reportDir' and @value='MyReportFolder']");
    }
}
