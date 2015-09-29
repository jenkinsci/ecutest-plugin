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
package de.tracetronic.jenkins.plugins.ecutest.tool;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

/**
 * Builder providing to stop ECU-TEST.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class StopETBuilder extends AbstractToolBuilder {

    /**
     * Defines the default timeout to stop ECU-TEST.
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Instantiates a new {@link StopETBuilder}.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param timeout
     *            the timeout
     */
    @DataBoundConstructor
    public StopETBuilder(final String toolName, final String timeout) {
        super(toolName, StringUtils.defaultIfEmpty(timeout, String.valueOf(DEFAULT_TIMEOUT)));
    }

    /**
     * @return the default timeout
     */
    public int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        // Check OS running this build
        if (!ProcessUtil.checkOS(launcher, listener)) {
            return false;
        }

        // Expand build parameters
        final EnvVars buildEnvVars = build.getEnvironment(listener);
        final int expandedTimeout = Integer.parseInt(EnvUtil.expandEnvVar(getTimeout(), buildEnvVars,
                String.valueOf(DEFAULT_TIMEOUT)));

        // Get selected ECU-TEST installation
        final AbstractToolInstallation installation = configureToolInstallation(listener,
                build.getEnvironment(listener));

        // Stop selected ECU-TEST
        if (installation instanceof ETInstallation) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logInfo(String.format("Stopping %s...", getToolName()));
            final ETClient etClient = new ETClient(getToolName(), expandedTimeout);
            if (etClient.stop(true, launcher, listener)) {
                logger.logInfo(String.format("%s stopped successfully.", getToolName()));
            } else {
                logger.logError(String.format("Stopping %s failed.", getToolName()));
                return false;
            }
        } else {
            throw new AbortException(de.tracetronic.jenkins.plugins.ecutest.Messages.ET_NoInstallation());
        }

        return true;
    }

    /**
     * DescriptorImpl for {@link StopETBuilder}.
     */
    @Extension(ordinal = 1005)
    public static final class DescriptorImpl extends AbstractToolDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(StopETBuilder.class);
            load();
        }

        @Override
        public int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Override
        public String getDisplayName() {
            return Messages.StopETBuilder_DisplayName();
        }
    }
}
