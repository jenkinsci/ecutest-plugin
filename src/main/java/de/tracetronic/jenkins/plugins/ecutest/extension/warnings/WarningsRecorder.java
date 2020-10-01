/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.warnings;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.util.RunResultHandler;
import io.jenkins.plugins.analysis.core.util.StageResultHandler;
import io.jenkins.plugins.analysis.warnings.WarningsPlugin;
import org.apache.commons.lang.RandomStringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;

/**
 * Class for recording test file checks as Warnings NG issues.
 */
public class WarningsRecorder {

    private final String displayName;
    private final String issueName;
    private final String issueFileName;

    /**
     * Instantiates a new {@link WarningsRecorder}.
     *
     * @param displayName   the display name for all recorded issues
     * @param issueName     the name for generating the issue id
     * @param issueFileName the file name pattern to search for issues
     */
    public WarningsRecorder(final String displayName, final String issueName, final String issueFileName) {
        this.displayName = displayName;
        this.issueName = issueName;
        this.issueFileName = issueFileName;
    }

    /**
     * Records test file checks as Warnings NG issues.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if recording detects any issues with error severity, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public boolean record(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                          final TaskListener listener) throws IOException, InterruptedException {
        boolean hasIssues = false;

        final WarningsPlugin plugin = new WarningsPlugin();
        plugin.setName(displayName);
        plugin.setPattern(issueFileName);
        plugin.setReportEncoding("UTF-8");
        plugin.setId(String.format("%s-%s", issueName, RandomStringUtils.randomAlphanumeric(8)));

        final IssuesRecorder recorder = new IssuesRecorder();
        recorder.setTools(plugin);
        // Prevent to fail the build due to missing fingerprints
        recorder.setFailOnError(false);
        recorder.setEnabledForFailure(true);
        recorder.setMinimumSeverity("ERROR");

        if (run instanceof AbstractBuild) {
            recorder.perform((AbstractBuild<?, ?>) run, launcher, (BuildListener) listener);
        } else {
            // Using reflection because required perform method for WorkflowRun is package-protected only!
            final Class<? extends IssuesRecorder> clazz = recorder.getClass();
            try {
                final Method perform = clazz.getDeclaredMethod("perform",
                    Run.class, FilePath.class, TaskListener.class, StageResultHandler.class);
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    perform.setAccessible(true);
                    return null;
                });
                perform.invoke(recorder, run, workspace, listener, new RunResultHandler(run));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logError(String.format("Could not invoke Warnings NG recorder: %s", e.getMessage()));
                return true;
            }
        }

        // Check for issues with ERROR severity and stop further execution if any
        final Optional<ResultAction> result = run.getActions(ResultAction.class).stream().filter(action ->
            action.getId().equals(plugin.getId())).findFirst();
        if (result.isPresent() && result.get().getResult().getIssues().getSizeOf("ERROR") > 0) {
            run.setResult(Result.FAILURE);
            hasIssues = true;
        }

        return hasIssues;
    }
}
