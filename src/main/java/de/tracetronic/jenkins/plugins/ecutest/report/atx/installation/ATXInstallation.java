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
     * @param name
     *            the name
     * @param toolName
     *            the tool name
     * @param config
     *            the configuration
     */
    @DataBoundConstructor
    public ATXInstallation(final String name, final String toolName, final ATXConfig config) {
        this.name = StringUtils.trimToEmpty(name);
        this.toolName = toolName;
        this.config = config == null ? new ATXConfig() : config;

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
     * @param name
     *            the name
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
}
