/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a ECU-TEST installation specified by name and home directory.
 */
public class ETInstallation extends AbstractToolInstallation {

    private static final long serialVersionUID = 1L;

    /**
     * Executable file name of ECU-TEST.
     */
    private static final String EXECUTABLE = "ECU-TEST.exe";

    /**
     * Executable file name of ECU-TEST COM server.
     */
    private static final String COM_EXECUTABLE = "ECU-TEST_COM.exe";

    /**
     * Executable file name of Tool-Server.
     */
    private static final String TS_EXECUTABLE = "Tool-Server.exe";

    /**
     * Instantiates a new {@link ETInstallation}.
     *
     * @param name       the name of the ECU-TEST installation
     * @param home       the home directory of the ECU-TEST installation
     * @param properties the ECU-TEST properties
     */
    @DataBoundConstructor
    public ETInstallation(final String name, final String home, final List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /**
     * Instantiates a new {@link ETInstallation}.
     *
     * @param source     the source to install the ECU-TEST installation
     * @param home       the home directory of the ECU-TEST installation
     * @param properties the ECU-TEST properties
     */
    public ETInstallation(final ETInstallation source, final String home,
                          final List<? extends ToolProperty<?>> properties) {
        super(source.getName(), home, properties);
    }

    @Override
    public ETInstallation forEnvironment(final EnvVars env) {
        return new ETInstallation(this, env.expand(getHome()), getProperties().toList());
    }

    @Override
    public ETInstallation forNode(@Nonnull final Node node, final TaskListener log)
        throws IOException, InterruptedException {
        return new ETInstallation(this, translateFor(node, log), getProperties().toList());
    }

    /**
     * Gets the ECU-TEST executable path on the given target system.
     *
     * @param launcher the launcher
     * @return the ECU-TEST executable
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public String getExecutable(final Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new GetExecutableCallable(getHome()));
    }

    /**
     * Gets the ECU-TEST COM server executable path on the given target system.
     *
     * @param launcher the launcher
     * @return the ECU-TEST COM server executable
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public String getComExecutable(final Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new GetComExecutableCallable(getHome()));
    }

    /**
     * Gets the executable path of the Tool-Server on the given target system. According to ECU-TEST 6.5 and above the
     * Tool-Server executable is directly located in ECU-TEST installation path, otherwise in sub directory
     * 'ToolServer'.
     *
     * @param launcher the launcher
     * @return the Tool-Server executable path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public String getTSExecutable(final Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new GetTSExecutableCallable(getHome()));
    }

    /**
     * Gets the programmatic identifier from tool properties.
     *
     * @return the progId, default progId if tool property does not exist
     */
    public String getProgId() {
        final String progId;
        final ETToolProperty toolProperty = getProperties().get(ETToolProperty.class);
        if (toolProperty != null) {
            progId = toolProperty.getProgId();
        } else {
            progId = ETToolProperty.DescriptorImpl.getDefaultProgId();
        }
        return progId;
    }

    /**
     * Gets the COM timeout from tool properties.
     *
     * @return the COM timeout, default timeout if tool property does not exist
     */
    public int getTimeout() {
        final int timeout;
        final ETToolProperty toolProperty = getProperties().get(ETToolProperty.class);
        if (toolProperty != null) {
            timeout = toolProperty.getTimeout();
        } else {
            timeout = ETToolProperty.DescriptorImpl.getDefaultTimeout();
        }
        return timeout;
    }

    /**
     * Returns whether to register the COM server before each start of ECU-TEST.
     *
     * @return {@code true} if option enabled,  {@code false } otherwise
     */
    public boolean isRegisterComServer() {
        final ETToolProperty toolProperty = getProperties().get(ETToolProperty.class);
        return toolProperty != null && toolProperty.isRegisterComServer();
    }

    /**
     * Gets all ECU-TEST installations.
     *
     * @return all available installations, never {@code null}
     */
    public static ETInstallation[] installs() {
        final Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance == null) {
            return new ETInstallation[0];
        }
        final DescriptorImpl etDescriptor = instance.getDescriptorByType(DescriptorImpl.class);
        return etDescriptor.getInstallations();
    }

    /**
     * Gets the ECU-TEST installation by name.
     *
     * @param name the name
     * @return installation by name, {@code null} if not found
     */
    @CheckForNull
    public static ETInstallation get(final String name) {
        final ETInstallation[] installations = installs();
        for (final ETInstallation installation : installations) {
            if (StringUtils.equals(name, installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    /**
     * {@link MasterToSlaveCallable} providing remote access to return the ECU-TEST executable path.
     */
    private static final class GetExecutableCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final String home;

        /**
         * Instantiates a new {@link GetExecutableCallable}.
         *
         * @param home the home directory of ECU-TEST
         */
        GetExecutableCallable(final String home) {
            this.home = home;
        }

        @Override
        public String call() {
            final File exe = getExeFile(home);
            return exe != null && exe.exists() ? exe.getPath() : null;
        }

        /**
         * Gets the expanded ECU-TEST COM server executable file path.
         *
         * @param home the home directory of ECU-TEST
         * @return the executable file or {@code null} if home directory is not set
         */
        @CheckForNull
        private File getExeFile(final String home) {
            if (home != null) {
                final String expHome = Util.replaceMacro(home, EnvVars.masterEnvVars);
                if (expHome != null) {
                    return DescriptorImpl.getExeFile(new File(expHome));
                }
            }
            return null;
        }
    }

    /**
     * {@link MasterToSlaveCallable} providing remote access to return the ECU-TEST COM server executable path.
     */
    private static final class GetComExecutableCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final String home;

        /**
         * Instantiates a new {@link GetComExecutableCallable}.
         *
         * @param home the home directory of ECU-TEST
         */
        GetComExecutableCallable(final String home) {
            this.home = home;
        }

        @Override
        public String call() {
            final File exe = getComExeFile(home);
            return exe != null && exe.exists() ? exe.getPath() : null;
        }

        /**
         * Gets the expanded ECU-TEST COM server executable file path.
         *
         * @param home the home directory of ECU-TEST
         * @return the executable file or {@code null} if home directory is not set
         */
        @CheckForNull
        private File getComExeFile(final String home) {
            if (home != null) {
                final String expHome = Util.replaceMacro(home, EnvVars.masterEnvVars);
                if (expHome != null) {
                    return DescriptorImpl.getComExeFile(new File(expHome));
                }
            }
            return null;
        }
    }

    /**
     * {@link MasterToSlaveCallable} providing remote access to return the Tool-Server executable path.
     */
    private static final class GetTSExecutableCallable extends MasterToSlaveCallable<String, IOException> {

        private static final long serialVersionUID = 1L;

        private final String home;

        /**
         * Instantiates a new {@link GetTSExecutableCallable}.
         *
         * @param home the home directory of ECU-TEST
         */
        GetTSExecutableCallable(final String home) {
            this.home = home;
        }

        @Override
        public String call() {
            File exe = getTSExeFile("");
            if (exe == null || !exe.exists()) {
                exe = getTSExeFile("ToolServer");
            }
            return exe != null && exe.exists() ? exe.getPath() : null;
        }

        /**
         * Gets the expanded Tool-Server executable file path.
         *
         * @param subDir the sub directory relative to home directory
         * @return the Tool-Server executable file or {@code null} if home directory is not set
         */
        @CheckForNull
        private File getTSExeFile(final String subDir) {
            if (home != null) {
                final String expHome = Util.replaceMacro(home, EnvVars.masterEnvVars);
                if (expHome != null) {
                    return DescriptorImpl.getTSExeFile(new File(expHome), subDir);
                }
            }
            return null;
        }
    }

    /**
     * DescriptorImpl of {@link ETInstallation}.
     */
    @Symbol("ecuTest")
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends ToolDescriptor<ETInstallation> {

        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        @CopyOnWrite
        @SuppressFBWarnings("VO_VOLATILE_REFERENCE_TO_ARRAY")
        private volatile ETInstallation[] installations = new ETInstallation[0];

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "Never used in a "
            + "critical way. Do not change in working legacy code.")
        public DescriptorImpl() {
            super();
            load();
        }

        /**
         * Gets the ECU-TEST executable file.
         *
         * @param home the home directory of ECU-TEST
         * @return the executable file or {@code null} if Unix-based system
         */
        @CheckForNull
        private static File getExeFile(final File home) {
            if (Functions.isWindows() && home != null) {
                return new File(home, EXECUTABLE);
            }
            return null;
        }

        /**
         * Gets the ECU-TEST COM server executable file.
         *
         * @param home the home directory of ECU-TEST
         * @return the executable file or {@code null} if Unix-based system
         */
        @SuppressWarnings("PMD.UnusedPrivateMethod")
        @CheckForNull
        private static File getComExeFile(final File home) {
            if (Functions.isWindows() && home != null) {
                return new File(home, COM_EXECUTABLE);
            }
            return null;
        }

        /**
         * Gets the Tool-Server executable file.
         *
         * @param home   the home directory of ECU-TEST
         * @param subDir the sub directory relative to home directory
         * @return the executable file or {@code null} if Unix-based system
         */
        @SuppressWarnings("PMD.UnusedPrivateMethod")
        @CheckForNull
        private static File getTSExeFile(final File home, final String subDir) {
            if (Functions.isWindows() && home != null && subDir != null) {
                return new File(new File(home, subDir), TS_EXECUTABLE);
            }
            return null;
        }

        @Override
        public synchronized void load() {
            if (getConfigFile().exists()) {
                super.load();
            } else {
                migrateFromOldConfigFile(StartETBuilder.DescriptorImpl.class);
                migrateFromOldConfigFile(StopETBuilder.DescriptorImpl.class);
                migrateFromOldConfigFile(StartTSBuilder.DescriptorImpl.class);
                migrateFromOldConfigFile(StopTSBuilder.DescriptorImpl.class);
                migrateFromOldConfigFile(JUnitPublisher.DescriptorImpl.class);
                migrateFromOldConfigFile(ReportGeneratorPublisher.DescriptorImpl.class);
                save();
            }
        }

        /**
         * Moves the configured installations from old descriptor implementations to this descriptor in order to retain
         * backward compatibility. Old configuration files will be removed automatically.
         *
         * @param oldClass the old descriptor class name
         * @since 1.12
         */
        @SuppressWarnings("rawtypes")
        private void migrateFromOldConfigFile(final Class oldClass) {
            LOGGER.log(Level.FINE, "Migrating ECU-TEST installations from: " + oldClass.getName());

            final XStream2 stream = new XStream2();
            stream.addCompatibilityAlias(oldClass.getName(), getClass());

            final XmlFile file = new XmlFile(stream,
                    new File(Jenkins.get().getRootDir(), oldClass.getEnclosingClass().getName() + ".xml"));
            if (file.exists()) {
                try {
                    file.unmarshal(this);
                } catch (final IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to migrate ECU-TEST installations from: " + file, e);
                } finally {
                    file.delete();
                }
            }
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ETInstallation_DisplayName();
        }

        @Override
        public ETInstallation[] getInstallations() {
            return installations.clone();
        }

        @Override
        public void setInstallations(final ETInstallation... installations) {
            // Remove empty installations
            final List<ETInstallation> inst = new ArrayList<>();
            if (installations != null) {
                Collections.addAll(inst, installations);
                for (final ETInstallation installation : installations) {
                    if (StringUtils.isBlank(installation.getName())) {
                        inst.remove(installation);
                    }
                }
            }
            this.installations = inst.toArray(new ETInstallation[0]);
            save();
        }

        /**
         * Gets the ECU-TEST installation specified by name.
         *
         * @param name the installation name
         * @return the installation or {@code null} if not found
         */
        @CheckForNull
        public ETInstallation getInstallation(final String name) {
            for (final ETInstallation installation : getInstallations()) {
                if (installation.getName().equals(name)) {
                    return installation;
                }
            }
            return null;
        }

        @Override
        public FormValidation doCheckHome(@QueryParameter final File value) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            FormValidation returnValue = FormValidation.ok();
            if (!Functions.isWindows()) {
                returnValue = FormValidation.warning(Messages.ETInstallation_IsUnixSystem());
            } else if (StringUtils.isNotEmpty(value.toString())) {
                if (value.isDirectory()) {
                    final File etExe = getExeFile(value);
                    if (!etExe.exists()) {
                        returnValue = FormValidation.error(Messages.ETInstallation_NotHomeDirectory(value));
                    }
                } else {
                    returnValue = FormValidation.error(Messages.ETInstallation_NotADirectory(value));
                }
            } else {
                returnValue = FormValidation.warning(Messages.ETInstallation_NotRequired());
            }
            return returnValue;
        }
    }
}
