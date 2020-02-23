/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Descriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder providing the import of one or multiple ECU-TEST packages.
 */
public class ImportPackageBuilder extends AbstractImportBuilder {

    /**
     * Instantiates a new {@link ImportPackageBuilder}.
     *
     * @param importConfigs the list of configured package importers
     */
    @DataBoundConstructor
    public ImportPackageBuilder(@CheckForNull final List<TMSConfig> importConfigs) {
        super(importConfigs);
    }

    /**
     * DescriptorImpl for {@link ImportPackageBuilder}.
     */
    @Symbol("importPackages")
    @Extension(ordinal = 10006)
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
                    if (config.isSubTypeOf(ImportPackageConfig.class)
                            || config.isSubTypeOf(ImportPackageAttributeConfig.class)) {
                        list.add(config);
                    }
                }
            }
            return list;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportPackageBuilder_DisplayName();
        }
    }
}
