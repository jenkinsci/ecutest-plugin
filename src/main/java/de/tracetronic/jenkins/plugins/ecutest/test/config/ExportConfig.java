/*
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

import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ExportTestValidator;

/**
 * Common base class for {@link ExportPackageConfig} and {@link ExportProjectConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ExportConfig implements Describable<ExportConfig>, Serializable, ExpandableConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Defines the default timeout for exports.
     */
    protected static final int DEFAULT_TIMEOUT = 60;

    private final String filePath;
    private final String exportPath;
    private final String credentialsId;
    private final String timeout;

    /**
     * Instantiates a new {@link ExportConfig}.
     *
     * @param filePath
     *            the file path
     * @param exportPath
     *            the export path
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the timeout
     */
    public ExportConfig(final String filePath, final String exportPath,
            final String credentialsId, final String timeout) {
        super();
        this.filePath = StringUtils.trimToEmpty(filePath);
        this.exportPath = StringUtils.trimToEmpty(exportPath);
        this.credentialsId = StringUtils.trimToEmpty(credentialsId);
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(DEFAULT_TIMEOUT));
    }

    /**
     * @return the file path to export
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return the export target path
     */
    public String getExportPath() {
        return exportPath;
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

    @CheckForNull
    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<ExportConfig> getDescriptor() {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            return (Descriptor<ExportConfig>) instance.getDescriptor(getClass());
        }
        return null;
    }

    /**
     * Gets all descriptors of {@link ExportConfig} type.
     *
     * @return the descriptor extension list
     */
    @CheckForNull
    public static DescriptorExtensionList<ExportConfig, Descriptor<ExportConfig>> all() {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            return instance.getDescriptorList(ExportConfig.class);
        }
        return null;
    }

    @Override
    public ExportPackageConfig expand(final EnvVars envVars) {
        final String expPackagePath = envVars.expand(getFilePath());
        final String expExportPath = envVars.expand(getExportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ExportPackageConfig(expPackagePath, expExportPath, expCredentialsId, expTimeout);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ExportPackageConfig) {
            final ExportPackageConfig that = (ExportPackageConfig) other;
            final String filePath = getFilePath();
            final String exportPath = getExportPath();
            final String thatFilePath = that.getFilePath();
            final String thatExportPath = that.getExportPath();
            result = (filePath == null ? thatFilePath == null : filePath.equals(thatFilePath))
                    && (exportPath == null ? thatExportPath == null : exportPath.equals(thatExportPath))
                    && (credentialsId == null ? that.getCredentialsId() == null :
                            credentialsId.equals(that.getCredentialsId()))
                    && (timeout == null ? that.getTimeout() == null : timeout.equals(that.getTimeout()));
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(getFilePath()).append(getExportPath()).append(credentialsId)
                .append(timeout).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ExportConfig}.
     */
    public abstract static class DescriptorImpl extends Descriptor<ExportConfig> {

        /**
         * Validator to check form fields.
         */
        protected final ExportTestValidator exportValidator = new ExportTestValidator();

        /**
         * Validates the file path to export.
         *
         * @param value
         *            the file path to export
         * @return the form validation
         */
        public abstract FormValidation doCheckFilePath(@QueryParameter String value);

        /**
         * Validates the export target path.
         *
         * @param value
         *            the export path
         * @return the form validation
         */
        public FormValidation doCheckExportPath(@QueryParameter final String value) {
            return exportValidator.validateExportPath(value);
        }

        /**
         * Fills the credentials drop-down menu.
         *
         * @return the credentials items
         */
        public ListBoxModel doFillCredentialsIdItems() {
            return new StandardListBoxModel().withEmptySelection().withMatching(
                    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class),
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
                            Collections.<DomainRequirement> emptyList()));
        }
    }
}
