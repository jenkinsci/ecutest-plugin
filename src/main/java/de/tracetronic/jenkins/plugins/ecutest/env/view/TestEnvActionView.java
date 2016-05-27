/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.listeners.RunListener;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;

/**
 * Shows the test related environment variables on a build as an action.
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
     * @param run
     *            the build
     * @param listener
     *            the listener
     */
    public TestEnvActionView(final Run<?, ?> run, final TaskListener listener) {
        super();
        build = run;
        this.listener = listener;
    }

    /**
     * Gets the test related build environment variables from the {@link TestEnvInvisibleAction} previously added to the
     * build.
     *
     * @return set of {@link ParameterValue}'s to show in the build page.
     */
    public Set<ParameterValue> getEnvVariables() {
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        final int testBuilderSize = testEnvActions.size();
        final Set<ParameterValue> testEnvVars = new LinkedHashSet<ParameterValue>();

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
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } catch (final InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return testEnvVars;
    }

    /**
     * Listener notifying the build on completion and adding this {@link TestEnvActionView} as a new build action.
     */
    @Extension
    public static final class RunListenerImpl extends RunListener<Run<?, ?>> {

        @Override
        public void onCompleted(final Run<?, ?> run, final TaskListener listener) {
            if (run.getAction(TestEnvInvisibleAction.class) != null) {
                run.addAction(new TestEnvActionView(run, listener));
            }
        }
    }
}
