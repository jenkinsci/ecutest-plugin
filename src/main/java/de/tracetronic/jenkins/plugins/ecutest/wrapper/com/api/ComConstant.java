/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMConstant API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComConstant {

    /**
     * Queries the constant name.
     *
     * @return the name of this constant
     * @throws ETComException in case of a COM exception
     */
    String getName() throws ETComException;

    /**
     * Queries the constant description.
     *
     * @return the description of this constant
     * @throws ETComException in case of a COM exception
     */
    String getDescription() throws ETComException;

    /**
     * Queries the constant value.
     *
     * @return the value of this constant
     * @throws ETComException in case of a COM exception
     */
    String getValue() throws ETComException;

}
