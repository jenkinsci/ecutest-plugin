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

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import hudson.EnvVars;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

/**
 * Class holding the configuration for importing a project directory from test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportPackageDirConfig extends ImportPackageConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ImportPackageDirConfig}.
     *
     * @param tmsPath
     *            the package directory path in test management system
     * @param importPath
     *            the import path
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the import timeout
     */
    @DataBoundConstructor
    public ImportPackageDirConfig(final String tmsPath, final String importPath,
            final String credentialsId, final String timeout) {
        super(tmsPath, importPath, credentialsId, timeout);
    }

    @Override
    public ImportPackageDirConfig expand(final EnvVars envVars) {
        final ImportPackageConfig config = super.expand(envVars);
        return new ImportPackageDirConfig(config.getTmsPath(), config.getImportPath(),
                config.getCredentialsId(), config.getTimeout());
    }

    @Override
    public final boolean canEqual(final Object other) {
        return other instanceof ImportPackageDirConfig;
    }

    /**
     * DescriptorImpl for {@link ImportPackageDirConfig}.
     */
    @Extension(ordinal = 2)
    public static class DescriptorImpl extends ImportPackageConfig.DescriptorImpl {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportPackageDirConfig_DisplayName();
        }
    }
}
