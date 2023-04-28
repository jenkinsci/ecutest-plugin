/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.client;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.util.DllUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Cache;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.Caches.CacheType;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;
import java.util.List;

/**
 * Client to generate ECU-TEST caches via COM interface.
 */
public class CacheClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to work properly.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(2021, 1, 0);

    private final CacheType type;
    private final String filePath;
    private final String dbChannel;
    private final boolean clear;

    /**
     * Instantiates a new {@link CacheClient}.
     *
     * @param type      the cache type
     * @param filePath  the database file path
     * @param dbChannel the database channel
     * @param clear     specifies whether to clear all caches
     */
    public CacheClient(final CacheType type, final String filePath, final String dbChannel, final boolean clear) {
        super();
        this.type = type;
        this.filePath = filePath;
        this.dbChannel = dbChannel;
        this.clear = clear;
    }

    public CacheType getType() {
        return type;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDbChannel() {
        return dbChannel;
    }

    public boolean isClear() {
        return clear;
    }

    /**
     * Generate cache files of this cache type.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     * @throws ETPluginException    in case of cache operation errors
     */
    public void generateCache(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException, ETPluginException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        logger.logInfo(String.format("Generating %s cache...", type.name()));
        if (!launcher.getChannel().call(new GenerateCacheCallable(type, filePath, dbChannel, clear, listener))) {
            throw new ETPluginException(String.format("Generating %s cache failed!", type.name()));
        }
        logger.logInfo(String.format("%s cache generated successfully.", type.name()));
    }

    /**
     * Checks the currently running ECU-TEST version for compatibility reasons and
     * tests whether the cache module is available.
     *
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if compatible, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public static boolean isCompatible(final FilePath workspace, final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, listener)) {
            logger.logError("No running ECU-TEST instance found, please configure one at first!");
            return false;
        }

        // Load JACOB library
        if (!DllUtil.loadLibrary(workspace.toComputer())) {
            logger.logError("Could not load JACOB library!");
            return false;
        }

        return launcher.getChannel().call(new CompatibleCacheCallable(ET_MIN_VERSION, listener));
    }

    /**
     * Checks already opened ECU-TEST instances.
     *
     * @param launcher the launcher
     * @param listener the listener
     * @return {@code true} if processes found, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    private static boolean checkETInstance(final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        final List<String> foundProcesses = ETClient.checkProcesses(launcher, listener, false);
        return !foundProcesses.isEmpty();
    }

    /**
     * {@link Callable} providing remote access to generate cache files for the given cache type.
     */
    private static final class GenerateCacheCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final CacheType type;
        private final String filePath;
        private final String dbChannel;
        private final boolean clear;
        private final TaskListener listener;

        GenerateCacheCallable(final CacheType type, final String filePath, final String dbChannel,
                              final boolean clear, final TaskListener listener) {
            this.type = type;
            this.filePath = filePath;
            this.dbChannel = dbChannel;
            this.clear = clear;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient client = new ETComClient(progId)) {
                final Caches caches = (Caches) client.getCaches();
                final Cache cache = caches.getCacheByType(type);
                final String cacheType = type.name();
                if (clear) {
                    logger.logInfo(String.format("- Removing all %s cache files...", cacheType));
                    cache.clear(true);
                }
                logger.logInfo(String.format("- Inserting %s to %s cache...", filePath, cacheType));
                cache.insert(filePath, dbChannel);
                final List<String> files = cache.getFiles();
                logger.logInfo(String.format("-> Available %s cache files: %s", cacheType, files.toString()));
            } catch (final ETComException e) {
                logger.logComException(e);
                return false;
            }
            return true;
        }
    }

    /**
     * {@link Callable} providing remote access to determine whether the cache module is available in ECU-TEST.
     */
    private static final class CompatibleCacheCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final TaskListener listener;
        private final ToolVersion minVersion;

        /**
         * Instantiates a {@link CompatibleCacheCallable}.
         *
         * @param minVersion the minimum required ECU-TEST version
         * @param listener   the listener
         */
        CompatibleCacheCallable(final ToolVersion minVersion, final TaskListener listener) {
            this.listener = listener;
            this.minVersion = minVersion;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isAvailable = false;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();

            // Check ECU-TEST version and cache module
            try (ETComClient comClient = new ETComClient(progId)) {
                final String comVersion = comClient.getVersion();
                final ToolVersion comToolVersion = ToolVersion.parse(comVersion);
                if (comToolVersion.compareTo(minVersion) < 0) {
                    logger.logError(String.format(
                        "The configured ECU-TEST version %s does not support the cache module. "
                            + "Please use at least ECU-TEST %s!", comVersion, minVersion.toMicroString()));
                }
                comClient.getCaches();
                isAvailable = true;
            } catch (final ETComException e) {
                logger.logError(String.format("The cache module is not available in running ECU-TEST instance! "
                    + "Please use at least ECU-TEST %s!", minVersion.toMicroString()));
                logger.logComException(e);
            }
            return isAvailable;
        }
    }
}
