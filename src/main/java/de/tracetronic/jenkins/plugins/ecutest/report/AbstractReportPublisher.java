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
package de.tracetronic.jenkins.plugins.ecutest.report;

import hudson.EnvVars;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.log.ETLogPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.AbstractToolInstallation;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;

/**
 * Common base class for {@link ATXPublisher}, {@link ETLogPublisher}, {@link JUnitPublisher} and {@link TRFPublisher}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractReportPublisher extends Recorder {

    private static final Logger LOGGER = Logger.getLogger(AbstractReportPublisher.class.getName());

    private final boolean allowMissing;
    private final boolean runOnFailed;

    /**
     * Instantiates a new {@link AbstractReportPublisher}.
     *
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     */
    public AbstractReportPublisher(final boolean allowMissing, final boolean runOnFailed) {
        super();
        this.allowMissing = allowMissing;
        this.runOnFailed = runOnFailed;
    }

    /**
     * Returns whether missing reports are allowed.
     *
     * @return {@code true} if missing reports are allowed, {@code false} otherwise
     */
    public boolean isAllowMissing() {
        return allowMissing;
    }

    /**
     * Returns whether this publisher can run for failed builds, too.
     *
     * @return {@code true} if this publisher can run for failed builds, {@code false} otherwise
     */
    public boolean isRunOnFailed() {
        return runOnFailed;
    }

    @Override
    public abstract Action getProjectAction(final AbstractProject<?, ?> project);

    @Override
    public Collection<? extends Action> getProjectActions(final AbstractProject<?, ?> project) {
        final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(getProjectAction(project));
        if (project instanceof MatrixProject && ((MatrixProject) project).getActiveConfigurations() != null) {
            for (final MatrixConfiguration mc : ((MatrixProject) project).getActiveConfigurations()) {
                try {
                    mc.onLoad(mc.getParent(), mc.getName());
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "Could not reload the matrix configuration");
                }
            }
        }

        return actions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Returns whether this publisher can continue processing. Returns {@code true} if the property {@code runOnFailed}
     * is set or if the build is not aborted or failed.
     *
     * @param result
     *            the build result
     * @return {@code true} if the build can continue
     */
    protected boolean canContinue(final Result result) {
        if (isRunOnFailed()) {
            return result.isBetterThan(Result.ABORTED);
        } else {
            return result.isBetterThan(Result.FAILURE);
        }
    }

    /**
     * Configures the tool installation for functioning in the node and the environment.
     *
     * @param toolName
     *            the tool name identifying the specific tool
     * @param listener
     *            the listener
     * @param env
     *            the environment
     * @return the tool installation
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    protected AbstractToolInstallation configureToolInstallation(final String toolName,
            final BuildListener listener, final EnvVars env) throws IOException, InterruptedException {
        AbstractToolInstallation installation = getToolInstallation(toolName, env);
        if (installation != null) {
            installation = installation.forNode(Computer.currentComputer().getNode(), listener);
            installation = installation.forEnvironment(env);
        }
        return installation;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param toolName
     *            the tool name identifying the specific tool
     * @param env
     *            the environment
     * @return the tool installation
     */
    public ETInstallation getToolInstallation(final String toolName, final EnvVars env) {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            final ETInstallation[] installations = instance.getDescriptorByType(
                    ETInstallation.DescriptorImpl.class).getInstallations();
            final String expToolName = env.expand(toolName);
            for (final ETInstallation installation : installations) {
                if (expToolName != null && expToolName.equals(installation.getName())) {
                    return installation;
                }
            }
        }
        return null;
    }
}
