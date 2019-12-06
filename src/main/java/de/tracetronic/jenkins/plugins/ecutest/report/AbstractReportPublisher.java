/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.env.ToolEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.AbstractToolBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETComRegisterClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractItem;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import hudson.tools.ToolInstallation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.RandomStringUtils;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Common base class for all report publishers.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
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

    private transient boolean downstream;
    private transient String workspace;
    private transient TTConsoleLogger logger;

    /**
     * Instantiates a new {@link AbstractReportPublisher}.
     */
    public AbstractReportPublisher() {
        super();
    }

    /**
     * Instantiates a new {@link AbstractReportPublisher}.
     *
     * @param allowMissing specifies whether missing reports are allowed
     * @param runOnFailed  specifies whether this publisher even runs on a failed build
     * @param archiving    specifies whether archiving artifacts is enabled
     * @param keepAll      specifies whether artifacts are archived for all successful builds,
     *                     otherwise only the most recent
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
     * Gets the first TRF file found in given report directory.
     *
     * @param reportDir the report directory
     * @return the first report file or {@code null} if not found
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    @CheckForNull
    public static FilePath getFirstReportFile(final FilePath reportDir) throws IOException, InterruptedException {
        final FilePath[] files = reportDir.list(TRFPublisher.TRF_INCLUDE, TRFPublisher.TRF_EXCLUDE);
        return files.length > 0 ? files[0] : null;
    }

    /**
     * Removes the report actions from all previous builds which published at project level.
     *
     * @param run   the run
     * @param clazz the report action class to remove
     * @throws IOException signals that an I/O exception has occurred
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

    /**
     * Returns whether missing reports are allowed.
     *
     * @return {@code true} if missing reports are allowed, {@code false} otherwise
     */
    public boolean isAllowMissing() {
        return allowMissing;
    }

    /**
     * @param allowMissing specifies whether missing reports are allowed
     */
    @DataBoundSetter
    public void setAllowMissing(final boolean allowMissing) {
        this.allowMissing = allowMissing;
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
     * @param runOnFailed specifies whether this publisher even runs on a failed build
     */
    @DataBoundSetter
    public void setRunOnFailed(final boolean runOnFailed) {
        this.runOnFailed = runOnFailed;
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
     * @return {@code true} if archiving artifacts is enabled, {@code false} otherwise
     * @see #isArchiving()
     */
    public boolean getArchiving() {
        return archiving;
    }

    /**
     * @param archiving specifies whether archiving artifacts is enabled
     */
    @DataBoundSetter
    public void setArchiving(final boolean archiving) {
        this.archiving = archiving;
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
     * @return {@code true} if artifacts should be archived for all successful builds, {@code false} otherwise
     * @see #isKeepAll()
     */
    public boolean getKeepAll() {
        return keepAll;
    }

    /**
     * @param keepAll specifies whether artifacts are archived for all successful builds,
     *                otherwise only the most recent
     */
    @DataBoundSetter
    public void setKeepAll(final boolean keepAll) {
        this.keepAll = keepAll;
    }

    /**
     * Returns whether this publisher is part of {@link DownStreamPublisher} actions.
     *
     * @return {@code true}, if downstream-based, {@code false} otherwise
     */
    public boolean isDownstream() {
        return downstream;
    }

    /**
     * Transient setter to inform this publisher that it is part of a {@link DownStreamPublisher}.
     *
     * @param downstream the downstream flag
     */
    public void setDownstream(final boolean downstream) {
        this.downstream = downstream;
    }

    /**
     * @return the downstream workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Transient setter for the downstream workspace.
     *
     * @param workspace the downstream workspace
     */
    public void setWorkspace(final String workspace) {
        this.workspace = workspace;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws InterruptedException, IOException {
        try {
            initLogger(listener);
            performReport(run, workspace, launcher, listener);
        } catch (final IOException e) {
            Util.displayIOException(e, listener);
            throw e;
        } catch (final ETPluginException e) {
            getLogger().logError(e.getMessage());
            throw new AbortException(e.getMessage());
        }
    }

    /**
     * Performs the report-specific post-build operations.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @throws InterruptedException the interrupted exception
     * @throws IOException          signals that an I/O exception has occurred
     * @throws ETPluginException    in case of report operation errors
     */
    protected abstract void performReport(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
        throws InterruptedException, IOException, ETPluginException;

    /**
     * Gets the logger instance.
     *
     * @return the logger
     */
    protected TTConsoleLogger getLogger() {
        return logger != null ? logger : new TTConsoleLogger(TaskListener.NULL);
    }

    /**
     * Initializes the logger.
     *
     * @param listener the listener
     */
    private void initLogger(final TaskListener listener) {
        logger = new TTConsoleLogger(listener);
    }

    /**
     * Determines whether this publisher will be skipped
     * depending on OS architecture and current build result.
     *
     * @param checkOS  specifies whether to check OS
     * @param run      the run
     * @param launcher the launcher
     * @return {@code true} when to skip, {@code false} otherwise
     * @throws ETPluginException if Unix-based launcher
     */
    protected boolean isSkipped(final boolean checkOS, final Run<?, ?> run, final Launcher launcher)
        throws ETPluginException {
        if (checkOS) {
            ProcessUtil.checkOS(launcher);
        }
        final Result buildResult = run.getResult();
        if (buildResult != null && !canContinue(buildResult)) {
            getLogger().logInfo(String.format("Skipping publisher since build result is %s", buildResult));
            return true;
        }
        return false;
    }

    /**
     * Returns whether this publisher can continue processing. Returns {@code true} if the property {@code runOnFailed}
     * is set or if the build is not aborted or failed.
     *
     * @param result the run result
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
     * Checks whether an ECU-TEST instance is still running.
     *
     * @param launcher the launcher
     * @return {@code true} if ECU-TEST is running, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    protected boolean isETRunning(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        return !foundProcesses.isEmpty();
    }

    /**
     * Configures an ECU-TEST client with given workspace settings.
     *
     * @param toolName  the tool name
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return the ECU-TEST client
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     * @throws ETPluginException    in case of a COM exception
     */
    protected ETClient getToolClient(final String toolName, final Run<?, ?> run, final FilePath workspace,
                                     final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException, ETPluginException {
        final ETInstallation installation = configureToolInstallation(toolName, workspace.toComputer(), listener,
            run.getEnvironment(listener));
        final String expandedToolName = run.getEnvironment(listener).expand(installation.getName());

        // Register ECU-TEST COM server
        if (installation.isRegisterComServer()) {
            final String installPath = installation.getComExecutable(launcher);
            ETComRegisterClient comClient = new ETComRegisterClient(expandedToolName, installPath);
            comClient.start(false, workspace, launcher, listener);
        }

        final String installPath = installation.getExecutable(launcher);
        final String workspaceDir = getWorkspaceDir(run, workspace);
        final String settingsDir = getSettingsDir(run, workspace);
        return new ETClient(expandedToolName, installPath, workspaceDir,
            settingsDir, StartETBuilder.DEFAULT_TIMEOUT, false);
    }

    /**
     * Configures the tool installation for functioning in the node and the environment.
     *
     * @param toolName the tool name identifying the specific tool
     * @param computer the computer
     * @param listener the listener
     * @param envVars  the environment variables
     * @return the tool installation
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     * @throws ETPluginException    if the selected tool installation is not configured
     */
    protected ETInstallation configureToolInstallation(final String toolName, final Computer computer,
                                                       final TaskListener listener, final EnvVars envVars)
        throws IOException, InterruptedException, ETPluginException {
        ETInstallation installation = getToolInstallation(toolName, envVars);
        if (installation != null && computer != null) {
            final Node node = computer.getNode();
            if (node != null) {
                installation = installation.forNode(node, listener);
                installation = installation.forEnvironment(envVars);
            }
        } else {
            throw new ETPluginException("The selected ECU-TEST installation is not configured for this node!");
        }
        // Set the COM settings for the current ECU-TEST instance
        VirtualChannel channel = computer.getChannel();
        if (channel != null) {
            channel.call(new AbstractToolBuilder.SetComPropertyCallable(
                installation.getProgId(), installation.getTimeout()));
        }
        ETComProperty.getInstance().setProgId(installation.getProgId());
        ETComProperty.getInstance().setTimeout(installation.getTimeout());
        return installation;
    }

    /**
     * Gets the tool installation by descriptor and tool name.
     *
     * @param toolName the tool name identifying the specific tool
     * @param envVars  the environment variables
     * @return the tool installation
     */
    public ETInstallation getToolInstallation(final String toolName, final EnvVars envVars) {
        final String expToolName = envVars.expand(toolName);
        return getToolDescriptor().getInstallation(expToolName);
    }

    /**
     * Gets the tool descriptor holding the installations.
     *
     * @return the tool descriptor
     */
    public ETInstallation.DescriptorImpl getToolDescriptor() {
        return ToolInstallation.all().get(ETInstallation.DescriptorImpl.class);
    }

    /**
     * Gets the workspace directory, either previous ECU-TEST workspace or default one.
     *
     * @param run       the run
     * @param workspace the workspace
     * @return the workspace directory
     */
    protected String getWorkspaceDir(final Run<?, ?> run, final FilePath workspace) {
        String workspaceDir;
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            workspaceDir = toolEnvAction.getToolWorkspace();
        } else if (isDownstream()) {
            workspaceDir = workspace.child(getWorkspace()).getRemote();
        } else {
            workspaceDir = "";
        }
        return workspaceDir;
    }

    /**
     * Gets the settings directory, either previous ECU-TEST settings or default one.
     *
     * @param run       the run
     * @param workspace the workspace
     * @return the settings directory
     */
    protected String getSettingsDir(final Run<?, ?> run, final FilePath workspace) {
        String settingsDir;
        final ToolEnvInvisibleAction toolEnvAction = run.getAction(ToolEnvInvisibleAction.class);
        if (toolEnvAction != null) {
            settingsDir = toolEnvAction.getToolSettings();
        } else if (isDownstream()) {
            settingsDir = workspace.child(getWorkspace()).getRemote();
        } else {
            settingsDir = "";
        }
        return settingsDir;
    }

    /**
     * Gets the archive target.
     *
     * @param run the run
     * @return the archive target
     */
    protected FilePath getArchiveTarget(final Run<?, ?> run) {
        return new FilePath(isKeepAll() ? getBuildArchiveDir(run) : getProjectArchiveDir(run.getParent()));
    }

    /**
     * Gets the directory where the reports are stored for the given project.
     *
     * @param project the project
     * @return the project archive directory
     */
    private File getProjectArchiveDir(final AbstractItem project) {
        return new File(project.getRootDir(), getUrlName());
    }

    /**
     * Gets the directory where the reports are stored for the given build.
     *
     * @param run the run
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
     * Gets the report directories either from test environment actions or downstream workspace.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @return the report directories
     * @throws IOException          signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    protected List<FilePath> getReportDirs(final Run<?, ?> run, final FilePath workspace, final Launcher launcher)
        throws IOException, InterruptedException {
        final List<FilePath> reportDirs = new ArrayList<>();
        if (isDownstream()) {
            final FilePath wsDir = workspace.child(getWorkspace());
            if (wsDir.exists()) {
                final FilePath reportDir = wsDir.child("TestReports");
                if (reportDir.exists()) {
                    reportDirs.addAll(reportDir.listDirectories());
                }
            }
        } else {
            final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
            for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
                final FilePath reportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
                if (reportDir.exists()) {
                    reportDirs.add(reportDir);
                }
            }
        }
        return reportDirs;
    }

    /**
     * Builds a list of TRF files for report generation.
     * Includes the TRF files generated during separate sub-project execution.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @return the list of report files
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    protected List<FilePath> getReportFiles(final Run<?, ?> run, final FilePath workspace, final Launcher launcher)
        throws IOException, InterruptedException {
        return getReportFiles(TRFPublisher.TRF_INCLUDES, TRFPublisher.TRF_EXCLUDES, run, workspace, launcher);
    }

    /**
     * Builds a list of report files for report generation.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param includes  the includes
     * @param excludes  the excludes
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @return the list of report files
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    protected List<FilePath> getReportFiles(final String includes, final String excludes, final Run<?, ?> run,
                                            final FilePath workspace, final Launcher launcher)
        throws IOException, InterruptedException {
        final List<FilePath> reportFiles = new ArrayList<>();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        for (final FilePath reportDir : reportDirs) {
            reportFiles.addAll(Arrays.asList(
                reportDir.list(includes, excludes)));
        }
        Collections.reverse(reportFiles);
        return reportFiles;
    }

    /**
     * Gets the size of given file.
     *
     * @param file the file
     * @return the file size
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    protected long getFileSize(final FilePath file) throws IOException, InterruptedException {
        return file.length();
    }

    /**
     * Gets the total size of given directory recursively.
     *
     * @param directory the directory
     * @return the directory size
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    protected long getDirectorySize(final FilePath directory) throws IOException, InterruptedException {
        long size = 0;
        final FilePath[] files = directory.list("**");
        for (final FilePath file : files) {
            size += file.length();
        }
        return size;
    }

    /**
     * Generates a random alphanumeric id to be used for linking reports.
     *
     * @return the random id string
     */
    public static String randomId() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public AbstractReportDescriptor getDescriptor() {
        return (AbstractReportDescriptor) super.getDescriptor();
    }
}
