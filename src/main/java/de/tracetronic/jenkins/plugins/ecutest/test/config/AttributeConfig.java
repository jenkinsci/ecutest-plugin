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
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Common base class for {@link ExportAttributeConfig} and {@link ImportAttributeConfig}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AttributeConfig extends TMSConfig {

    private static final long serialVersionUID = 1L;

    private final String filePath;

    /**
     * Instantiates a new {@link AttributeConfig}.
     *
     * @param filePath
     *            the test file path whose attributes to import or export
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the export timeout
     */
    public AttributeConfig(final String filePath, final String credentialsId, final String timeout) {
        super(credentialsId, timeout);
        this.filePath = StringUtils.trimToEmpty(filePath);
    }

    /**
     * @return the test file path whose attributes to import or export
     */
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
            result = (filePath == null ? thatFilePath == null : filePath.equals(thatFilePath))
                    && (getCredentialsId() == null ? that.getCredentialsId() == null :
                        getCredentialsId().equals(that.getCredentialsId()))
                        && (getTimeout() == null ? that.getTimeout() == null :
                            getTimeout().equals(that.getTimeout()));
        }
        return result;
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder(17, 31).append(getFilePath()).append(getCredentialsId()).append(getTimeout())
                .toHashCode();
    }
}
