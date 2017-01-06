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

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.Serializable;
import java.util.Collections;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.ImportProjectValidator;

/**
 * Common base class for {@link ImportProjectArchiveConfig}, {@link ImportProjectTMSConfig} and
 * {@link ImportProjectDirTMSConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ImportProjectConfig implements Describable<ImportProjectConfig>, Serializable, ExpandableConfig {

    private static final long serialVersionUID = 1L;

    private final String projectPath;
    private final String importPath;

    /**
     * Instantiates a new {@link ImportProjectConfig}.
     *
     * @param projectPath
     *            the project path
     * @param importPath
     *            the import path
     */
    public ImportProjectConfig(final String projectPath, final String importPath) {
        super();
        this.projectPath = StringUtils.trimToEmpty(projectPath);
        this.importPath = StringUtils.trimToEmpty(importPath);
    }

    /**
     * @return the project path to import
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * @return the import target path
     */
    public String getImportPath() {
        return importPath;
    }

    @CheckForNull
    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<ImportProjectConfig> getDescriptor() {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            return (Descriptor<ImportProjectConfig>) instance.getDescriptor(getClass());
        }
        return null;
    }

    /**
     * Gets all descriptors of {@link ImportProjectConfig} type.
     *
     * @return the descriptor extension list
     */
    @CheckForNull
    public static DescriptorExtensionList<ImportProjectConfig, Descriptor<ImportProjectConfig>> all() {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            return instance.getDescriptorList(ImportProjectConfig.class);
        }
        return null;
    }

    /**
     * DescriptorImpl for {@link ImportProjectConfig}.
     */
    public abstract static class DescriptorImpl extends Descriptor<ImportProjectConfig> {

        /**
         * Validator to check form fields.
         */
        protected final ImportProjectValidator importValidator = new ImportProjectValidator();

        /**
         * Validates the project path to import.
         *
         * @param value
         *            the project to import
         * @return the form validation
         */
        public abstract FormValidation doCheckProjectPath(@QueryParameter String value);

        /**
         * Validates the import target path.
         *
         * @param value
         *            the import path
         * @return the form validation
         */
        public FormValidation doCheckImportPath(@QueryParameter final String value) {
            return importValidator.validateImportPath(value);
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
