/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;

import java.io.IOException;
import java.util.List;

/**
 * Represents a base tool installation specified by name and home directory.
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
     * Gets the executable file path of the tool on the given target system.
     *
     * @param launcher the launcher
     * @return the executable file path or {@code null} if home directory is not set or file does not exist
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public abstract String getExecutable(Launcher launcher) throws IOException, InterruptedException;
}
