/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * Common base class for {@link ExportAttributeConfig} and {@link ImportAttributeConfig}.
 */
public abstract class AttributeConfig extends TMSConfig {

    private static final long serialVersionUID = 1L;

    private final String filePath;

    /**
     * Instantiates a new {@link AttributeConfig}.
     *
     * @param filePath      the test file path whose attributes to import or export
     * @param credentialsId the credentials id
     * @param timeout       the export timeout
     */
    public AttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(credentialsId, timeout);
        this.filePath = StringUtils.trimToEmpty(filePath);
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public final boolean equals(final Object other) {
        boolean result = false;
        if (other instanceof AttributeConfig) {
            final AttributeConfig that = (AttributeConfig) other;
            final String filePath = getFilePath();
            final String thatFilePath = that.getFilePath();
            result = Objects.equals(filePath, thatFilePath)
                && (getCredentialsId() == null ? that.getCredentialsId() == null
                    : getCredentialsId().equals(that.getCredentialsId()))
                && (getTimeout() == null ? that.getTimeout() == null
                    : getTimeout().equals(that.getTimeout()));
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(getFilePath()).append(getCredentialsId()).append(getTimeout())
            .toHashCode();
    }
}
