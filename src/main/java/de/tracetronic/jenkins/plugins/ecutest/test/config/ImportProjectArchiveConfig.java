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
import hudson.util.FormValidation;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;

/**
 * Class holding the configuration for importing a project from an archive.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectArchiveConfig extends ImportProjectConfig {

    private static final long serialVersionUID = 1L;

    private final String importConfigPath;
    private final boolean replaceFiles;

    /**
     * Instantiates a new {@link ImportProjectArchiveConfig}.
     *
     * @param projectPath
     *            the archive path
     * @param importPath
     *            the import path
     * @param importConfigPath
     *            the import configuration path
     * @param replaceFiles
     *            specifies whether to replace files
     */
    @DataBoundConstructor
    public ImportProjectArchiveConfig(final String projectPath, final String importPath, final String importConfigPath,
            final boolean replaceFiles) {
        super(projectPath, importPath);
        this.importConfigPath = StringUtils.trimToEmpty(importConfigPath);
        this.replaceFiles = replaceFiles;
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
        final String expProjectPath = envVars.expand(getProjectPath());
        final String expImportPath = envVars.expand(getImportPath());
        final String expImportConfigPath = envVars.expand(getImportConfigPath());
        return new ImportProjectArchiveConfig(expProjectPath, expImportPath, expImportConfigPath, isReplaceFiles());
    }

    /**
     * DescriptorImpl for {@link ImportProjectArchiveConfig}.
     */
    @Extension(ordinal = 3)
    public static class DescriptorImpl extends ImportProjectConfig.DescriptorImpl {

        @Override
        public FormValidation doCheckProjectPath(@QueryParameter final String value) {
            return importValidator.validateArchivePath(value);
        }

        /**
         * Validates the import configuration target path.
         *
         * @param value
         *            the import configuration path
         * @return the form validation
         */
        public FormValidation doCheckImportConfigPath(@QueryParameter final String value) {
            return importValidator.validateImportConfigPath(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.ImportProjectArchiveConfig_DisplayName();
        }

    }

}
