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

import hudson.EnvVars;
import hudson.Extension;
import hudson.util.FormValidation;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;

/**
 * Class holding the configuration for importing a project from test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectConfig extends ImportConfig {

    private static final long serialVersionUID = 1L;

    /**
     * @since 1.17
     */
    private final boolean importMissingPackages;

    /**
     * @deprecated since 1.17
     */
    @Deprecated
    private transient String projectPath;

    /**
     * Instantiates a new {@link ImportProjectConfig}.
     *
     * @param tmsPath
     *            the project path in test management system
     * @param importPath
     *            the import path
     * @param importMissingPackages
     *            specifies whether to import missing packages
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the import timeout
     */
    @DataBoundConstructor
    public ImportProjectConfig(final String tmsPath, final String importPath,
            final boolean importMissingPackages, final String credentialsId, final String timeout) {
        super(tmsPath, importPath, credentialsId, timeout);
        this.importMissingPackages = importMissingPackages;
    }

    /**
     * Convert legacy configuration into the new class structure.
     *
     * @return an instance of this class with all the new fields transferred from the old structure to the new one
     */
    public final Object readResolve() {
        if (projectPath != null) {
            return new ImportProjectConfig(projectPath, getImportPath(), isImportMissingPackages(),
                    getCredentialsId(), getTimeout());
        }
        return this;
    }

    /**
     * @return specifies whether to import missing packages
     */
    public boolean isImportMissingPackages() {
        return importMissingPackages;
    }

    @Override
    public ImportProjectConfig expand(final EnvVars envVars) {
        final String expTmsPath = envVars.expand(getTmsPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expCredentialsId = envVars.expand(getCredentialsId());
        final String expTimeout = EnvUtil.expandEnvVar(getTimeout(), envVars, String.valueOf(DEFAULT_TIMEOUT));
        return new ImportProjectConfig(expTmsPath, expImportPath, isImportMissingPackages(),
                expCredentialsId, expTimeout);
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ImportProjectConfig) {
            final ImportProjectConfig that = (ImportProjectConfig) other;
            result = that.canEqual(this) && super.equals(that) && importMissingPackages == that.importMissingPackages;
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ImportProjectConfig;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode()).append(importMissingPackages).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ImportProjectConfig}.
     */
    @Extension(ordinal = 3)
    public static class DescriptorImpl extends ImportConfig.DescriptorImpl {

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        @Override
        public FormValidation doCheckTmsPath(@QueryParameter final String value) {
            return tmsValidator.validateTestPath(value);
        }

        /**
         * Validates the timeout.
         *
         * @param value
         *            the timeout
         * @return the form validation
         */
        @Override
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return tmsValidator.validateTimeout(value, getDefaultTimeout());
        }

        @Override
        public String getDisplayName() {
            return Messages.ImportProjectConfig_DisplayName();
        }
    }
}
