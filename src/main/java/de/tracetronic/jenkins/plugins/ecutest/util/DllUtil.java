/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import com.jacob.com.LibraryLoader;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.PluginWrapper;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;
import jenkins.model.Jenkins;
import jenkins.model.Jenkins.MasterComputer;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing library operations, especially for JACOB COM Bridge.
 */
public final class DllUtil {

    private static final Logger LOGGER = Logger.getLogger(DllUtil.class.getName());

    /**
     * Defines the provided JACOB DLL for 32-bit systems.
     */
    private static final String JACOB_DLL_X86 = "jacob-1.20-x86.dll";

    /**
     * Defines the provided JACOB DLL for 64-bit systems.
     */
    private static final String JACOB_DLL_X64 = "jacob-1.20-x64.dll";

    /**
     * Instantiates a {@link DllUtil}.
     */
    private DllUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Loads the JACOB library.
     *
     * @param computer the computer
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static boolean loadLibrary(@CheckForNull final Computer computer) throws IOException, InterruptedException {
        if (computer == null) {
            return false;
        }
        final FilePath libPath = getJacobLibrary(computer);
        return libPath.act(new LoadLibraryCallable());
    }

    /**
     * Gets the file path to the JACOB library.
     *
     * @param computer the computer
     * @return the library file path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    private static FilePath getJacobLibrary(final Computer computer) throws IOException, InterruptedException {
        final FilePath jacobLib;
        if (computer instanceof MasterComputer) {
            jacobLib = getLocalLibrary(computer);
        } else {
            jacobLib = getRemoteLibrary(computer);
            final FilePath localLib = getLocalLibrary(computer);
            if (!copyLibrary(localLib, jacobLib)) {
                throw new IOException("Could not copy JACOB library to Jenkins agent!");
            }
        }
        return jacobLib;
    }

    /**
     * Gets the local file path to the JACOB library.
     *
     * @param computer the computer
     * @return the local library file path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    private static FilePath getLocalLibrary(final Computer computer) throws IOException, InterruptedException {
        FilePath localLib = null;
        final String libFile = getLibraryFile(computer);
        final Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance == null) {
            return new FilePath(FilePath.localChannel, libFile);
        }
        final File rootDir = instance.getRootDir();
        final PluginWrapper wrapper = instance.getPluginManager().getPlugin("ecutest");
        if (wrapper != null) {
            localLib = new FilePath(FilePath.localChannel, rootDir.getAbsolutePath() + "/plugins/"
                + wrapper.getShortName() + "/WEB-INF/lib/" + libFile);
        }
        return localLib;
    }

    /**
     * Gets the remote file path to the JACOB library.
     *
     * @param computer the computer
     * @return the remote library file path
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    private static FilePath getRemoteLibrary(final Computer computer) throws IOException, InterruptedException {
        FilePath remoteLib = null;
        final String libFile = getLibraryFile(computer);
        final Node node = computer.getNode();
        if (node != null) {
            final FilePath rootPath = node.getRootPath();
            if (rootPath != null) {
                remoteLib = rootPath.child("lib/" + libFile);
            }
        }
        return remoteLib;

    }

    /**
     * Gets the library file for the respective system architecture.
     *
     * @param computer the computer
     * @return the library file
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    public static String getLibraryFile(final Computer computer) throws IOException, InterruptedException {
        return "amd64".equals(computer.getSystemProperties().get("os.arch")) ? JACOB_DLL_X64 : JACOB_DLL_X86;
    }

    /**
     * Copies the library file from source to the destination which can be on remote.
     *
     * @param src  the source file
     * @param dest the destination file
     * @return {@code true} if successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     */
    private static boolean copyLibrary(final FilePath src, final FilePath dest) throws IOException,
        InterruptedException {
        return PathUtil.copyRemoteFile(src, dest);
    }

    /**
     * {@link FileCallable} providing remote file access to load a library.
     */
    private static final class LoadLibraryCallable extends MasterToSlaveFileCallable<Boolean> {

        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new {@link LoadLibraryCallable}.
         */
        LoadLibraryCallable() {
        }

        @Override
        public Boolean invoke(final File libFile, final VirtualChannel channel) {
            if (libFile.exists() && libFile.isFile()) {
                final String libProperty = System.getProperty(LibraryLoader.JACOB_DLL_PATH);
                if (!StringUtils.isBlank(libProperty)) {
                    LOGGER.log(Level.FINE, String.format("%s already loaded.", libFile));
                } else {
                    System.setProperty(LibraryLoader.JACOB_DLL_PATH, libFile.getAbsolutePath());
                    try {
                        LibraryLoader.loadJacobLibrary();
                    } catch (final UnsatisfiedLinkError e) {
                        LOGGER.log(Level.SEVERE,
                            String.format("Loading local library %s failed: %s", libFile, e.getMessage()));
                        return false;
                    }
                }
            } else {
                LOGGER.log(Level.SEVERE, String.format("Local library %s does not exist!", libFile));
                return false;
            }
            return true;
        }
    }
}
