/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.JacobException;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComAnalysisEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComApplication;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComCaches;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComPackage;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComProject;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestBenchConfiguration;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestConfiguration;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestEnvironment;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestManagement;
import org.apache.commons.lang.StringUtils;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * COM client to initialize a COM connection and to perform requests on application specific COM API.
 * <p>
 * All threads from COM will be automatically released after closing the client or at the latest when finalizing
 * occurred.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETComClient implements ComApplication, AutoCloseable {

    /**
     * The COMApplication dispatch.
     */
    private ETComDispatch dispatch;

    /**
     * Holds the status when to release the dispatch.
     */
    private boolean releaseDispatch;

    /**
     * Specifies whether to apply the configured COM timeout.
     */
    private boolean useTimeout;

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} with the configured COM
     * settings.
     *
     * @throws ETComException in case of a COM exception or if the timeout is reached
     */
    public ETComClient() throws ETComException {
        final ETComProperty properties = ETComProperty.getInstance();
        initDispatch(properties.getProgId());
        waitForConnection(properties.getTimeout());
    }

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} with given programmatic
     * identifier and waits for connection within the default timeout.
     *
     * @param progId the programmatic identifier
     * @throws ETComException in case of a COM exception or if the timeout is reached
     */
    public ETComClient(final String progId) throws ETComException {
        initDispatch(progId);
        waitForConnection(ETComProperty.DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} and waits for connection within
     * the given timeout.
     *
     * @param timeout the timeout waiting for a connection
     * @throws ETComException in case of a COM exception or if the timeout is reached
     */
    public ETComClient(final int timeout) throws ETComException {
        initDispatch(ETComProperty.DEFAULT_PROG_ID);
        waitForConnection(timeout);
    }

    /**
     * Instantiates a new {@link ETComClient} by initializing the {@link ETComDispatch} with given programmatic
     * identifier and waits for connection within the given timeout.
     *
     * @param progId  the programmatic identifier
     * @param timeout the timeout waiting for a connection
     * @throws ETComException in case of a COM exception or if the timeout is reached
     */
    public ETComClient(final String progId, final int timeout) throws ETComException {
        initDispatch(progId);
        waitForConnection(timeout);
    }

    /**
     * Initializes the a single-threaded {@link ComThread} and sets the {@link ETComDispatch} instance using the default
     * program id returned from the {@link ActiveXComponent}.
     *
     * @param progId the programmatic identifier
     * @throws ETComException in case of a COM exception
     */
    private void initDispatch(final String progId) throws ETComException {
        final ETComProperty properties = ETComProperty.getInstance();
        final int timeout = properties.getTimeout();
        if (timeout == 0) {
            useTimeout = false;
            initSTA(progId);
        } else {
            useTimeout = true;
            initMTA(progId);
        }
    }

    /**
     * Initializes the a single-threaded {@link ComThread} and sets the {@link ETComDispatch} instance using the default
     * program id returned from the {@link ActiveXComponent}.
     *
     * @param progId the programmatic identifier
     * @throws ETComException in case of a COM exception
     */
    private void initSTA(final String progId) throws ETComException {
        try {
            ComThread.InitSTA();
            final ActiveXComponent component = new ActiveXComponent(StringUtils.isEmpty(progId) ?
                ETComProperty.DEFAULT_PROG_ID : progId);
            dispatch = new ETComDispatch(component.getObject(), false);
        } catch (final JacobException e) {
            throw new ETComException(e.getMessage(), e);
        }
    }

    /**
     * Initializes the a single-threaded {@link ComThread} and sets the {@link ETComDispatch} instance using the default
     * program id returned from the {@link ActiveXComponent}.
     *
     * @param progId the programmatic identifier
     * @throws ETComException in case of a COM exception
     */
    private void initMTA(final String progId) throws ETComException {
        try {
            ComThread.InitMTA();
            releaseDispatch = false;
            final InitDispatch initDispatch = new InitDispatch(progId);
            final InitDispatchExceptionHandler exceptionHandler = new InitDispatchExceptionHandler();
            initDispatch.setUncaughtExceptionHandler(exceptionHandler);
            initDispatch.start();

            final int timeout = ETComProperty.DEFAULT_CONNECTION_TIMEOUT;
            final long endTimeMillis = System.currentTimeMillis() + (long) timeout * 1000L;
            while (System.currentTimeMillis() < endTimeMillis) {
                if (dispatch != null && dispatch.isAttached()) {
                    return;
                }
                if (releaseDispatch) {
                    throw new ETComException(exceptionHandler.getThrowable());
                }
                Thread.sleep(100L);
            }
            throw new ETComTimeoutException(String.format(
                "Maximum timeout of %d seconds exceeded: COM server not available!", timeout));
        } catch (final InterruptedException e) {
            throw new ETComException(e.getMessage(), e);
        }
    }

    /**
     * Waits for a valid COM connection within the given timeout.
     *
     * @param timeout the timeout waiting for a connection
     * @throws ETComException in case of a COM exception or if the timeout is reached
     */
    private void waitForConnection(final int timeout) throws ETComException {
        final long endTimeMillis = System.currentTimeMillis() + (long) timeout * 1000L;
        while (timeout <= 0 || System.currentTimeMillis() < endTimeMillis) {
            try {
                if (isApplicationRunning()) {
                    return;
                } else {
                    Thread.sleep(1000L);
                }
            } catch (final ETComException e) {
                if (e instanceof ETComTimeoutException) {
                    return;
                }
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException ex) {
                    throw new ETComException(e.getMessage(), e);
                }
            } catch (final InterruptedException e) {
                throw new ETComException(e.getMessage(), e);
            }
        }
        throw new ETComTimeoutException(
            String.format("Maximum timeout of %d seconds exceeded: COM server not available!", timeout));
    }

    @Override
    public void close() {
        if (useTimeout) {
            releaseDispatch = true;
            ComThread.quitMainSTA();
        } else {
            try {
                releaseDispatch();
            } catch (final ETComException e) {
                // noop
            } finally {
                ComThread.Release();
            }
        }
    }

    @SuppressWarnings("checkstyle:superfinalize")
    @Override
    protected void finalize() throws Throwable {
        if (!useTimeout) {
            try {
                releaseDispatch();
            } finally {
                ComThread.Release();
                super.finalize();
            }
        } // else noop to prevent JVM crash
    }

    /**
     * Releases the {@link Dispatch}.
     *
     * @throws ETComException in case of a COM exception
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
    public ComTestEnvironment start() throws ETComException {
        return new TestEnvironment(dispatch.performDirectRequest("Start").toDispatch(), useTimeout);
    }

    @Override
    public ComTestEnvironment stop() throws ETComException {
        return new TestEnvironment(dispatch.performDirectRequest("Stop").toDispatch(), useTimeout);
    }

    @Override
    public ComTestEnvironment getTestEnvironment() throws ETComException {
        return new TestEnvironment(dispatch.performRequest("GetTestEnvironment").toDispatch(), useTimeout);
    }

    @Override
    public ComAnalysisEnvironment getAnalysisEnvironment() throws ETComException {
        return new AnalysisEnvironment(dispatch.performRequest("GetAnalysisEnvironment").toDispatch(), useTimeout);
    }

    @Override
    public ComTestManagement getTestManagement() throws ETComException {
        return new TestManagement(dispatch.performRequest("GetTestManagementModule").toDispatch(), useTimeout);
    }

    @Override
    public ComCaches getCaches() throws ETComException {
        return new Caches(dispatch.performRequest("Caches").toDispatch(), useTimeout);
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

    /**
     * Same as {@link #quit(int)} but without timeout.
     * Must be used for ECU-TEST below version 8.0.
     *
     * @return {@code true} if successful
     * @throws ETComException in case of a COM exception
     * @see #quit(int)
     */
    public boolean quit() throws ETComException {
        return dispatch.performRequest("Quit").getBoolean();
    }

    @Override
    public boolean quit(final int timeout) throws ETComException {
        if (ETPlugin.ToolVersion.parse(getVersion()).compareWithoutMicroTo(new ETPlugin.ToolVersion(8, 0, 0)) >= 0) {
            return dispatch.performRequest("Quit", new Variant(timeout)).getBoolean();
        } else {
            return quit();
        }
    }

    /**
     * Same as {@link #exit(int)} but without timeout.
     * Must be used for ECU-TEST below version 8.0.
     *
     * @return {@code true} if successful
     * @throws ETComException in case of a COM exception
     * @see #exit(int)
     */
    public boolean exit() throws ETComException {
        return dispatch.performRequest("Exit").getBoolean();
    }

    @Override
    public boolean exit(final int timeout) throws ETComException {
        if (ETPlugin.ToolVersion.parse(getVersion()).compareWithoutMicroTo(new ETPlugin.ToolVersion(8, 0, 0)) >= 0) {
            return dispatch.performRequest("Exit", new Variant(timeout)).getBoolean();
        } else {
            return exit();
        }
    }

    @Override
    public ComPackage openPackage(final String path) throws ETComException {
        return new Package(dispatch.performRequest("OpenPackage", new Variant(path)).toDispatch(), useTimeout);
    }

    @Override
    public boolean closePackage(final String path) throws ETComException {
        return dispatch.performRequest("ClosePackage", new Variant(path)).getBoolean();
    }

    /**
     * Same as {@link #openProject(String, boolean, String)} but with default parameters.
     *
     * @param path the full path name of the project to open
     * @return the {@link ComProject} dispatch, if the project is successfully opened, {@code null} otherwise
     * @throws ETComException in case of a COM exception
     * @see #openProject(String, boolean, String)
     */
    public ComProject openProject(final String path) throws ETComException {
        return openProject(path, false, "");
    }

    @Override
    public ComProject openProject(final String path, final boolean execInCurrentPkgDir,
                                  final String filterExpression) throws ETComException {
        return new Project(dispatch.performRequest("OpenProject", new Variant(path),
            new Variant(execInCurrentPkgDir), new Variant(filterExpression)).toDispatch(), useTimeout);
    }

    @Override
    public boolean closeProject(final String path) throws ETComException {
        return dispatch.performRequest("CloseProject", new Variant(path)).getBoolean();
    }

    @Override
    public boolean importProject(final String path, final String importPath, final String importConfigPath,
                                 final boolean replaceFiles) throws ETComException {
        return dispatch.performDirectRequest("ImportProject", new Variant(path), new Variant(replaceFiles),
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
        return new TestConfiguration(dispatch.performRequest("GetCurrentTestConfiguration").toDispatch(),
            useTimeout);
    }

    @Override
    public ComTestBenchConfiguration getCurrentTestBenchConfiguration() throws ETComException {
        return new TestBenchConfiguration(dispatch.performRequest("GetCurrentTestbenchConfiguration").toDispatch(),
            useTimeout);
    }

    @Override
    public boolean waitForIdle(final int timeout) throws ETComException {
        if (timeout == 0) {
            return dispatch.performDirectRequest("WaitForIdle").getBoolean();
        } else {
            return dispatch.performDirectRequest("WaitForIdle", new Variant(timeout)).getBoolean();
        }
    }

    @Override
    public boolean updateUserLibraries() throws ETComException {
        return dispatch.performDirectRequest("UpdateUserLibraries").getBoolean();
    }

    /**
     * Separate MTA COM thread that initializes and keeps the COM dispatch alive until the client gets closed.
     * Calling methods to this dispatch can be made from main or other MTA threads.
     */
    private final class InitDispatch extends Thread {

        private final String progId;

        /**
         * Instantiates a new {@link InitDispatch}.
         *
         * @param progId the programmatic identifier
         */
        InitDispatch(final String progId) {
            super();
            this.progId = StringUtils.isEmpty(progId) ? ETComProperty.DEFAULT_PROG_ID : progId;
        }

        @Override
        public void run() {
            ActiveXComponent component = null;
            try {
                ComThread.InitMTA();
                component = new ActiveXComponent(progId);
                dispatch = new ETComDispatch(component.getObject(), true);
                while (!dispatch.isAttached()) {
                    sleep(100L);
                }
                while (!releaseDispatch) {
                    sleep(100L);
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (component != null) {
                    component.safeRelease();
                }
                if (dispatch != null) {
                    dispatch.safeRelease();
                }
                ComThread.Release();
            }
        }
    }

    /**
     * Handles uncaught exceptions from {@link InitDispatch} thread.
     */
    private final class InitDispatchExceptionHandler implements UncaughtExceptionHandler {

        private Throwable throwable;

        /**
         * @return the throwable from failing thread
         */
        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            throwable = e;
            releaseDispatch = true;
        }
    }
}
