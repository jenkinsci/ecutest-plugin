/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env.view;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.InvisibleAction;
import hudson.model.ParameterValue;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shows the test related environment variables as an build action.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestEnvActionView extends InvisibleAction {

    private static final Logger LOGGER = Logger.getLogger(TestEnvActionView.class.getName());

    private final Run<?, ?> build;
    private final transient TaskListener listener;

    /**
     * Instantiates a new {@link TestEnvActionView}.
     *
     * @param run      the build
     * @param listener the listener
     */
    public TestEnvActionView(final Run<?, ?> run, final TaskListener listener) {
        super();
        build = run;
        this.listener = listener;
    }

    /**
     * Gets the test related build environment variables from the {@link TestEnvInvisibleAction}
     * previously added to the build.
     *
     * @return set of {@link ParameterValue}'s to show in the build page.
     */
    public Set<ParameterValue> getEnvVariables() {
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        final int testBuilderSize = testEnvActions.size();
        final Set<ParameterValue> testEnvVars = new LinkedHashSet<>();

        try {
            final EnvVars envVars = build.getEnvironment(listener);
            String buildWs = "";
            final String buildWsPath = envVars.get("WORKSPACE");
            if (buildWsPath != null) {
                buildWs = buildWsPath + File.separator;
            }

            for (int i = 0; i < testBuilderSize; i++) {
                for (final Entry<String, String> entry : envVars.entrySet()) {
                    if (entry.getKey().startsWith("TT_TEST") && entry.getKey().endsWith(String.valueOf(i))) {
                        final String value = entry.getValue().replace(buildWs, "");
                        testEnvVars.add(new StringParameterValue(entry.getKey(), value));
                    }
                }
            }
        } catch (final IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return testEnvVars;
    }

    /**
     * Gets the list of {@link TestEnvInvisibleAction}s according to this build action.
     *
     * @return the test environment actions
     */
    public List<TestEnvInvisibleAction> getTestEnvActions() {
        return build.getActions(TestEnvInvisibleAction.class);
    }

    /**
     * Gets the file name of the icon.
     *
     * @return the icon file name
     */
    public String getIconFile() {
        return ETPlugin.getIconFileName("icon-ecutest-test-param", "icon-xlg");
    }

    /**
     * Listener notifying the build on completion and adding this {@link TestEnvActionView} as a new build action.
     */
    @Extension
    public static final class RunListenerImpl extends RunListener<Run<?, ?>> {

        @Override
        public void onCompleted(final Run<?, ?> run, @Nonnull final TaskListener listener) {
            if (run.getAction(TestEnvInvisibleAction.class) != null) {
                run.addAction(new TestEnvActionView(run, listener));
            }
        }
    }
}
