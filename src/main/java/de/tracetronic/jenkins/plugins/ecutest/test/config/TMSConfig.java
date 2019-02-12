/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TMSValidator;
import hudson.DescriptorExtensionList;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

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
     * @param credentialsId the credentials id
     * @param timeout       the timeout
     */
    public TMSConfig(final String credentialsId, final String timeout) {
        super();
        this.credentialsId = StringUtils.trimToEmpty(credentialsId);
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(DEFAULT_TIMEOUT));
    }

    /**
     * @return the default timeout
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
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
     * Gets the credentials providing access to user name and password.
     *
     * @param project the project
     * @return the credentials
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException the interrupted exception
     */
    @CheckForNull
    public StandardUsernamePasswordCredentials getCredentials(final Item project) throws IOException,
        InterruptedException {
        final List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider
            .lookupCredentials(StandardUsernamePasswordCredentials.class, project, ACL.SYSTEM,
                Collections.emptyList());
        return CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(credentialsId));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(getClass());
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
         * @param value the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return tmsValidator.validateTimeout(value, getDefaultTimeout());
        }

        /**
         * Fills the credentials drop-down menu.
         *
         * @param item          the item
         * @param credentialsId the credentials id
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
                    Collections.emptyList(),
                    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
        }
    }
}
