/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExpandableConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches.CacheType;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class holding the configuration for generating an ECU-TEST cache type.
 */
public class CacheConfig extends AbstractDescribableImpl<CacheConfig> implements ExpandableConfig, Serializable {

    private static final long serialVersionUID = 1L;

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

    @Override
    public CacheConfig expand(final EnvVars envVars) {
        final String expFilePath = envVars.expand(getFilePath());
        final String expDbChannel = envVars.expand(getDbChannel());
        return new CacheConfig(getType(), expFilePath, expDbChannel, isClear());
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof CacheConfig) {
            final CacheConfig that = (CacheConfig) other;
            result = Objects.equals(type, that.type)
                && Objects.equals(filePath, that.filePath)
                && Objects.equals(dbChannel, that.dbChannel)
                && clear == that.clear;
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(type).append(filePath)
            .append(dbChannel).append(clear).toHashCode();
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
