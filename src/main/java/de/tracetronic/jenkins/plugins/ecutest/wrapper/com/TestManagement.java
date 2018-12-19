/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestManagement;

/**
 * COM object providing operations to offer access to the test management interface.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestManagement extends ETComDispatch implements ComTestManagement {

    /**
     * Instantiates a new {@link TestManagement}.
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public TestManagement(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public boolean login(final String user, final String password) throws ETComException {
        return performRequest("Login", new Variant(user), new Variant(password)).getBoolean();
    }

    @Override
    public boolean logout() throws ETComException {
        return performRequest("Logout").getBoolean();
    }

    /**
     * Same as {@link #importPackage(String, String, int)} but without timeout.
     *
     * @param tmProjectPath the path specifying the package in the test management system
     * @param importPath    the directory to save the package (relative to package directory or absolute)
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importPackage(final String tmProjectPath, final String importPath) throws ETComException {
        return importPackage(tmProjectPath, importPath, 0);
    }

    @Override
    public boolean importPackage(final String tmProjectPath, final String importPath, final int timeout)
        throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ImportPackage", new Variant(tmProjectPath), new Variant(importPath))
                .getBoolean();
        } else {
            return performDirectRequest("ImportPackage", new Variant(tmProjectPath), new Variant(importPath),
                new Variant(timeout)).getBoolean();
        }
    }

    /**
     * Same as {@link #importPackageDirectory(String, String, int)} but without timeout.
     *
     * @param tmDirectoryPath the path specifying the package in the test management system
     * @param importPath      the directory to save the package (relative to package directory or absolute)
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importPackageDirectory(final String tmDirectoryPath, final String importPath) throws ETComException {
        return importPackageDirectory(tmDirectoryPath, importPath, 0);
    }

    @Override
    public boolean importPackageDirectory(final String tmDirectoryPath, final String importPath, final int timeout)
        throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ImportPackageDirectory", new Variant(tmDirectoryPath),
                new Variant(importPath)).getBoolean();
        } else {
            return performDirectRequest("ImportPackageDirectory", new Variant(tmDirectoryPath),
                new Variant(importPath),
                new Variant(timeout)).getBoolean();
        }
    }

    /**
     * Same as {@link #importProject(String, String, boolean, int)} but without timeout.
     *
     * @param tmProjectPath the path specifying the project in the test management system
     * @param importPath    the directory to save the project (relative to package directory or absolute)
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importProject(final String tmProjectPath, final String importPath) throws ETComException {
        return importProject(tmProjectPath, importPath, false, 0);
    }

    /**
     * Same as {@link #importProject(String, String, boolean, int)} but with time out and default optional parameter.
     *
     * @param tmProjectPath the path specifying the project in the test management system
     * @param importPath    the directory to save the project (relative to package directory or absolute)
     * @param timeout       the timeout in seconds to wait for import to be finished
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importProject(final String tmProjectPath, final String importPath, final int timeout)
        throws ETComException {
        return importProject(tmProjectPath, importPath, false, timeout);
    }

    @Override
    public boolean importProject(final String tmProjectPath, final String importPath,
                                 final boolean importMissingPackages, final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ImportProject", new Variant(tmProjectPath), new Variant(importPath),
                new Variant(importMissingPackages)).getBoolean();
        } else {
            return performDirectRequest("ImportProject", new Variant(tmProjectPath), new Variant(importPath),
                new Variant(importMissingPackages), new Variant(timeout)).getBoolean();
        }
    }

    /**
     * Same as {@link #importProjectDirectory(String, String, int)} but without timeout.
     *
     * @param tmDirectoryPath the path specifying the project in the test management system
     * @param importPath      the directory to save the project (relative to package directory or absolute)
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importProjectDirectory(final String tmDirectoryPath, final String importPath) throws ETComException {
        return importProjectDirectory(tmDirectoryPath, importPath, 0);
    }

    @Override
    public boolean importProjectDirectory(final String tmDirectoryPath, final String importPath, final int timeout)
        throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ImportProjectDirectory", new Variant(tmDirectoryPath),
                new Variant(importPath)).getBoolean();
        } else {
            return performDirectRequest("ImportProjectDirectory", new Variant(tmDirectoryPath),
                new Variant(importPath),
                new Variant(timeout)).getBoolean();
        }
    }

    /**
     * Same as {@link #importPackageAttributes(String, int)} but without timeout.
     *
     * @param filePath the file path of the package whose attributes have to be imported
     *                 (relative to package directory or absolute)
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importPackageAttributes(final String filePath) throws ETComException {
        return importPackageAttributes(filePath, 0);
    }

    @Override
    public boolean importPackageAttributes(final String filePath, final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ImportPackageAttributes", new Variant(filePath)).getBoolean();
        } else {
            return performDirectRequest("ImportPackageAttributes", new Variant(filePath), new Variant(timeout))
                .getBoolean();
        }
    }

    /**
     * Same as {@link #importProjectAttributes(String, int)} but without timeout.
     *
     * @param filePath the file path of the project whose attributes have to be imported
     *                 (relative to package directory or absolute)
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean importProjectAttributes(final String filePath) throws ETComException {
        return importProjectAttributes(filePath, 0);
    }

    @Override
    public boolean importProjectAttributes(final String filePath, final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ImportProjectAttributes", new Variant(filePath)).getBoolean();
        } else {
            return performDirectRequest("ImportProjectAttributes", new Variant(filePath), new Variant(timeout))
                .getBoolean();
        }
    }

    /**
     * Same as {@link #exportPackage(String, String, boolean, int)} but without timeout.
     *
     * @param filePath   the file path of the package to be exported (relative to package directory or absolute)
     * @param exportPath the path specifying where the package should be placed at
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportPackage(final String filePath, final String exportPath) throws ETComException {
        return exportPackage(filePath, exportPath, false, 0);
    }

    /**
     * Same as {@link #exportPackage(String, String, boolean, int)} but without timeout and default optional parameter.
     *
     * @param filePath   the file path of the package to be exported (relative to package directory or absolute)
     * @param exportPath the path specifying where the package should be placed at
     * @param timeout    the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportPackage(final String filePath, final String exportPath, final int timeout)
        throws ETComException {
        return exportPackage(filePath, exportPath, false, timeout);
    }

    @Override
    public boolean exportPackage(final String filePath, final String exportPath, final boolean createNewPath,
                                 final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ExportPackage", new Variant(filePath), new Variant(exportPath),
                new Variant(createNewPath)).getBoolean();
        } else {
            return performDirectRequest("ExportPackage", new Variant(filePath), new Variant(exportPath),
                new Variant(createNewPath), new Variant(timeout)).getBoolean();
        }
    }

    /**
     * Same as {@link #exportProject(String, String, boolean, int)} but without timeout.
     *
     * @param filePath   the file path of the project to be exported (relative to package directory or absolute)
     * @param exportPath the path specifying where the project should be placed at
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportProject(final String filePath, final String exportPath) throws ETComException {
        return exportProject(filePath, exportPath, false, 0);
    }

    /**
     * Same as {@link #exportProject(String, String, boolean, int)} but without timeout and default optional parameter.
     *
     * @param filePath   the file path of the project to be exported (relative to package directory or absolute)
     * @param exportPath the path specifying where the project should be placed at
     * @param timeout    the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportProject(final String filePath, final String exportPath, final int timeout)
        throws ETComException {
        return exportProject(filePath, exportPath, false, timeout);
    }

    @Override
    public boolean exportProject(final String filePath, final String exportPath, final boolean createNewPath,
                                 final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ExportProject", new Variant(filePath), new Variant(exportPath),
                new Variant(createNewPath)).getBoolean();
        } else {
            return performDirectRequest("ExportProject", new Variant(filePath), new Variant(exportPath),
                new Variant(createNewPath), new Variant(timeout)).getBoolean();
        }
    }

    /**
     * Same as {@link #exportPackageAttributes(String, int)} but without timeout.
     *
     * @param filePath the file path of the package whose attributes have to be exported
     *                 (relative to package directory or absolute)
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportPackageAttributes(final String filePath) throws ETComException {
        return exportPackageAttributes(filePath, 0);
    }

    @Override
    public boolean exportPackageAttributes(final String filePath, final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ExportPackageAttributes", new Variant(filePath)).getBoolean();
        } else {
            return performDirectRequest("ExportPackageAttributes", new Variant(filePath), new Variant(timeout))
                .getBoolean();
        }
    }

    /**
     * Same as {@link #exportProjectAttributes(String, int)} but without timeout.
     *
     * @param filePath the file path of the project whose attributes have to be exported
     *                 (relative to package directory or absolute)
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportProjectAttributes(final String filePath) throws ETComException {
        return exportProjectAttributes(filePath, 0);
    }

    @Override
    public boolean exportProjectAttributes(final String filePath, final int timeout) throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ExportProjectAttributes", new Variant(filePath)).getBoolean();
        } else {
            return performDirectRequest("ExportProjectAttributes", new Variant(filePath), new Variant(timeout))
                .getBoolean();
        }
    }

    /**
     * Same as {@link #exportReport(String, String, int)} but without archive path.
     *
     * @param filePath the file path of the report file to be exported
     * @param timeout  the timeout in seconds to wait for export to be finished
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportReport(final String filePath, final int timeout) throws ETComException {
        return exportReport(filePath, null, timeout);
    }

    /**
     * Same as {@link #exportReport(String, String, int)} but without timeout.
     *
     * @param filePath    the file path of the report file to be exported
     * @param archivePath if the exportPath does not exist, it will be created
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws ETComException in case of a COM exception
     */
    public boolean exportReport(final String filePath, final String archivePath) throws ETComException {
        return exportReport(filePath, archivePath, 0);
    }

    @Override
    public boolean exportReport(final String filePath, final String archivePath, final int timeout)
        throws ETComException {
        if (timeout == 0) {
            return performDirectRequest("ExportReport", new Variant(filePath), new Variant(archivePath)).getBoolean();
        } else {
            return performDirectRequest("ExportReport", new Variant(filePath), new Variant(archivePath),
                new Variant(timeout)).getBoolean();
        }
    }
}
