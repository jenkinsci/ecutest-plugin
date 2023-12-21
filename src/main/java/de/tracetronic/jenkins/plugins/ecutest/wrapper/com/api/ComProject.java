/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTestClient.CheckInfoHolder;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

import java.util.List;

/**
 * Represents the ecu.test specific COMProject API.
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
     *
     * <p>Every list element is a tuple containing:</p>
     *
     * <ul>
     *   <li>file path</li>
     *   <li>seriousness (Error, Warning, Note)</li>
     *   <li>error message</li>
     *   <li>line number</li>
     * </ul>
     *
     * @return the error list
     * @throws ETComException in case of a COM exception
     */
    List<CheckInfoHolder> check() throws ETComException;

    /**
     * Returns converted error descriptions into specific Warnings NG plugin JSON format.
     *
     * <ul>
     *   <li>issues
     *     <ul>
     *       <li>fileName (Package name from file path)</li>
     *       <li>severity (Error -&gt; ERROR, Warning -&gt; HIGH, Note -&gt; NORMAL)</li>
     *       <li>description (error message)</li>
     *       <li>startLine (line number)</li>
     *     </ul>
     *   </li>
     *   <li>size (count of issues)</li>
     * </ul>
     *
     * @return the error descriptions as issues in JSON format
     * @throws ETComException in case of a COM exception
     */
    String checkNG() throws ETComException;

}
