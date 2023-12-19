/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.env.view;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.InvisibleAction;
import hudson.model.ParameterValue;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Show the tool related environment variables as an build action.
 */
public class ToolEnvActionView extends InvisibleAction {

    private static final Logger LOGGER = Logger.getLogger(ToolEnvActionView.class.getName());

    private final Run<?, ?> build;
    private final transient TaskListener listener;

    /**
     * Instantiates a new {@link ToolEnvActionView}.
     *
     * @param run      the build
     * @param listener the listener
     */
    public ToolEnvActionView(final Run<?, ?> run, final TaskListener listener) {
        super();
        build = run;
        this.listener = listener;
    }

    /**
     * Gets the tool related build environment variables from the {@link ToolEnvInvisibleAction}
     * previously added to the build.
     *
     * @return set of {@link ParameterValue}'s to show in the build page.
     */
    public Set<ParameterValue> getEnvVariables() {
        final List<ToolEnvInvisibleAction> toolEnvActions = build.getActions(ToolEnvInvisibleAction.class);
        final int toolBuilderSize = toolEnvActions.size();
        final Set<ParameterValue> toolEnvVars = new LinkedHashSet<>();

        try {
            final EnvVars envVars = build.getEnvironment(listener);
            for (int i = 0; i < toolBuilderSize; i++) {
                for (final Entry<String, String> entry : envVars.entrySet()) {
                    if (entry.getKey().startsWith("TT_TOOL") && entry.getKey().endsWith(String.valueOf(i))) {
                        toolEnvVars.add(new StringParameterValue(entry.getKey(), entry.getValue()));
                    }
                }
            }
        } catch (final IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return toolEnvVars;
    }

    /**
     * Gets the list of {@link ToolEnvInvisibleAction}s according to this build action.
     *
     * @return the tool environment actions
     */
    public List<ToolEnvInvisibleAction> getToolEnvActions() {
        return build.getActions(ToolEnvInvisibleAction.class);
    }

    /**
     * Gets the file name of the icon.
     *
     * @return the icon file name
     */
    public String getIconFile() {
        return ETPlugin.getIconFileName("icon-ecutest-tool-param", "icon-xlg");
    }

    /**
     * Listener notifying the build on completion and adding this {@link ToolEnvInvisibleAction} as a new build action.
     */
    @Extension
    public static final class RunListenerImpl extends RunListener<Run<?, ?>> {

        @Override
        public void onCompleted(final Run<?, ?> run, @Nonnull final TaskListener listener) {
            if (run.getAction(ToolEnvInvisibleAction.class) != null) {
                run.addAction(new ToolEnvActionView(run, listener));
            }
        }
    }
}
