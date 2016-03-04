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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.tools.ToolDescriptor;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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
    private static final String TS_EXECUTABLE = "ToolServer/Tool-Server.exe";

    private transient String installationPath;

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
    protected Object readResolve() {
        if (installationPath != null) {
            return new ETInstallation(getName(), installationPath, null);
        }
        return this;
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
     * @return the Tool-Server executable file
     */
    protected File getTSExeFile(final File home) {
        return DescriptorImpl.getTSExeFile(home);
    }

    /**
     * Gets the executable path of the Tool-Server on the given target system.
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
        return launcher.getChannel().call(new Callable<String, IOException>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String call() throws IOException {
                final File exe = getTSExeFile();
                return exe != null && exe.exists() ? exe.getPath() : null;
            }
        });
    }

    /**
     * Gets the expanded Tool-Server executable file path.
     *
     * @return the Tool-Server executable file path or {@code null} if home directory is not set
     */
    @CheckForNull
    private File getTSExeFile() {
        if (getHome() != null) {
            final String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
            return getTSExeFile(new File(home));
        }
        return null;
    }

    /**
     * DescriptorImpl of {@link ETInstallation}.
     */
    @Extension(ordinal = 1001)
    public static class DescriptorImpl extends ToolDescriptor<ETInstallation> {

        @Override
        public String getDisplayName() {
            return "ECU-TEST";
        }

        @Override
        public ETInstallation[] getInstallations() {
            final Jenkins instance = Jenkins.getInstance();
            if (instance != null) {
                // Take available installations from one of the tool descriptors
                final ETInstallation[] startInstallations = instance.getDescriptorByType(
                        StartETBuilder.DescriptorImpl.class).getInstallations();
                return startInstallations;
            }
            return new ETInstallation[0];
        }

        @Override
        public void setInstallations(final ETInstallation... installations) {
            final Jenkins instance = Jenkins.getInstance();
            if (instance != null) {
                // Set installations to all descriptors
                instance.getDescriptorByType(StartETBuilder.DescriptorImpl.class).setInstallations(installations);
                instance.getDescriptorByType(StopETBuilder.DescriptorImpl.class).setInstallations(installations);
                instance.getDescriptorByType(StartTSBuilder.DescriptorImpl.class).setInstallations(installations);
                instance.getDescriptorByType(StopTSBuilder.DescriptorImpl.class).setInstallations(installations);
                instance.getDescriptorByType(JUnitPublisher.DescriptorImpl.class).setInstallations(installations);
            }
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return Collections.emptyList();
        }

        @Override
        public FormValidation doCheckName(@QueryParameter final String value) {
            return FormValidation.validateRequired(value);
        }

        @Override
        public FormValidation doCheckHome(@QueryParameter final File value) {
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
         * @return the executable file or {@code null} if Unix-based system
         */
        @CheckForNull
        private static File getTSExeFile(final File home) {
            if (Functions.isWindows() && home != null) {
                return new File(home, TS_EXECUTABLE);
            }
            return null;
        }
    }
}
