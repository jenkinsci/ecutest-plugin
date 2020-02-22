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
 * Represents the ECU-TEST specific COMPackage API.
 */
public interface ComPackage {

    /**
     * Queries the package name.
     *
     * @return the name of this package
     * @throws ETComException in case of a COM exception
     */
    String getName() throws ETComException;

    /**
     * Queries the package description.
     *
     * @return the description of this package
     * @throws ETComException in case of a COM exception
     */
    String getDescription() throws ETComException;

    /**
     * Returns a list of the errors of the package (including all sub packages).
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
