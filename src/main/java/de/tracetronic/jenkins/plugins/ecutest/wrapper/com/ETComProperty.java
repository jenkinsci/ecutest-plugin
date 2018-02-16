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
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Serializable singleton class holding the current COM properties.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public final class ETComProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default COM specific programmatic identifier.
     */
    public static final String DEFAULT_PROG_ID = "ECU-TEST.Application";

    /**
     * Default COM response timeout in seconds.
     */
    public static final int DEFAULT_TIMEOUT = 0;

    /**
     * Default COM connection timeout in seconds.
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 120;

    private String progId = DEFAULT_PROG_ID;
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * Gets the current COM specific programmatic identifier set by latest ECU-TEST startup.
     *
     * @return the programmatic identifier
     */
    public String getProgId() {
        return progId;
    }

    /**
     * Sets the COM specific programmatic identifier.
     *
     * @param progId
     *            the progId to set
     */
    public void setProgId(final String progId) {
        this.progId = StringUtils.defaultIfBlank(progId, DEFAULT_PROG_ID);
    }

    /**
     * Gets the current COM response timeout set by latest ECU-TEST startup.
     * 
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the COM response timeout.
     * 
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * Private constructor.
     */
    private ETComProperty() {
    }

    /**
     * Ensures not to impact the state of object in which it was serialized.
     *
     * @return the singleton instance
     */
    private Object readResolve() {
        return getInstance();
    }

    /**
     * Initializes and holds the singleton for this class.
     */
    private static final class SingletonHolder {

        private static final ETComProperty INSTANCE = new ETComProperty();

        /**
         * Private constructor.
         */
        private SingletonHolder() {
            throw new UnsupportedOperationException("Singleton class");
        }
    }

    /**
     * Returns the current instance of {@link ETComProperty}.
     *
     * @return the singleton instance
     */
    public static ETComProperty getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
