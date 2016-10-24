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
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.tools.ToolProperty;
import hudson.tools.ToolDescriptor;
import hudson.util.FormValidation;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.junit.JUnitPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;

/**
 * Represents a ECU-TEST installation specified by name and home directory.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETInstallation extends AbstractToolInstallation {

    private static final long serialVersionUID = 1L;

    /**
     * Executable file name of ECU-TEST.
     */
    private static final String EXECUTABLE = "ECU-TEST.exe";

    /**
     * Executable file name of Tool-Server.
     */
    private static final String TS_EXECUTABLE = "Tool-Server.exe";

    /**
     * Instantiates a new {@link ETInstallation}.
     *
     * @param name
     *            the name of the ECU-TEST installation
     * @param home
     *            the home directory of the ECU-TEST installation
     * @param properties
     *            the ECU-TEST properties
     */
    @DataBoundConstructor
    public ETInstallation(final String name, final String home, final List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /**
     * Instantiates a new {@link ETInstallation}.
     *
     * @param source
     *            the source to install the ECU-TEST installation
     * @param home
     *            the home directory of the ECU-TEST installation
     * @param properties
     *            the ECU-TEST properties
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
    public ETInstallation forNode(final Node node, final TaskListener log) throws IOException, InterruptedException {
        return new ETInstallation(this, translateFor(node, log), getProperties().toList());
    }

    @Override
    protected File getExeFile(final File home) {
        return DescriptorImpl.getExeFile(home);
    }

    /**
     * Gets the Tool-Server executable file relative to given home directory.
     *
     * @param home
     *            the home directory of the tool
     * @param subDir
     *            the sub directory relative to home directory
     * @return the Tool-Server executable file
     */
    protected File getTSExeFile(final File home, final String subDir) {
        return DescriptorImpl.getTSExeFile(home, subDir);
    }

    /**
     * Gets the executable path of the Tool-Server on the given target system.
     * According to ECU-TEST 6.5 and above the Tool-Server executable is directly
     * located in ECU-TEST installation path, otherwise in sub directory 'ToolServer'.
     *
     * @param launcher
     *            the launcher
     * @return the Tool-Server executable path
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    public String getTSExecutable(final Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new MasterToSlaveCallable<String, IOException>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String call() throws IOException {
                File exe = getTSExeFile("");
                if (exe == null || !exe.exists()) {
                    exe = getTSExeFile("ToolServer");
                }
                return exe != null && exe.exists() ? exe.getPath() : null;
            }
        });
    }

    /**
     * Gets the expanded Tool-Server executable file path.
     *
     * @param subDir
     *            the sub directory relative to home directory
     * @return the Tool-Server executable file path or {@code null} if home directory is not set
     */
    @CheckForNull
    private File getTSExeFile(final String subDir) {
        if (getHome() != null) {
            final String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
            return getTSExeFile(new File(home), subDir);
        }
        return null;
    }

    /**
     * Gets the programmatic identifier from tool properties.
     *
     * @return the progId, default progId if tool property does not exist
     */
    public String getProgId() {
        String progId;
        final ETToolProperty toolProperty = getProperties().get(ETToolProperty.class);
        if (toolProperty != null) {
            progId = toolProperty.getProgId();
        } else {
            progId = ETToolProperty.DescriptorImpl.getDefaultProgId();
        }
        return progId;
    }

    /**
     * DescriptorImpl of {@link ETInstallation}.
     */
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends ToolDescriptor<ETInstallation> {

        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        @CopyOnWrite
        private volatile ETInstallation[] installations = new ETInstallation[0];

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
            load();
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
         * Moves the configured installations from old descriptor implementations to this descriptor
         * in order to retain backward compatibility. Old configuration files will be removed automatically.
         *
         * @param oldClass
         *            the old descriptor class name
         * @since 1.12
         */
        @SuppressWarnings("rawtypes")
        private void migrateFromOldConfigFile(final Class oldClass) {
            LOGGER.log(Level.FINE, "Migrating ECU-TEST installations from: " + oldClass.getName());

            final XStream2 stream = new XStream2();
            stream.addCompatibilityAlias(oldClass.getName(), getClass());

            final Jenkins instance = Jenkins.getInstance();
            if (instance != null) {
                final XmlFile file = new XmlFile(stream,
                        new File(instance.getRootDir(), oldClass.getEnclosingClass().getName() + ".xml"));
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
        }

        @Override
        public String getDisplayName() {
            return Messages.ET_DisplayName();
        }

        @Override
        public ETInstallation[] getInstallations() {
            return installations.clone();
        }

        @Override
        public void setInstallations(final ETInstallation... installations) {
            // Remove empty installations
            final List<ETInstallation> inst = new ArrayList<ETInstallation>();
            if (installations != null) {
                Collections.addAll(inst, installations);
                for (final ETInstallation installation : installations) {
                    if (StringUtils.isBlank(installation.getName())) {
                        inst.remove(installation);
                    }
                }
            }
            this.installations = inst.toArray(new ETInstallation[inst.size()]);
            save();
        }

        /**
         * Gets the ECU-TEST installation specified by name.
         *
         * @param name
         *            the installation name
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
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            FormValidation returnValue = FormValidation.ok();
            if (!Functions.isWindows()) {
                returnValue = FormValidation.warning(Messages.ET_IsUnixSystem());
            } else if (StringUtils.isNotEmpty(value.toString())) {
                if (value.isDirectory()) {
                    final File etExe = getExeFile(value);
                    if (etExe == null || !etExe.exists()) {
                        returnValue = FormValidation.error(Messages.ET_NotHomeDirectory(value));
                    }
                } else {
                    returnValue = FormValidation.error(Messages.ET_NotADirectory(value));
                }
            } else {
                returnValue = FormValidation.warning(Messages.ET_NotRequired());
            }
            return returnValue;
        }

        /**
         * Gets the ECU-TEST executable file.
         *
         * @param home
         *            the home directory of ECU-TEST
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
         * Gets the Tool-Server executable file.
         *
         * @param home
         *            the home directory of ECU-TEST
         * @param subDir
         *            the sub directory relative to home directory
         * @return the executable file or {@code null} if Unix-based system
         */
        @CheckForNull
        private static File getTSExeFile(final File home, final String subDir) {
            if (Functions.isWindows() && home != null && subDir != null) {
                return new File(new File(home, subDir), TS_EXECUTABLE);
            }
            return null;
        }
    }
}
