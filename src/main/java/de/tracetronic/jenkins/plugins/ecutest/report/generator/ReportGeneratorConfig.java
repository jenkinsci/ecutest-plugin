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
     * Instantiates a new {@link ReportGeneratorConfig}.
     *
     * @param name     the name
     * @param settings the settings
     */
    @DataBoundConstructor
    public ReportGeneratorConfig(final String name, final List<ReportGeneratorSetting> settings) {
        this.name = StringUtils.trimToEmpty(name);
        this.settings = settings == null ? new ArrayList<>() : removeEmptySettings(settings);
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

    @Override
    public ReportGeneratorConfig expand(final EnvVars envVars) {
        final String expName = envVars.expand(getName());
        final List<ReportGeneratorSetting> settings = new ArrayList<>();
        for (final ReportGeneratorSetting setting : getSettings()) {
            settings.add(setting.expand(envVars));
        }
        return new ReportGeneratorConfig(expName, settings);
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
