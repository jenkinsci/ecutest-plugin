/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

import java.util.List;

/**
 * Represents the ECU-TEST specific COMCache API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComCache {

    /**
     * Insert a database to the cache.
     *
     * @param filePath  the file path of the database to be added to the cache
     * @param dbChannel the optional database channel. Only needed by bus and service databases.
     * @throws ETComException in case of a COM exception
     */
    void insert(String filePath, String dbChannel) throws ETComException;

    /**
     * Remove all cache files of this category.
     *
     * @param force specifies wether to close already opend cache files
     * @throws ETComException in case of a COM exception
     */
    void clear(boolean force) throws ETComException;

    /**
     * Returns all cache files of this category.
     *
     * @return the list of available cache files
     * @throws ETComException in case of a COM exception
     */
    List<String> getFiles() throws ETComException;

}
