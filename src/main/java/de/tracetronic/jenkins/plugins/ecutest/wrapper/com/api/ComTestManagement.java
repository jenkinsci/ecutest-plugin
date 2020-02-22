/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;

/**
 * Represents the ECU-TEST specific COMTestManagement API.
 */
public interface ComTestManagement {

    /**
     * Checks if the test management service can be started.
     *
     * @return {@code true} if the service could be started, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean isAvailable() throws ETComException;

    /**
     * Performs a login to the preconfigured test management service.
     *
     * @param user     the user name
     * @param password the password
     * @return {@code true} if login succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean login(String user, String password) throws ETComException;

    /**
     * Performs a logout to the preconfigured test management service.
     *
     * @return {@code true} if logout succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean logout() throws ETComException;

    /**
     * Imports a package from a test management system.
     * The tmProjectPath specifies the package in the test management system.
     * The package will be imported into directory given by importPath.
     *
     * @param tmPackagePath the path specifying the package in the test management system
     * @param importPath    the directory to save the package (relative to package directory or absolute)
     * @param timeout       the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importPackage(String tmPackagePath, String importPath, int timeout) throws ETComException;

    /**
     * Imports a directory from a test management system with all sub directories and package.
     * The tmDirectoryPath specifies the directory in the test management system.
     * The directory will be imported into the directory given by importPath.
     *
     * @param tmDirectoryPath the path specifying the package in the test management system
     * @param importPath      the directory to save the package (relative to package directory or absolute)
     * @param timeout         the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importPackageDirectory(String tmDirectoryPath, String importPath, int timeout) throws ETComException;

    /**
     * Imports a project from a test management system.
     * The tmProjectPath specifies the project in the test management system.
     * The project will be imported into directory given by importPath.
     *
     * @param tmProjectPath         the path specifying the project in the test management system
     * @param importPath            the directory to save the project (relative to package directory or absolute)
     * @param importMissingPackages specifies whether missing packages will be automatically imported
     * @param timeout               the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importProject(String tmProjectPath, String importPath, boolean importMissingPackages, int timeout)
        throws ETComException;

    /**
     * Imports a directory from a test management system with all sub directories and projects.
     * The tmDirectoryPath specifies the directory in the test management system.
     * The directory will be imported into the directory given by importPath.
     *
     * @param tmDirectoryPath the path specifying the project in the test management system
     * @param importPath      the directory to save the project (relative to package directory or absolute)
     * @param timeout         the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importProjectDirectory(String tmDirectoryPath, String importPath, int timeout) throws ETComException;

    /**
     * Imports the attributes of given package to test management system.
     *
     * @param filePath the file path of the package whose attributes have to be imported
     *                 (relative to package directory or absolute)
     * @param timeout  the timeout in seconds to wait for export to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importPackageAttributes(String filePath, int timeout) throws ETComException;

    /**
     * Imports the attributes of given project to test management system.
     *
     * @param filePath the file path of the project whose attributes have to be imported
     *                 (relative to package directory or absolute)
     * @param timeout  the timeout in seconds to wait for export to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean importProjectAttributes(String filePath, int timeout) throws ETComException;

    /**
     * Exports the given package to test management system.
     * The exportPath is needed to specify where the package should be placed at.
     *
     * @param filePath      the file path of the package to be exported (relative to package directory or absolute)
     * @param exportPath    the path specifying where the package should be placed at
     * @param createNewPath if the exportPath does not exist, it will be created
     * @param timeout       the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean exportPackage(String filePath, String exportPath, boolean createNewPath, int timeout)
        throws ETComException;

    /**
     * Exports the given project to test management system.
     * The exportPath is needed to specify where the project should be placed at.
     *
     * @param filePath      the file path of the project to be exported (relative to package directory or absolute)
     * @param exportPath    the path specifying where the project should be placed at
     * @param createNewPath if the exportPath does not exist, it will be created
     * @param timeout       the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean exportProject(String filePath, String exportPath, boolean createNewPath, int timeout)
        throws ETComException;

    /**
     * Exports the attributes of given package to test management system.
     *
     * @param filePath the file path of the package whose attributes have to be exported
     *                 (relative to package directory or absolute)
     * @param timeout  the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean exportPackageAttributes(String filePath, int timeout) throws ETComException;

    /**
     * Exports the attributes of given project to test management system.
     *
     * @param filePath the file path of the project whose attributes have to be exported
     *                 (relative to package directory or absolute)
     * @param timeout  the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean exportProjectAttributes(String filePath, int timeout) throws ETComException;

    /**
     * Exports the given report file to test management system.
     * The archive path may be used to copy the report to another directory and to reference
     * it from the test management entry.
     *
     * @param filePath    the file path of the report file to be exported
     * @param archivePath if the exportPath does not exist, it will be created
     * @param timeout     the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    boolean exportReport(String filePath, String archivePath, int timeout) throws ETComException;

}
