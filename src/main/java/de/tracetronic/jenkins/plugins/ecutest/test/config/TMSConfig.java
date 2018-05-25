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
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Item;
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
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.TMSValidator;

/**
 * Base configuration class for connecting to test management systems.
 * 
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class TMSConfig implements Describable<TMSConfig>, Serializable, ExpandableConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Defines the default timeout for connection to test management system.
     */
    protected static final int DEFAULT_TIMEOUT = 60;

    private final String credentialsId;
    private final String timeout;

    /**
     * Instantiates a new {@code TMSConfig}.
     *
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the timeout
     */
    public TMSConfig(final String credentialsId, final String timeout) {
        super();
        this.credentialsId = StringUtils.trimToEmpty(credentialsId);
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(DEFAULT_TIMEOUT));
    }

    /**
     * @return the credentials id
     */
    public String getCredentialsId() {
        return credentialsId;
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
     * Gets the credentials providing access to user name and password.
     *
     * @param project
     *            the project
     * @return the credentials
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    @CheckForNull
    public StandardUsernamePasswordCredentials getCredentials(final Item project) throws IOException,
            InterruptedException {
        final List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider
                .lookupCredentials(StandardUsernamePasswordCredentials.class, project, ACL.SYSTEM,
                        Collections.<DomainRequirement> emptyList());
        return CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(credentialsId));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(getClass());
    }

    /**
     * Gets all descriptors of {@link ImportProjectConfig} type.
     *
     * @return the descriptor extension list
     */
    public static DescriptorExtensionList<TMSConfig, Descriptor<TMSConfig>> all() {
        return Jenkins.getInstance().getDescriptorList(TMSConfig.class);
    }

    /**
     * DescriptorImpl for {@link TMSConfig}.
     */
    public abstract static class DescriptorImpl extends Descriptor<TMSConfig> {

        /**
         * Validator to check form fields.
         */
        protected final TMSValidator tmsValidator = new TMSValidator();

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * Validates the timeout.
         *
         * @param value
         *            the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return tmsValidator.validateTimeout(value, getDefaultTimeout());
        }

        /**
         * Fills the credentials drop-down menu.
         *
         * @param item
         *            the item
         * @param credentialsId
         *            the credentials id
         * @return the credentials items
         */
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath final Item item,
                @QueryParameter final String credentialsId) {
            final StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                    .includeEmptyValue()
                    .includeMatchingAs(ACL.SYSTEM, item, StandardCredentials.class,
                            Collections.<DomainRequirement> emptyList(),
                            CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
        }
    }
}
