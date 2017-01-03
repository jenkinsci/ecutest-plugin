/**
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.EnvVars;
import hudson.Extension;
import hudson.security.ACL;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;

/**
 * Class holding the configuration for importing a project from test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectTMSConfig extends ImportProjectConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Defines the default timeout for importing a project.
     */
    protected static final int DEFAULT_TIMEOUT = 60;

    private final String credentialsId;
    private final String timeout;

    /**
     * Instantiates a new {@link ImportProjectTMSConfig}.
     *
     * @param projectPath
     *            the project path in test management system
     * @param importPath
     *            the import path
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the import timeout
     */
    @DataBoundConstructor
    public ImportProjectTMSConfig(final String projectPath, final String importPath, final String credentialsId,
            final String timeout) {
        super(projectPath, importPath);
        this.credentialsId = StringUtils.trimToEmpty(credentialsId);
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(DEFAULT_TIMEOUT));
    }

    /**
     * @return the timeout as integer
     */
    public int getParsedTimeout() {
        return ExecutionConfig.parse(getTimeout());
    }

    /**
     * @return the timeout as string
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @return the default timeout
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the credentials id used for authentication
     */
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Gets the credentials providing access to user name and password.
     *
     * @return the credentials
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    @CheckForNull
    public StandardUsernamePasswordCredentials getCredentials() throws IOException, InterruptedException {
        final List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
                Collections.<DomainRequirement> emptyList());
        return CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(credentialsId));
    }

    @Override
    public ImportProjectTMSConfig expand(final EnvVars envVars) {
        final String expProjectPath = envVars.expand(getProjectPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ImportProjectTMSConfig(expProjectPath, expImportPath, expCredentialsId, expTimeout);
    }

    /**
     * DescriptorImpl for {@link ImportProjectTMSConfig}.
     */
    @Extension(ordinal = 2)
    public static class DescriptorImpl extends ImportProjectConfig.DescriptorImpl {

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Override
        public FormValidation doCheckProjectPath(@QueryParameter final String value) {
            return importValidator.validateProjectPath(value);
        }

        /**
         * Validates the timeout.
         *
         * @param value
         *            the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return importValidator.validateTimeout(value, getDefaultTimeout());
        }

        @Override
        public String getDisplayName() {
            return Messages.ImportProjectTMSConfig_DisplayName();
        }
    }
}
