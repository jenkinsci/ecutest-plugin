/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestBenchConfiguration;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestConfiguration;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.util.ArgumentListBuilder;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * Client to start and stop ECU-TEST by either COM or XML-RPC communication.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETClient extends AbstractToolClient {

    private final String workspaceDir;
    private final String settingsDir;
    private final boolean debug;
    private String version;
    private String lastTbc;
    private String lastTcf;

    /**
     * Instantiates a new {@link ETClient}.
     *
     * @param toolName     the tool name identifying the chosen {@link ETInstallation}.
     * @param installPath  the ECU-TEST install path
     * @param workspaceDir the ECU-TEST workspace directory
     * @param settingsDir  the ECU-TEST settings directory
     * @param timeout      the timeout to start ECU-TEST
     * @param debug        the debug mode
     */
    public ETClient(final String toolName, final String installPath, final String workspaceDir,
                    final String settingsDir, final int timeout, final boolean debug) {
        super(toolName, installPath, timeout);
        this.workspaceDir = StringUtils.trimToEmpty(workspaceDir);
        this.settingsDir = StringUtils.trimToEmpty(settingsDir);
        this.debug = debug;
        version = "";
        lastTbc = "";
        lastTcf = "";
    }

    /**
     * Instantiates a new {@link ETClient}.
     *
     * @param toolName the tool name identifying the chosen {@link ETInstallation}.
     * @param timeout  the timeout to start ECU-TEST
     */
    public ETClient(final String toolName, final int timeout) {
        super(toolName, timeout);
        workspaceDir = "";
        settingsDir = "";
        debug = false;
        version = "";
        lastTbc = "";
        lastTcf = "";
    }

    /**
     * Checks already opened ECU-TEST instances.
     *
     * @param launcher the launcher
     * @param kill     specifies whether to task-kill the running processes
     * @return list of found processes, can be empty but never {@code null}
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static List<String> checkProcesses(final Launcher launcher, final boolean kill)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(new CheckProcessCallable(kill));
    }

    /**
     * Closes already opened ECU-TEST instances.
     *
     * @param kill     specifies whether to task-kill the running processes
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true} if ECU-TEST instance has been stopped successfully
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static boolean stopProcesses(final Launcher launcher, final TaskListener listener, final boolean kill)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(new StopCallable(StartETBuilder.DEFAULT_TIMEOUT, kill, listener));
    }

    /**
     * Gets the COM version of currently running ECU-TEST instance.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return the COM version
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static String getComVersion(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(new VersionCallable(listener));
    }

    /**
     * @return the workspace directory
     */
    public String getWorkspaceDir() {
        return workspaceDir;
    }

    /**
     * @return the settings directory
     */
    public String getSettingsDir() {
        return settingsDir;
    }

    /**
     * @return the debug mode
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the last loaded TBC file path
     */
    public String getLastTbc() {
        return lastTbc;
    }

    /**
     * @return the last loaded TCF file path
     */
    public String getLastTcf() {
        return lastTcf;
    }

    @SuppressWarnings({"checkstyle:cyclomaticcomplexity", "checkstyle:npathcomplexity"})
    @Override
    public boolean start(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                         final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(String.format("Starting %s...", getToolName()));

        // Check open processes
        if (checkProcesses) {
            final List<String> foundProcesses = checkProcesses(launcher, true);
            if (!foundProcesses.isEmpty()) {
                logger.logInfo(String.format("Terminated running processes: %s", foundProcesses));
            }
        }

        // Check ECU-TEST location and architecture
        if (StringUtils.isEmpty(getInstallPath())) {
            logger.logError("ECU-TEST executable could not be found!");
            return false;
        } else {
            // Check architecture compatibility between JVM and ECU-TEST
            final boolean is64BitJVM = ProcessUtil.is64BitJVM(workspace.toComputer());
            if (!checkProcessArchitecture(getInstallPath(), is64BitJVM, launcher)) {
                logger.logError("The configured ECU-TEST executable is not compatible with running Java VM! "
                    + "Please install a 64-bit JRE which supports 64-bit ECU-TEST installation!");
                return false;
            }
            // Launch process
            if (!launchProcess(launcher, listener)) {
                return false;
            }
        }

        // Initialize COM connection
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }
        final String comVersion = launcher.getChannel().call(new StartCallable(getTimeout(), listener));
        if (comVersion.isEmpty()) {
            logger.logError("Could not determine ECU-TEST version!");
            return false;
        } else {
            version = comVersion;
            logger.logDebug("COM ProgID: " + ETComProperty.getInstance().getProgId());
            logger.logDebug("COM version: " + comVersion);
        }

        // Check ECU-TEST version
        final ToolVersion comToolVersion = ToolVersion.parse(comVersion);
        if (comToolVersion.compareWithoutMicroTo(ETPlugin.ET_MAX_VERSION) > 0) {
            logger.logWarn(String.format(
                "The configured ECU-TEST version %s might be incompatible with this plugin. "
                    + "Currently supported versions: %s up to %s", comVersion,
                ETPlugin.ET_MIN_VERSION.toMinorString(), ETPlugin.ET_MAX_VERSION.toMinorString()));
        } else if (comToolVersion.compareTo(ETPlugin.ET_MIN_VERSION) < 0) {
            logger.logError(String.format(
                "The configured ECU-TEST version %s is not compatible with this plugin. "
                    + "Please use at least ECU-TEST %s!", comVersion, ETPlugin.ET_MIN_VERSION.toMicroString()));
            // Close ECU-TEST
            stop(checkProcesses, workspace, launcher, listener);
            return false;
        }

        // Read currently loaded configurations
        if (comToolVersion.compareWithoutMicroTo(new ToolVersion(7, 0, 0)) >= 0) {
            lastTbc = launcher.getChannel().call(new LastTbcCallable(listener));
            lastTcf = launcher.getChannel().call(new LastTcfCallable(listener));
        }

        logger.logInfo(String.format("%s started successfully.", getToolName()));
        return true;
    }

    @Override
    public boolean stop(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                        final TaskListener listener) throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(String.format("Stopping %s...", getToolName()));

        // Check open processes
        if (checkProcesses) {
            final List<String> foundProcesses = checkProcesses(launcher, false);
            if (foundProcesses.isEmpty()) {
                logger.logWarn("No running ECU-TEST instance found!");
                return true;
            }
        }

        // Close COM connection and stop ECU-TEST
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }
        if (launcher.getChannel().call(new StopCallable(getTimeout(), checkProcesses, listener))) {
            logger.logInfo(String.format("%s stopped successfully.", getToolName()));
            return true;
        }
        return false;
    }

    @Override
    public boolean restart(final boolean checkProcesses, final FilePath workspace, final Launcher launcher,
                           final TaskListener listener) throws IOException, InterruptedException {
        return stop(checkProcesses, workspace, launcher, listener)
            && start(checkProcesses, workspace, launcher, listener);
    }

    @Override
    protected ArgumentListBuilder createCmdLine() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getInstallPath());

        if (!getWorkspaceDir().isEmpty()) {
            args.add("--workspaceDir", getWorkspaceDir());
        }

        if (!getSettingsDir().isEmpty()) {
            args.add("-s", getSettingsDir());
        }

        if (isDebug()) {
            args.add("-d");
        }

        // Create full workspace automatically
        args.add("--startupAutomated=CreateDirs");

        return args;
    }

    /**
     * Checks the process architecture compatibility between ECU-TEST and underlying JVM that runs the slave.
     * A 64-bit JVM supports both 32-bit and 64-bit ECU-TEST, while 32-bit JVM is only compatible with 32-bit ECU-TEST.
     *
     * @param processPath the full process path
     * @param is64BitJVM  specifies whether the JVM supports 64-bit architecture
     * @param launcher    the launcher
     * @return {@code true} if architectures are compatible, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public boolean checkProcessArchitecture(final String processPath, final boolean is64BitJVM,
                                            final Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new CheckProcessArchitectureCallable(processPath, is64BitJVM));
    }

    /**
     * Updates all user libraries.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true} if update is successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public boolean updateUserLibs(Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo("Updating user libraries...");

        return launcher.getChannel().call(new UpdateUserLibsCallable(listener));
    }

    /**
     * Checks whether the currently selected configurations are started.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true} if configurations are started, {@code false} otherwise
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public boolean checkConfigStatus(final Launcher launcher, TaskListener listener)
        throws IOException, InterruptedException {
        return launcher.getChannel().call(new CheckConfigStatus(listener));
    }

    /**
     * {@link Callable} providing remote access to establish a COM connection.
     */
    private static final class StartCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final int timeout;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link StartCallable}.
         *
         * @param timeout  the timeout
         * @param listener the listener
         */
        StartCallable(final int timeout, final TaskListener listener) {
            this.timeout = timeout;
            this.listener = listener;
        }

        @Override
        public String call() throws IOException {
            String version = "";
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId, timeout)) {
                if (comClient.isApplicationRunning()) {
                    version = comClient.getVersion();
                }
            } catch (final ETComException e) {
                logger.logComException(e.getMessage());
            }
            return version;
        }
    }

    /**
     * {@link Callable} providing remote access to close ECU-TEST via COM.
     */
    private static final class StopCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final int timeout;
        private final boolean checkProcesses;
        private final TaskListener listener;

        /**
         * Instantiates a {@link StopCallable}.
         *
         * @param timeout        the timeout
         * @param checkProcesses specifies whether to check open processes after closing
         * @param listener       the listener
         */
        StopCallable(final int timeout, final boolean checkProcesses, final TaskListener listener) {
            this.timeout = timeout;
            this.checkProcesses = checkProcesses;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isTerminated = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId, timeout)) {
                if (comClient.isApplicationRunning()) {
                    isTerminated = comClient.quit() || comClient.exit();
                } else {
                    logger.logError("ECU-TEST COM instance is not ready to use!");
                }
            } catch (final ETComException e) {
                logger.logComException(e.getMessage());
            } finally {
                if (checkProcesses) {
                    final List<String> foundProcesses = ProcessUtil.checkETProcesses(true);
                    if (!foundProcesses.isEmpty()) {
                        logger.logInfo(String.format("Terminated running processes: %s", foundProcesses));
                    }
                }
            }
            return isTerminated;
        }
    }

    /**
     * {@link Callable} providing remote access to check open ECU-TEST processes.
     */
    private static final class CheckProcessCallable extends MasterToSlaveCallable<List<String>, IOException> {

        private static final long serialVersionUID = 1L;

        private final boolean kill;

        /**
         * Instantiates a new {@link CheckProcessCallable}.
         *
         * @param kill specifies whether to task-kill running processes
         */
        CheckProcessCallable(final boolean kill) {
            this.kill = kill;
        }

        @Override
        public List<String> call() throws IOException {
            return ProcessUtil.checkETProcesses(kill);
        }
    }

    /**
     * {@link Callable} providing remote access to request the COM version of currently running ECU-TEST instance.
     */
    private static final class VersionCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;

        /**
         * Instantiates a new {@link VersionCallable}.
         *
         * @param listener the listener
         */
        VersionCallable(final TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public String call() throws IOException {
            String comVersion = "";
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                comVersion = comClient.getVersion();
            } catch (final ETComException e) {
                logger.logError("-> Caught COM exception: " + e.getMessage());
            }
            return comVersion;
        }
    }

    /**
     * {@link Callable} providing remote access to request the last loaded TBC of currently running ECU-TEST instance.
     */
    private static final class LastTbcCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;

        /**
         * Instantiates a new {@link LastTbcCallable}.
         *
         * @param listener the listener
         */
        LastTbcCallable(final TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public String call() throws IOException {
            String tbcFilePath = "";
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId);
                 TestBenchConfiguration tbc = (TestBenchConfiguration)
                     comClient.getCurrentTestBenchConfiguration()) {
                tbcFilePath = StringUtils.trimToEmpty(tbc.getFileName());
            } catch (final ETComException e) {
                logger.logError("-> Caught COM exception: " + e.getMessage());
            }
            return tbcFilePath;
        }
    }

    /**
     * {@link Callable} providing remote access to request the last loaded TCF of currently running ECU-TEST instance.
     */
    private static final class LastTcfCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;

        /**
         * Instantiates a new {@link LastTcfCallable}.
         *
         * @param listener the listener
         */
        LastTcfCallable(final TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public String call() throws IOException {
            String tcfFilePath = "";
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId);
                 TestConfiguration tcf = (TestConfiguration) comClient.getCurrentTestConfiguration()) {
                tcfFilePath = StringUtils.trimToEmpty(tcf.getFileName());
            } catch (final ETComException e) {
                logger.logError("-> Caught COM exception: " + e.getMessage());
            }
            return tcfFilePath;
        }
    }

    /**
     * {@link Callable} providing remote access to check architecture of ECU-TEST process against JVM architecture.
     */
    private static final class CheckProcessArchitectureCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final String processPath;
        private final boolean is64BitJVM;

        /**
         * Instantiates a new {@link CheckProcessArchitectureCallable}.
         *
         * @param processPath the full process file path
         * @param is64BitJVM  specifies whether the JVM supports 64-bit architecture
         */
        CheckProcessArchitectureCallable(final String processPath, final boolean is64BitJVM) {
            this.processPath = processPath;
            this.is64BitJVM = is64BitJVM;
        }

        @Override
        public Boolean call() throws IOException {
            final boolean is64BitProc = ProcessUtil.is64BitExecutable(processPath);
            return is64BitJVM || !is64BitProc;
        }
    }

    /**
     * {@link Callable} providing remote access to update all user libraries.
     */
    private static final class UpdateUserLibsCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;

        /**
         * Instantiates a new {@link UpdateUserLibsCallable}.
         *
         * @param listener the listener
         */
        UpdateUserLibsCallable(final TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                return comClient.updateUserLibraries();
            } catch (final ETComException e) {
                logger.logError("-> Caught COM exception: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * {@link Callable} providing remote access to check whether the currently selected configurations are started.
     */
    private static final class CheckConfigStatus extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;

        /**
         * Instantiates a new {@link CheckConfigStatus}.
         *
         * @param listener the listener
         */
        CheckConfigStatus(final TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                String comVersion = comClient.getVersion();
                final ToolVersion toolVersion = ToolVersion.parse(comVersion);
                if (toolVersion.compareWithoutMicroTo(new ToolVersion(8, 0, 0)) >= 0) {
                    return comClient.isStarted();
                } else {
                    logger.logWarn("-> Checking configuration status is not supported. " +
                        "Please use at least ECU-TEST 8.0!");
                }
            } catch (final ETComException e) {
                logger.logError("-> Caught COM exception: " + e.getMessage());
            }
            return false;
        }
    }
}
