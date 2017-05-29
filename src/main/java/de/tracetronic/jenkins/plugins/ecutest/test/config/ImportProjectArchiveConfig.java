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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;

/**
 * Class holding the configuration for importing a project from an archive.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectArchiveConfig extends ImportConfig {

    private static final long serialVersionUID = 1L;

    private final String importConfigPath;
    private final boolean replaceFiles;

    /**
     * @deprecated since 1.17
     */
    @Deprecated
    private transient String projectPath;

    /**
     * Instantiates a new {@link ImportProjectArchiveConfig}.
     *
     * @param tmsPath
     *            the project path
     * @param importPath
     *            the import path
     * @param importConfigPath
     *            the import config path
     * @param replaceFiles
     *            the replace files
     */
    @DataBoundConstructor
    public ImportProjectArchiveConfig(final String tmsPath, final String importPath,
            final String importConfigPath, final boolean replaceFiles) {
        super(tmsPath, importPath, null, null);
        this.importConfigPath = StringUtils.trimToEmpty(importConfigPath);
        this.replaceFiles = replaceFiles;
    }

    /**
     * Convert legacy configuration into the new class structure.
     *
     * @return an instance of this class with all the new fields transferred from the old structure to the new one
     */
    public final Object readResolve() {
        if (projectPath != null) {
            return new ImportProjectArchiveConfig(projectPath, getImportPath(), getImportConfigPath(), isReplaceFiles());
        }
        return this;
    }

    /**
     * @return the import configuration path
     */
    public String getImportConfigPath() {
        return importConfigPath;
    }

    /**
     * @return {@code true} when existing files should be overwritten, {@code false} otherwise
     */
    public boolean isReplaceFiles() {
        return replaceFiles;
    }

    @Override
    public ImportProjectArchiveConfig expand(final EnvVars envVars) {
        final String expProjectPath = envVars.expand(getTmsPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expImportConfigPath = envVars.expand(getImportConfigPath());
        return new ImportProjectArchiveConfig(expProjectPath, expImportPath, expImportConfigPath, isReplaceFiles());
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof ImportProjectArchiveConfig) {
            final ImportProjectArchiveConfig that = (ImportProjectArchiveConfig) other;
            result = that.canEqual(this)
                    && super.equals(that)
                    && (importConfigPath == null ? that.importConfigPath == null : importConfigPath
                    .equals(that.importConfigPath))
                    && replaceFiles == that.replaceFiles;
        }
        return result;
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ImportProjectArchiveConfig;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(super.hashCode())
                .append(importConfigPath).append(replaceFiles).toHashCode();
    }

    /**
     * DescriptorImpl for {@link ImportProjectArchiveConfig}.
     */
    @Extension(ordinal = 4)
    public static class DescriptorImpl extends ImportConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckTmsPath(@QueryParameter final String value) {
            return tmsValidator.validateArchivePath(value);
        }

        /**
         * Validates the import configuration target path.
         *
         * @param value
         *            the import configuration path
         * @return the form validation
         */
        public FormValidation doCheckImportConfigPath(@QueryParameter final String value) {
            return tmsValidator.validateImportConfigPath(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.ImportProjectArchiveConfig_DisplayName();
        }
    }
}
