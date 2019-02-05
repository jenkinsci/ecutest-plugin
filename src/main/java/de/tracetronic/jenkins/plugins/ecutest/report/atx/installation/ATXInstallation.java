/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.ATXPublisher;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import java.io.Serializable;

/**
 * Class holding all the ATX settings.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXInstallation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String toolName;
    private final ATXConfig config;

    /**
     * Instantiates a new {@link ATXInstallation}.
     *
     * @param name     the name
     * @param toolName the tool name
     * @param config   the configuration
     */
    @DataBoundConstructor
    public ATXInstallation(final String name, final String toolName, final ATXConfig config) {
        this.name = StringUtils.trimToEmpty(name);
        this.toolName = toolName;
        this.config = config == null ? new ATXConfig() : config;

    }

    /**
     * Gets all ATX installations.
     *
     * @return all available installations, never {@code null}
     */
    public static ATXInstallation[] all() {
        final Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance == null) {
            return new ATXInstallation[0];
        }
        final ATXPublisher.DescriptorImpl atxDescriptor = instance
            .getDescriptorByType(ATXPublisher.DescriptorImpl.class);
        return atxDescriptor.getInstallations();
    }

    /**
     * Gets the ATX installation by name.
     *
     * @param name the name
     * @return installation by name, {@code null} if not found
     */
    @CheckForNull
    public static ATXInstallation get(final String name) {
        final ATXInstallation[] installations = all();
        for (final ATXInstallation installation : installations) {
            if (StringUtils.equals(name, installation.getName())) {
                return installation;
            }
        }
        return null;
    }

    /**
     * @return the name of the installation
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the tool name.
     *
     * @return the toolName
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the configuration
     */
    public ATXConfig getConfig() {
        return config;
    }
}
