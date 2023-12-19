/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import hudson.Launcher;
import hudson.model.Computer;
import org.apache.commons.lang.StringUtils;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class providing process and system operations.
 */
public final class ProcessUtil {

    /**
     * Defines ecu.test related process names.
     */
    private static final List<String> ET_PROCS = Arrays.asList("ECU-TEST.exe", "ecu.test.exe", "ECU-TEST_COM.exe", "ECU-TE~1.EXE");

    /**
     * Defines Tool-Server related process names.
     */
    private static final List<String> TS_PROCS = Collections.singletonList("Tool-Server.exe");

    /**
     * Instantiates a new {@link ProcessUtil}.
     */
    private ProcessUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Check the ecu.test processes and kills them if appropriate.
     *
     * @param kill specifies whether to task-kill the running processes
     * @return the list of found or killed processes
     */
    public static List<String> checkETProcesses(final boolean kill) {
        return checkProcesses(ET_PROCS, kill);
    }

    /**
     * Check the Tool-Server processes and kills them if appropriate.
     *
     * @param kill specifies whether to task-kill the running processes
     * @return the list of found or killed processes
     */
    public static List<String> checkTSProcess(final boolean kill) {
        return checkProcesses(TS_PROCS, kill);
    }

    /**
     * Checks a list of processes and kills them if appropriate.
     *
     * @param processes the list of processes to check
     * @param kill      specifies whether to task-kill the running processes
     * @return the list of found or killed processes
     */
    private static List<String> checkProcesses(final List<String> processes, final boolean kill) {
        final List<String> found = new ArrayList<>();
        WinProcess.enableDebugPrivilege();
        for (final WinProcess winProcess : WinProcess.all()) {
            try {
                final String cmdLine = winProcess.getCommandLine();
                for (final String process : processes) {
                    if (StringUtils.containsIgnoreCase(cmdLine, process)) {
                        found.add(process);
                        if (kill) {
                            killProcess(winProcess);
                        }
                    }
                }
            } catch (final WinpException ignored) {
                // Skip system pseudo-processes with insufficient security privileges
            }
        }
        return found;
    }

    /**
     * Kills this process and all the descendant processes that this process launched.
     *
     * @param process the process to kill
     */
    private static void killProcess(final WinProcess process) {
        process.killRecursively();
    }

    /**
     * Checks the operating system of a launcher.
     *
     * <p>Most of the builders and publishers implemented by this plugin require to run on Windows.
     *
     * @param launcher the launcher
     * @throws ETPluginException if Unix-based launcher
     */
    public static void checkOS(final Launcher launcher) throws ETPluginException {
        if (launcher.isUnix()) {
            throw new ETPluginException("Trying to build Windows related configuration on an Unix-based system! "
                + "Restrict the project to be built on a particular Windows agent or master.");
        }
    }

    /**
     * From https://stackoverflow.com/a/35418180
     *
     * <p>Reads the .exe file to find headers that tell us if the file is 32 or 64 bit.
     *
     * <p>Note: Assumes byte pattern 0x50, 0x45, 0x00, 0x00 just before the byte that tells us the architecture.
     *
     * @param filePath fully qualified .exe file path.
     * @return {@code true} if the file is a 64-bit executable, {@code false} otherwise.
     * @throws IOException if there is a problem reading the file or the file does not end in .exe.
     */
    @SuppressWarnings("checkstyle:booleanexpressioncomplexity")
    public static boolean is64BitExecutable(final String filePath) throws IOException {
        if (!filePath.endsWith(".exe")) {
            throw new IOException(String.format("%s is not a Windows .exe file.", filePath));
        }
        // Should be enough bytes to make it to the necessary header
        final byte[] fileData = new byte[1024];
        try (FileInputStream input = new FileInputStream(filePath)) {
            final int bytesRead = input.read(fileData);
            for (int i = 0; i < bytesRead; i++) {
                if (fileData[i] == 0x50
                    && i + 5 < bytesRead
                    && fileData[i + 1] == 0x45
                    && fileData[i + 2] == 0
                    && fileData[i + 3] == 0) {
                    return fileData[i + 4] == 0x64;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the underlying JVM supports 64-bit architecture.
     *
     * @param computer the computer
     * @return {@code true} 64-bit architecture is supported, {@code false} otherwise.
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public static boolean is64BitJVM(final Computer computer) throws IOException, InterruptedException {
        if (computer == null) {
            throw new IOException("Could not access node properties!");
        }
        return "amd64".equals(computer.getSystemProperties().get("os.arch"));
    }
}
