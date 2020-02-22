/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Serializable singleton class holding the current COM properties.
 */
public final class ETComProperty implements Serializable {

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
    private static final long serialVersionUID = 1L;
    private String progId = DEFAULT_PROG_ID;
    private int timeout = DEFAULT_TIMEOUT;

    /**
     * Private constructor.
     */
    private ETComProperty() {
    }

    /**
     * Returns the current instance of {@link ETComProperty}.
     *
     * @return the singleton instance
     */
    public static ETComProperty getInstance() {
        return SingletonHolder.INSTANCE;
    }

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
     * @param progId the progId to set
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
     * @param timeout the timeout to set
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
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
}
