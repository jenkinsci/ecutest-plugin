/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMConstants API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComConstants {

    /**
     * Queries the number of constants.
     *
     * @return the number of constants
     * @throws ETComException in case of a COM exception
     */
    int getCount() throws ETComException;

    /**
     * Returns a specified constant by index.
     * The index should be larger than 0 and lesser than the number of constants.
     * The count of constants can be determined with {@link #getCount()}.
     *
     * @param id the id of the constant
     * @return the specified constant
     * @throws ETComException in case of a COM exception
     */
    ComConstant item(int id) throws ETComException;

    /**
     * Returns a specified constant by name.
     *
     * @param name the name of the constant
     * @return the specified constant
     * @throws ETComException in case of a COM exception
     */
    ComConstant item(String name) throws ETComException;

}
