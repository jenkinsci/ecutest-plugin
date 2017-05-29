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

import hudson.util.FormValidation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.QueryParameter;

/**
 * Common base class for {@link ImportPackageConfig} and {@link ImportProjectConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class ImportConfig extends TMSConfig {

    private static final long serialVersionUID = 1L;

    private final String tmsPath;
    private final String importPath;

    /**
     * @deprecated since 1.17
     */
    @Deprecated
    private transient String projectPath;

    /**
     * Instantiates a new {@link ImportConfig}.
     *
     * @param tmsPath
     *            the file path
     * @param importPath
     *            the export path
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the timeout
     */
    public ImportConfig(final String tmsPath, final String importPath,
            final String credentialsId, final String timeout) {
        super(credentialsId, timeout);
        this.tmsPath = StringUtils.trimToEmpty(tmsPath);
        this.importPath = StringUtils.trimToEmpty(importPath);
    }

    /**
     * @deprecated since 1.17
     * @return the project path
     */
    @Deprecated
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * @return the path in the test management system
     */
    public String getTmsPath() {
        return tmsPath;
    }

    /**
     * @return the import target path
     */
    public String getImportPath() {
        return importPath;
    }

    @Override
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public boolean equals(final Object other) {
        boolean result = false;
        if (canEqual(other) && other instanceof ImportConfig) {
            final ImportConfig that = (ImportConfig) other;
            final String tmsPath = getTmsPath();
            final String importPath = getImportPath();
            final String thatFilePath = that.getTmsPath();
            final String thatImportPath = that.getImportPath();
            result = that.canEqual(this)
                    && (tmsPath == null ? thatFilePath == null : tmsPath.equals(thatFilePath))
                    && (importPath == null ? thatImportPath == null : importPath.equals(thatImportPath))
                    && (getCredentialsId() == null ? that.getCredentialsId() == null :
                        getCredentialsId().equals(that.getCredentialsId()))
                        && (getTimeout() == null ? that.getTimeout() == null :
                            getTimeout().equals(that.getTimeout()));
        }
        return result;
    }

    /**
     * Implementation according to <a href="www.artima.com/lejava/articles/equality.html">Equality Pitfall #4</a>.
     *
     * @param other
     *            the other object
     * @return {@code true} if the other object is an instance of the class in which canEqual is (re)defined,
     *         {@code false} otherwise.
     */
    public boolean canEqual(final Object other) {
        return other instanceof ImportConfig;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(getTmsPath()).append(getImportPath()).append(getCredentialsId())
                .append(getTimeout()).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ImportConfig}.
     */
    public abstract static class DescriptorImpl extends TMSConfig.DescriptorImpl {

        /**
         * Validates the file path in the test management system.
         *
         * @param value
         *            the file path
         * @return the form validation
         */
        public abstract FormValidation doCheckTmsPath(@QueryParameter String value);

        /**
         * Validates the import target path.
         *
         * @param value
         *            the import path
         * @return the form validation
         */
        public FormValidation doCheckImportPath(@QueryParameter final String value) {
            return tmsValidator.validateImportPath(value);
        }
    }
}
