/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExpandableConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ReportGeneratorValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class holding the report generator configuration.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGeneratorConfig extends AbstractDescribableImpl<ReportGeneratorConfig> implements ExpandableConfig,
    Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<ReportGeneratorSetting> settings;
    /**
     * @since 2.9
     */
    private boolean usePersistedSettings;

    /**
     * Instantiates a new {@link ReportGeneratorConfig}.
     *
     * @param name                 the name
     * @param settings             the settings
     * @param usePersistedSettings the use persisted settings
     */
    @DataBoundConstructor
    public ReportGeneratorConfig(final String name, final List<ReportGeneratorSetting> settings,
                                 final boolean usePersistedSettings) {
        this.name = StringUtils.trimToEmpty(name);
        this.settings = settings == null ? new ArrayList<>() : removeEmptySettings(settings);
        this.usePersistedSettings = usePersistedSettings;
    }

    /**
     * Removes empty settings.
     *
     * @param settings the settings
     * @return the list of valid settings
     */
    private static List<ReportGeneratorSetting> removeEmptySettings(final List<ReportGeneratorSetting> settings) {
        final List<ReportGeneratorSetting> validSettings = new ArrayList<>();
        for (final ReportGeneratorSetting setting : settings) {
            if (StringUtils.isNotBlank(setting.getName())) {
                validSettings.add(setting);
            }
        }
        return validSettings;
    }

    /**
     * Gets the name of the related template directory.
     *
     * @return the template name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the settings
     */
    public List<ReportGeneratorSetting> getSettings() {
        return settings;
    }


    /**
     * @return specifies whether to use report generator settings from persisted configurations files (XML)
     */
    public boolean isUsePersistedSettings() {
        return usePersistedSettings;
    }

    @Override
    public ReportGeneratorConfig expand(final EnvVars envVars) {
        final String expName = envVars.expand(getName());
        final List<ReportGeneratorSetting> settings = new ArrayList<>();
        for (final ReportGeneratorSetting setting : getSettings()) {
            settings.add(setting.expand(envVars));
        }
        return new ReportGeneratorConfig(expName, settings, usePersistedSettings);
    }

    /**
     * DescriptorImpl for {@link ReportGeneratorConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ReportGeneratorConfig> {

        /**
         * Defines the standard report generators shipped with ECU-TEST installation.
         */
        private static final List<String> REPORT_GENERATORS = Arrays.asList(
            "ATX", "EXCEL", "HTML", "JSON", "OMR", "TestSpec", "TRF-SPLIT", "TXT", "UNIT");

        private final ReportGeneratorValidator reportValidator = new ReportGeneratorValidator();

        /**
         * Fills the generator drop-down menu.
         *
         * @return the generator items
         */
        public ListBoxModel doFillNameItems() {
            final ListBoxModel model = new ListBoxModel();
            for (final String generator : REPORT_GENERATORS) {
                model.add(generator, generator);
            }
            return model;
        }

        /**
         * Validates the generator name.
         *
         * @param value the value
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return reportValidator.validateGeneratorName(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Report Generator Configuration";
        }
    }
}
