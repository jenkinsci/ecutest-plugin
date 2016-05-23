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
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractItem;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
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
@SuppressWarnings("unchecked")
public abstract class AbstractReportPublisher extends Recorder implements SimpleBuildStep {

    private boolean allowMissing;
    private boolean runOnFailed;
    /**
     * {@code Boolean} type is required to retain backward compatibility with default value {@code true}.
     *
     * @since 1.9
     */
    private Boolean archiving = true;
    /**
     * {@code Boolean} type is required to retain backward compatibility with default value {@code true}.
     *
     * @since 1.9
     */
    private Boolean keepAll = true;

    /**
     * Instantiates a new {@link AbstractReportPublisher}.
     */
    public AbstractReportPublisher() {
        super();
    }

    /**
     * Instantiates a new {@link AbstractReportPublisher}.
     *
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     * @param archiving
     *            specifies whether archiving artifacts is enabled
     * @param keepAll
     *            specifies whether artifacts are archived for all successful builds,
     *            otherwise only the most recent
     */
    public AbstractReportPublisher(final boolean allowMissing, final boolean runOnFailed, final boolean archiving,
            final boolean keepAll) {
        super();
        this.allowMissing = allowMissing;
        this.runOnFailed = runOnFailed;
        this.archiving = archiving;
        this.keepAll = keepAll;
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

    /**
     * Returns whether archiving artifacts is enabled.
     *
     * @return {@code true} if archiving artifacts is enabled, {@code false} otherwise
     */
    public Boolean isArchiving() {
        return archiving;
    }

    /**
     * Equivalent getter with {@code boolean} return type.
     *
     * @see #isArchiving()
     * @return {@code true} if archiving artifacts is enabled, {@code false} otherwise
     */
    public boolean getArchiving() {
        return archiving;
    }

    /**
     * Returns whether artifacts are archived for all successful builds, otherwise only the most recent.
     *
     * @return {@code true} if artifacts should be archived for all successful builds, {@code false} otherwise
     */
    public Boolean isKeepAll() {
        return keepAll;
    }

    /**
     * Equivalent getter with {@code boolean} return type.
     *
     * @see #isKeepAll()
     * @return {@code true} if artifacts should be archived for all successful builds, {@code false} otherwise
     */
    public boolean getKeepAll() {
        return keepAll;
    }

    /**
     * @param allowMissing
     *            specifies whether missing reports are allowed
     */
    @DataBoundSetter
    public void setAllowMissing(final boolean allowMissing) {
        this.allowMissing = allowMissing;
    }

    /**
     * @param runOnFailed
     *            specifies whether this publisher even runs on a failed build
     */
    @DataBoundSetter
    public void setRunOnFailed(final boolean runOnFailed) {
        this.runOnFailed = runOnFailed;
    }

    /**
     * @param archiving
     *            specifies whether archiving artifacts is enabled
     */
    @DataBoundSetter
    public void setArchiving(final boolean archiving) {
        this.archiving = archiving;
    }

    /**
     * @param keepAll
     *            specifies whether artifacts are archived for all successful builds,
     *            otherwise only the most recent
     */
    @DataBoundSetter
    public void setKeepAll(final boolean keepAll) {
        this.keepAll = keepAll;
    }

    // @Override
    // public abstract Action getProjectAction(final AbstractProject<?, ?> project);
    //
    // @Override
    // public Collection<? extends Action> getProjectActions(final AbstractProject<?, ?> project) {
    // final ArrayList<Action> actions = new ArrayList<Action>();
    // actions.add(getProjectAction(project));
    // if (project instanceof MatrixProject && ((MatrixProject) project).getActiveConfigurations() != null) {
    // for (final MatrixConfiguration mc : ((MatrixProject) project).getActiveConfigurations()) {
    // try {
    // mc.onLoad(mc.getParent(), mc.getName());
    // } catch (final IOException e) {
    // LOGGER.log(Level.SEVERE, "Could not reload the matrix configuration");
    // }
    // }
    // }
    //
    // return actions;
    // }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Returns whether this publisher can continue processing. Returns {@code true} if the property {@code runOnFailed}
     * is set or if the build is not aborted or failed.
     *
     * @param result
     *            the run result
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
     * @param envVars
     *            the environment variables
     * @return the tool installation
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    protected AbstractToolInstallation configureToolInstallation(final String toolName,
            final TaskListener listener, final EnvVars envVars) throws IOException, InterruptedException {
        AbstractToolInstallation installation = getToolInstallation(toolName, envVars);

        if (installation != null) {
            final Node node = Computer.currentComputer().getNode();
            if (node != null) {
                installation = installation.forNode(node, listener);
            }
            installation = installation.forEnvironment(envVars);
        }
        return installation;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param toolName
     *            the tool name identifying the specific tool
     * @param envVars
     *            the environment variables
     * @return the tool installation
     */
    public ETInstallation getToolInstallation(final String toolName, final EnvVars envVars) {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            final ETInstallation[] installations = instance.getDescriptorByType(
                    ETInstallation.DescriptorImpl.class).getInstallations();
            final String expToolName = envVars.expand(toolName);
            for (final ETInstallation installation : installations) {
                if (StringUtils.equals(expToolName, installation.getName())) {
                    return installation;
                }
            }
        }
        return null;
    }

    /**
     * Gets the workspace directory, either previous ECU-TEST workspace or default one.
     *
     * @param run
     *            the run
     * @return the workspace directory
     */
    protected String getWorkspaceDir(final Run<?, ?> run) {
        String workspaceDir = "";
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspaceDir = toolEnvAction.getToolWorkspace();
        }
        return workspaceDir;
    }

    /**
     * Gets the settings directory, either previous ECU-TEST settings or default one.
     *
     * @param run
     *            the run
     * @return the settings directory
     */
    protected String getSettingsDir(final Run<?, ?> run) {
        String settingsDir = "";
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            settingsDir = toolEnvAction.getToolSettings();
        }
        return settingsDir;
    }

    /**
     * Gets the archive target.
     *
     * @param run
     *            the run
     * @return the archive target
     */
    protected FilePath getArchiveTarget(final Run<?, ?> run) {
        return new FilePath(isKeepAll() ? getBuildArchiveDir(run) : getProjectArchiveDir(run.getParent()));
    }

    /**
     * Gets the directory where the reports are stored for the given project.
     *
     * @param project
     *            the project
     * @return the project archive directory
     */
    private File getProjectArchiveDir(final AbstractItem project) {
        return new File(project.getRootDir(), getUrlName());
    }

    /**
     * Gets the directory where the reports are stored for the given build.
     *
     * @param run
     *            the run
     * @return the build archive directory
     */
    private File getBuildArchiveDir(final Run<?, ?> run) {
        return new File(run.getRootDir(), getUrlName());
    }

    /**
     * Gets the URL name that will be used for archiving and linking the reports.
     *
     * @return the URL name
     */
    protected abstract String getUrlName();

    /**
     * Builds a list of report files for report generation.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param run
     *            the run
     * @param launcher
     *            the launcher
     * @return the list of report files
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    protected List<FilePath> getReportFiles(final Run<?, ?> run, final Launcher launcher)
            throws IOException, InterruptedException {
        final List<FilePath> reportFiles = new ArrayList<FilePath>();
        final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath testReportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
            if (testReportDir.exists()) {
                reportFiles.addAll(Arrays.asList(testReportDir.list("**/" + TRFPublisher.TRF_FILE_NAME)));
            }
        }
        Collections.reverse(reportFiles);
        return reportFiles;
    }

    /**
     * Removes the report actions from all previous builds which published at project level.
     *
     * @param run
     *            the run
     * @param clazz
     *            the report action class to remove
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    @SuppressWarnings("deprecation")
    public static void removePreviousReports(final Run<?, ?> run,
            final Class<? extends AbstractReportAction> clazz) throws IOException {
        Run<?, ?> prevBuild = run.getPreviousBuild();
        while (prevBuild != null) {
            final AbstractReportAction buildAction = prevBuild.getAction(clazz);
            if (buildAction != null && buildAction.isProjectLevel()) {
                prevBuild.getActions().remove(buildAction);
                prevBuild.save();
            }
            prevBuild = prevBuild.getPreviousBuild();
        }
    }
}
