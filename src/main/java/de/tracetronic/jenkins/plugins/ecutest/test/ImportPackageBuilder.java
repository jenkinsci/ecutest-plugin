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
package de.tracetronic.jenkins.plugins.ecutest.test;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Descriptor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;

/**
 * Builder providing the import of one or multiple ECU-TEST packages.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportPackageBuilder extends AbstractImportBuilder {

    /**
     * Instantiates a new {@link ImportPackageBuilder}.
     *
     * @param importConfigs
     *            the list of configured package importers
     */
    @DataBoundConstructor
    public ImportPackageBuilder(@CheckForNull final List<TMSConfig> importConfigs) {
        super(importConfigs);
    }

    /**
     * DescriptorImpl for {@link ImportPackageBuilder}.
     */
    @Symbol("importPackages")
    @Extension(ordinal = 1003)
    public static final class DescriptorImpl extends AbstractImportBuilder.DescriptorImpl {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(ImportPackageBuilder.class);
            load();
        }

        /**
         * Gets the applicable test importers.
         *
         * @return the applicable test importers
         */
        public List<Descriptor<? extends TMSConfig>> getApplicableImporters() {
            final List<Descriptor<? extends TMSConfig>> list = new ArrayList<>();
            final DescriptorExtensionList<TMSConfig, Descriptor<TMSConfig>> configs = ImportConfig.all();
            if (configs != null) {
                for (final Descriptor<TMSConfig> config : configs) {
                    if (config.isSubTypeOf(ImportPackageConfig.class) ||
                            config.isSubTypeOf(ImportPackageAttributeConfig.class)) {
                        list.add(config);
                    }
                }
            }
            return list;
        }

        @Override
        public String getDisplayName() {
            return Messages.ImportPackageBuilder_DisplayName();
        }
    }
}
