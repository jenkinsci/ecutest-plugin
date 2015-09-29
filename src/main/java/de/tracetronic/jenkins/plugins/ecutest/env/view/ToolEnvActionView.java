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
package de.tracetronic.jenkins.plugins.ecutest.env.view;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.InvisibleAction;
import hudson.model.ParameterValue;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.StringParameterValue;
import hudson.model.listeners.RunListener;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;

/**
 * Show the tool related environment variables on a build as an action.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolEnvActionView extends InvisibleAction {

    private static final Logger LOGGER = Logger.getLogger(ToolEnvActionView.class.getName());

    private final AbstractBuild<?, ?> build;
    private final transient TaskListener listener;

    /**
     * Instantiates a new {@link ToolEnvActionView}.
     *
     * @param build
     *            the build
     * @param listener
     *            the listener
     */
    public ToolEnvActionView(final AbstractBuild<?, ?> build, final TaskListener listener) {
        super();
        this.build = build;
        this.listener = listener;
    }

    /**
     * Gets the tool related build environment variables from the {@link ToolEnvInvisibleAction} previously added to the
     * build.
     *
     * @return set of {@link ParameterValue}'s to show in the build page.
     */
    public Set<ParameterValue> getEnvVariables() {
        final List<ToolEnvInvisibleAction> toolEnvActions = build.getActions(ToolEnvInvisibleAction.class);
        final int toolBuilderSize = toolEnvActions.size();
        final Set<ParameterValue> toolEnvVars = new LinkedHashSet<ParameterValue>();

        try {
            final EnvVars envVars = build.getEnvironment(listener);
            for (int i = 0; i < toolBuilderSize; i++) {
                for (final Entry<String, String> entry : envVars.entrySet()) {
                    if (entry.getKey().startsWith("TT_TOOL") && entry.getKey().endsWith(String.valueOf(i))) {
                        toolEnvVars.add(new StringParameterValue(entry.getKey(), entry.getValue()));
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } catch (final InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return toolEnvVars;
    }

    /**
     * Listener notifying the build on completion and adding this {@link ToolEnvInvisibleAction} as a new build action.
     */
    @Extension
    public static final class RunListenerImpl extends RunListener<AbstractBuild<?, ?>> {

        @Override
        public void onCompleted(final AbstractBuild<?, ?> run, final TaskListener listener) {
            if (run.getAction(ToolEnvInvisibleAction.class) != null) {
                run.addAction(new ToolEnvActionView(run, listener));
            }
        }
    }
}
