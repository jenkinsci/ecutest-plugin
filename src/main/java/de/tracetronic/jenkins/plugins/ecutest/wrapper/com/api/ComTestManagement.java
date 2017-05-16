/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMTestManagement API.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public interface ComTestManagement {

    /**
     * Performs a login to the preconfigured test management service.
     *
     * @param user
     *            the user name
     * @param password
     *            the password
     * @return {@code true} if login succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean login(String user, String password) throws ETComException;

    /**
     * Performs a logout to the preconfigured test management service.
     *
     * @return {@code true} if logout succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean logout() throws ETComException;

    /**
     * Imports a package from a test management system.
     * The tmProjectPath specifies the package in the test management system.
     * The package will be imported into directory given by importPath.
     *
     * @param tmPackagePath
     *            the path specifying the package in the test management system
     * @param importPath
     *            the directory to save the package (relative to package directory or absolute)
     * @param timeout
     *            the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean importPackage(String tmPackagePath, String importPath, int timeout) throws ETComException;

    /**
     * Imports a directory from a test management system with all sub directories and package.
     * The tmDirectoryPath specifies the directory in the test management system.
     * The directory will be imported into the directory given by importPath.
     *
     * @param tmDirectoryPath
     *            the path specifying the package in the test management system
     * @param importPath
     *            the directory to save the package (relative to package directory or absolute)
     * @param timeout
     *            the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean importPackageDirectory(String tmDirectoryPath, String importPath, int timeout) throws ETComException;

    /**
     * Imports a project from a test management system.
     * The tmProjectPath specifies the project in the test management system.
     * The project will be imported into directory given by importPath.
     *
     * @param tmProjectPath
     *            the path specifying the project in the test management system
     * @param importPath
     *            the directory to save the project (relative to package directory or absolute)
     * @param importMissingPackages
     *            specifies whether missing packages will be automatically imported
     * @param timeout
     *            the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean importProject(String tmProjectPath, String importPath, boolean importMissingPackages, int timeout)
            throws ETComException;

    /**
     * Imports a directory from a test management system with all sub directories and projects.
     * The tmDirectoryPath specifies the directory in the test management system.
     * The directory will be imported into the directory given by importPath.
     *
     * @param tmDirectoryPath
     *            the path specifying the project in the test management system
     * @param importPath
     *            the directory to save the project (relative to package directory or absolute)
     * @param timeout
     *            the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean importProjectDirectory(String tmDirectoryPath, String importPath, int timeout) throws ETComException;

    /**
     * Exports the given package to test management system.
     * The exportPath is needed to specify where the package should be placed at.
     *
     * @param filePath
     *            the file path of the package to be exported (relative to package directory or absolute)
     * @param exportPath
     *            the path specifying where the package should be placed at
     * @param createNewPath
     *            if the exportPath does not exist, it will be created
     * @param timeout
     *            the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean exportPackage(String filePath, String exportPath, boolean createNewPath, int timeout)
            throws ETComException;

    /**
     * Exports the given project to test management system.
     * The exportPath is needed to specify where the project should be placed at.
     *
     * @param filePath
     *            the file path of the project to be exported (relative to package directory or absolute)
     * @param exportPath
     *            the path specifying where the project should be placed at
     * @param createNewPath
     *            if the exportPath does not exist, it will be created
     * @param timeout
     *            the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean exportProject(String filePath, String exportPath, boolean createNewPath, int timeout)
            throws ETComException;

    /**
     * Exports the attributes of given package to test management system.
     *
     * @param filePath
     *            the file path of the package whose attributes have to be exported
     *            (relative to package directory or absolute)
     * @param timeout
     *            the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean exportPackageAttributes(String filePath, int timeout) throws ETComException;

    /**
     * Exports the attributes of given project to test management system.
     *
     * @param filePath
     *            the file path of the project whose attributes have to be exported
     *            (relative to package directory or absolute)
     * @param timeout
     *            the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean exportProjectAttributes(String filePath, int timeout) throws ETComException;

    /**
     * Exports the given report file to test management system.
     * The archive path may be used to copy the report to another directory and to reference
     * it from the test management entry.
     *
     * @param filePath
     *            the file path of the report file to be exported
     * @param archivePath
     *            if the exportPath does not exist, it will be created
     * @param timeout
     *            the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException
     *             in case of a COM exception
     */
    boolean exportReport(String filePath, String archivePath, int timeout) throws ETComException;
}
