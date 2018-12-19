/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import jenkins.security.MasterToSlaveCallable;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Represents a base tool installation specified by name and home directory.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractToolInstallation extends ToolInstallation implements
    EnvironmentSpecific<AbstractToolInstallation>, NodeSpecific<AbstractToolInstallation> {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link AbstractToolInstallation}.
     *
     * @param name       the name of the tool
     * @param home       the home directory of the tool
     * @param properties the tool properties
     */
    public AbstractToolInstallation(final String name, final String home,
                                    final List<? extends ToolProperty<?>> properties) {
        super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), properties);
    }

    /**
     * Instantiates a new {@link AbstractToolInstallation}.
     *
     * @param source     the source to install the tool
     * @param home       the home directory of the tool
     * @param properties the tool properties
     */
    public AbstractToolInstallation(final AbstractToolInstallation source, final String home,
                                    final List<? extends ToolProperty<?>> properties) {
        super(Util.fixEmptyAndTrim(source.getName()), Util.fixEmptyAndTrim(home), properties);
    }

    /**
     * Gets the executable path of the tool on the given target system.
     *
     * @param launcher the launcher
     * @return the executable
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public String getExecutable(final Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new MasterToSlaveCallable<String, IOException>() {

            private static final long serialVersionUID = 1L;

            @Override
            public String call() throws IOException {
                final File exe = getExeFile();
                return exe != null && exe.exists() ? exe.getPath() : null;
            }
        });
    }

    /**
     * Gets the expanded executable file path.
     *
     * @return the executable file path or {@code null} if home directory is not set
     */
    @CheckForNull
    private File getExeFile() {
        if (getHome() != null) {
            final String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
            if (home != null) {
                return getExeFile(new File(home));
            }
        }
        return null;
    }

    /**
     * Gets the executable file relative to given home directory.
     *
     * @param home the home directory of the tool
     * @return the executable file
     */
    protected abstract File getExeFile(File home);

}
