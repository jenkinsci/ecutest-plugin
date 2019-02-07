/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMTestConfiguration API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComTestConfiguration {

    /**
     * Assigns a value to a global constant. If the global constant does not exist it is created. This method requires a
     * test configuration file to be loaded, otherwise an exception is thrown. The changed test configuration is saved
     * instantly.
     *
     * @param name  the name of the constant to be modified
     * @param value the value to be assigned
     * @throws ETComException in case of a COM exception
     */
    void setGlobalConstant(String name, String value) throws ETComException;

    /**
     * Queries all global constants of the currently loaded test configuration.
     *
     * @return the global constants
     * @throws ETComException in case of a COM exception
     */
    ComConstants getGlobalConstants() throws ETComException;

    /**
     * Returns the full path of this test configuration.
     *
     * @return the TCF file path
     * @throws ETComException in case of a COM exception
     */
    String getFileName() throws ETComException;

}
