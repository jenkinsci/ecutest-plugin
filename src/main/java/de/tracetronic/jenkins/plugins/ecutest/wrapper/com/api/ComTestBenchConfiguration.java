/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMTestbenchConfiguration API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComTestBenchConfiguration {

    /**
     * Returns the full path of this test bench configuration.
     *
     * @return the TBC file path
     * @throws ETComException in case of a COM exception
     */
    String getFileName() throws ETComException;

}
