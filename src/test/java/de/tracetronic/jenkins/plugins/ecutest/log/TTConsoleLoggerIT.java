/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.log;

import org.htmlunit.html.HtmlPage;
import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Test;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link TTConsoleLogger}.
 */
public class TTConsoleLoggerIT extends IntegrationTestBase {

    @Test
    public void testPlainTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.log("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Plain text log output should be present in build console log",
            consoleLog.asNormalizedText().contains("TTConsoleLogger"));
    }

    @Test
    public void testInfoTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logInfo("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated info log output should be present in build console log",
            consoleLog.asNormalizedText().contains("[TT] INFO: TTConsoleLogger"));
    }

    @Test
    public void testWarnTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logWarn("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated warn log output should be present in build console log",
            consoleLog.asNormalizedText().contains("[TT] WARN: TTConsoleLogger"));
    }

    @Test
    public void testErrorTextLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logError("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated error log output should be present in build console log",
            consoleLog.asNormalizedText().contains("[TT] ERROR: TTConsoleLogger"));
    }

    @Test
    public void testComExceptionLogger() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logComException(new ETComException("TTConsoleLogger"));
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        assertTrue("Annotated error log output should be present in build console log",
            consoleLog.asNormalizedText().contains("[TT] ERROR: Caught ComException: TTConsoleLogger"));
    }

    @Test
    public void testEnabledDebugTextLogger() throws Exception {
        System.setProperty("ecutest.debugLog", "true");
        final HtmlPage consoleLog = logDebug();
        assertTrue("Annotated debug log output should be present in build console log",
            consoleLog.asNormalizedText().contains("[TT] DEBUG: TTConsoleLogger"));
    }

    @Test
    public void testDisabledDebugTextLogger() throws Exception {
        System.setProperty("ecutest.debugLog", "false");
        final HtmlPage consoleLog = logDebug();
        assertFalse("Annotated debug log output should NOT be present in build console log",
            consoleLog.asNormalizedText().contains("[TT] DEBUG: TTConsoleLogger"));
    }

    private HtmlPage logDebug() throws Exception {
        final FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                                   final BuildListener listener) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logDebug("TTConsoleLogger");
                return true;
            }
        });

        final FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        final HtmlPage consoleLog = getWebClient().getPage(build, "console");
        return consoleLog;
    }
}
