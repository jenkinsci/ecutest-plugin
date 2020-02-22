/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
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
 * Builder providing the export of one or multiple ECU-TEST projects.
 */
public class ExportProjectBuilder extends AbstractExportBuilder {

    /**
     * Instantiates a new {@link ExportProjectBuilder}.
     *
     * @param exportConfigs the list of configured project exporters
     */
    @DataBoundConstructor
    public ExportProjectBuilder(@CheckForNull final List<TMSConfig> exportConfigs) {
        super(exportConfigs);
    }

    /**
     * DescriptorImpl for {@link ExportProjectBuilder}.
     */
    @Symbol("exportProjects")
    @Extension(ordinal = 10003)
    public static final class DescriptorImpl extends AbstractExportBuilder.DescriptorImpl {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(ExportProjectBuilder.class);
            load();
        }

        /**
         * Gets the applicable test exporters.
         *
         * @return the applicable test exporters
         */
        public List<Descriptor<? extends TMSConfig>> getApplicableExporters() {
            final List<Descriptor<? extends TMSConfig>> list = new ArrayList<>();
            final DescriptorExtensionList<TMSConfig, Descriptor<TMSConfig>> configs = ExportConfig.all();
            if (configs != null) {
                for (final Descriptor<TMSConfig> config : configs) {
                    if (config.isSubTypeOf(ExportProjectConfig.class) ||
                        config.isSubTypeOf(ExportProjectAttributeConfig.class)) {
                        list.add(config);
                    }
                }
            }
            return list;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ExportProjectBuilder_DisplayName();
        }
    }
}
