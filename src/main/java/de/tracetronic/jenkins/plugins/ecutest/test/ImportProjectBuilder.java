/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
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
 * Builder providing the import of one or multiple ECU-TEST projects.
 */
public class ImportProjectBuilder extends AbstractImportBuilder {

    /**
     * Instantiates a new {@link ImportProjectBuilder}.
     *
     * @param importConfigs the list of configured project importers
     */
    @DataBoundConstructor
    public ImportProjectBuilder(@CheckForNull final List<TMSConfig> importConfigs) {
        super(importConfigs);
    }

    /**
     * DescriptorImpl for {@link ImportProjectBuilder}.
     */
    @Symbol("importProjects")
    @Extension(ordinal = 10004)
    public static final class DescriptorImpl extends AbstractImportBuilder.DescriptorImpl {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(ImportProjectBuilder.class);
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
            for (final Descriptor<TMSConfig> config : configs) {
                if (config.isSubTypeOf(ImportProjectConfig.class)
                        || config.isSubTypeOf(ImportProjectAttributeConfig.class)
                        || config.isSubTypeOf(ImportProjectArchiveConfig.class)) {
                    list.add(config);
                }
            }
            return list;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ImportProjectBuilder_DisplayName();
        }
    }
}
