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
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.JacobException;
import com.jacob.com.Variant;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComApplication;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComPackage;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComProject;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestConfiguration;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestManagement;

/**
 * COM client to initialize a COM connection and to perform requests on application specific COM API.
 * <p>
 * All threads from COM will be automatically released after closing the client or at the latest when finalizing
 * occurred.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETComClient implements ComApplication, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ETComClient.class.getName());

    /**
     * Default connection timeout in seconds.
     */
    private static final int DEFAULT_TIMOUT = 120;

    /**
     * COMApplication dispatch.
     */
    protected ETComDispatch dispatch;

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} and waits for connection within
     * the default timeout.
     *
     * @throws ETComException
     *             in case of a COM exception or if the timeout is reached
     */
    public ETComClient() throws ETComException {
        initDispatch(ETComProgId.DEFAULT_PROG_ID);
        waitForConnection(DEFAULT_TIMOUT);
    }

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} with given programmatic
     * identifier and waits for connection within the default timeout.
     *
     * @param progId
     *            the programmatic identifier
     * @throws ETComException
     *             in case of a COM exception or if the timeout is reached
     */
    public ETComClient(final String progId) throws ETComException {
        initDispatch(progId);
        waitForConnection(DEFAULT_TIMOUT);
    }

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} and waits for connection within
     * the given timeout.
     *
     * @param timeout
     *            the timeout waiting for a connection
     * @throws ETComException
     *             in case of a COM exception or if the timeout is reached
     */
    public ETComClient(final int timeout) throws ETComException {
        initDispatch(ETComProgId.DEFAULT_PROG_ID);
        waitForConnection(timeout);
    }

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} with given programmatic
     * identifier and waits for connection within the given timeout.
     *
     * @param progId
     *            the programmatic identifier
     * @param timeout
     *            the timeout waiting for a connection
     * @throws ETComException
     *             in case of a COM exception or if the timeout is reached
     */
    public ETComClient(final String progId, final int timeout) throws ETComException {
        initDispatch(progId);
        waitForConnection(timeout);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            releaseDispatch();
        } finally {
            ComThread.Release();
            super.finalize();
        }
    }

    /**
     * Initializes the a single-threaded {@link COMThread} and sets the {@link ETComDispatch} instance using the default
     * program id returned from the {@link ActiveXComponent}.
     *
     * @param progId
     *            the programmatic identifier
     * @throws ETComException
     *             in case of a COM exception
     */
    private void initDispatch(final String progId) throws ETComException {
        try {
            ComThread.InitSTA();
            final ActiveXComponent component = new ActiveXComponent(StringUtils.isEmpty(progId) ?
                    ETComProgId.DEFAULT_PROG_ID : progId);
            dispatch = new ETComDispatch(component.getObject());
        } catch (final JacobException e) {
            throw new ETComException(e.getMessage(), e);
        }
    }

    /**
     * Releases the {@link Dispatch}.
     *
     * @throws ETComException
     *             in case of a COM exception
     */
    private void releaseDispatch() throws ETComException {
        if (dispatch != null) {
            try {
                dispatch.safeRelease();
            } catch (final JacobException e) {
                throw new ETComException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() {
        try {
            releaseDispatch();
        } catch (final ETComException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            ComThread.Release();
        }
    }

    /**
     * Waits for a valid COM connection within the given timeout.
     *
     * @param timeout
     *            the timeout waiting for a connection
     * @throws ETComException
     *             in case of a COM exception or if the timeout is reached
     */
    private void waitForConnection(final int timeout) throws ETComException {
        final long endTimeMillis = System.currentTimeMillis() + Long.valueOf(timeout) * 1000L;
        while (timeout <= 0 || System.currentTimeMillis() < endTimeMillis) {
            try {
                if (isApplicationRunning()) {
                    return;
                } else {
                    Thread.sleep(1000L);
                }
            } catch (final ETComException e) {
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException e1) {
                    throw new ETComException(e.getMessage(), e);
                }
            } catch (final InterruptedException e) {
                throw new ETComException(e.getMessage(), e);
            }
        }
        throw new ETComException(String.format("Timeout of %d seconds reached: COM server not available!",
                timeout));
    }

    @Override
    public ComTestEnvironment start() throws ETComException {
        return new TestEnvironment(dispatch.performRequest("Start").toDispatch());
    }

    @Override
    public ComTestEnvironment stop() throws ETComException {
        return new TestEnvironment(dispatch.performRequest("Stop").toDispatch());
    }

    @Override
    public ComTestEnvironment getTestEnvironment() throws ETComException {
        return new TestEnvironment(dispatch.performRequest("GetTestEnvironment").toDispatch());
    }

    @Override
    public ComTestManagement getTestManagement() throws ETComException {
        return new TestManagement(dispatch.performRequest("GetTestManagementModule").toDispatch());
    }

    @Override
    public boolean isApplicationRunning() throws ETComException {
        return dispatch.performRequest("IsApplicationRunning").getBoolean();
    }

    @Override
    public String getVersion() throws ETComException {
        return dispatch.performRequest("GetVersion").getString();
    }

    @Override
    public String getSetting(final String settingName) throws ETComException {
        return dispatch.performRequest("GetSetting", new Variant(settingName)).getString();
    }

    @Override
    public boolean quit() throws ETComException {
        return dispatch.performRequest("Quit").getBoolean();
    }

    @Override
    public boolean exit() throws ETComException {
        return dispatch.performRequest("Exit").getBoolean();
    }

    @Override
    public ComPackage openPackage(final String path) throws ETComException {
        return new Package(dispatch.performRequest("OpenPackage", new Variant(path)).toDispatch());
    }

    @Override
    public boolean closePackage(final String path) throws ETComException {
        return dispatch.performRequest("ClosePackage", new Variant(path)).getBoolean();
    }

    /**
     * Same as {@link #openProject(String, boolean, String)} but with default parameters.
     *
     * @param path
     *            the full path name of the project to open
     * @return the {@link ComProject} dispatch, if the project is successfully opened, {@code null} otherwise
     * @throws ETComException
     *             in case of a COM exception
     * @see #openProject(String, boolean, String)
     */
    public ComProject openProject(final String path) throws ETComException {
        return openProject(path, false, "");
    }

    @Override
    public ComProject openProject(final String path, final boolean execInCurrentPkgDir,
            final String filterExpression) throws ETComException {
        return new Project(dispatch.performRequest("OpenProject", new Variant(path),
                new Variant(execInCurrentPkgDir), new Variant(filterExpression)).toDispatch());
    }

    @Override
    public boolean closeProject(final String path) throws ETComException {
        return dispatch.performRequest("CloseProject", new Variant(path)).getBoolean();
    }

    @Override
    public boolean importProject(final String path, final String importPath, final String importConfigPath,
            final boolean replaceFiles) throws ETComException {
        return dispatch.performRequest("ImportProject", new Variant(path), new Variant(replaceFiles),
                new Variant(false), new Variant(importPath), new Variant(importConfigPath)).getBoolean();
    }

    @Override
    public boolean openTestbenchConfiguration(final String path) throws ETComException {
        return dispatch.performRequest("OpenTestbenchConfiguration", new Variant(path)).getBoolean();
    }

    @Override
    public boolean openTestConfiguration(final String path) throws ETComException {
        return dispatch.performRequest("OpenTestConfiguration", new Variant(path)).getBoolean();
    }

    @Override
    public ComTestConfiguration getCurrentTestConfiguration() throws ETComException {
        return new TestConfiguration(dispatch.performRequest("GetCurrentTestConfiguration").toDispatch());
    }

    @Override
    public boolean waitForIdle(final int timeout) throws ETComException {
        if (timeout == 0) {
            return dispatch.performRequest("WaitForIdle").getBoolean();
        } else {
            return dispatch.performRequest("WaitForIdle", new Variant(timeout)).getBoolean();
        }
    }
}
