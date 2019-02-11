/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches.CacheType;
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

/**
 * Class holding the configuration for generating an ECU-TEST cache type.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class CacheConfig extends AbstractDescribableImpl<CacheConfig> implements Serializable {

    private final CacheType type;
    private final String filePath;
    private final String dbChannel;
    private final boolean clear;

    /**
     * Instantiates a new {@link CacheConfig}.
     *
     * @param type      the cache type
     * @param filePath  the database file path
     * @param dbChannel the database channel
     * @param clear     specifies whether to clear all caches
     */
    @DataBoundConstructor
    public CacheConfig(final CacheType type, final String filePath, final String dbChannel, final boolean clear) {
        super();
        this.type = type;
        this.filePath = StringUtils.trimToEmpty(filePath);
        this.dbChannel = StringUtils.trimToEmpty(dbChannel);
        this.clear = clear;
    }

    public CacheType getType() {
        return type;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDbChannel() {
        return dbChannel;
    }

    public boolean isClear() {
        return clear;
    }

    /**
     * DescriptorImpl for {@link CacheConfig}.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<CacheConfig> {

        private final ToolValidator toolValidator = new ToolValidator();

        /**
         * Fills the cache type drop-down menu.
         *
         * @return the cache type items
         */
        public ListBoxModel doFillTypeItems() {
            final ListBoxModel model = new ListBoxModel();
            for (final CacheType type : CacheType.values()) {
                model.add(type.name(), type.name());
            }
            return model;
        }

        /**
         * Validates the cache file path.
         *
         * @param value the file path
         * @return the form validation
         */
        public FormValidation doCheckFilePath(@QueryParameter final String value) {
            return toolValidator.validateRequiredParamValue(value);
        }

        /**
         * Validates the database channel.
         *
         * @param value the file path
         * @return the form validation
         */
        public FormValidation doCheckDbChannel(@QueryParameter final String value) {
            return toolValidator.validateParameterizedValue(value);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Cache Configuration";
        }
    }
}
