/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

import java.util.List;

/**
 * Represents the ECU-TEST specific COMProject API.
 */
public interface ComProject {

    /**
     * Queries the project name.
     *
     * @return the name of this project
     * @throws ETComException in case of a COM exception
     */
    String getName() throws ETComException;

    /**
     * Queries the list of packages used in this project.
     *
     * @return the list of referenced packages
     * @throws ETComException in case of a COM exception
     */
    String getPackages() throws ETComException;

    /**
     * Returns a list of the errors of the project.
     * Every list element is a tuple containing:
     * <ul>
     * <li>file path</li>
     * <li>seriousness (Error, Warning, Note)</li>
     * <li>error message</li>
     * <li>line number</li>
     * </ul>
     *
     * @return the error list
     * @throws ETComException in case of a COM exception
     */
    List<CheckInfoHolder> check() throws ETComException;

}
