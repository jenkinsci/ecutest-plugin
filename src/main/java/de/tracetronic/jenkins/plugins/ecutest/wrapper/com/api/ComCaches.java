/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ecu.test specific COMCaches API.
 */
public interface ComCaches {

    /**
     * Provides access to A2L cache files.
     *
     * @return the A2l cache object
     * @throws ETComException in case of a COM exception
     */
    ComCache getA2lCache() throws ETComException;

    /**
     * Provides access to ELF cache files.
     *
     * @return the ELF cache object
     * @throws ETComException in case of a COM exception
     */
    ComCache getElfCache() throws ETComException;

    /**
     * Provides access to bus cache files.
     *
     * @return the bus cache object
     * @throws ETComException in case of a COM exception
     */
    ComCache getBusCache() throws ETComException;

    /**
     * Provides access to model cache files.
     *
     * @return the model cache object
     * @throws ETComException in case of a COM exception
     */
    ComCache getModelCache() throws ETComException;

    /**
     * Provides access to service cache files.
     *
     * @return the service cache object
     * @throws ETComException in case of a COM exception
     */
    ComCache getServiceCache() throws ETComException;

}
