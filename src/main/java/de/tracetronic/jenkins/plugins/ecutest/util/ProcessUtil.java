/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.util;

import hudson.Launcher;
import hudson.model.BuildListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;

/**
 * Utility class providing process and system operations.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public final class ProcessUtil {

    private static final Logger LOGGER = Logger.getLogger(DllUtil.class.getName());

    /**
     * Defines ECU-TEST related process names.
     */
    private static final List<String> ET_PROCS = Arrays.asList("ECU-TEST.exe", "ECU-TEST_COM.exe", "ECU-TE~1.EXE");

    /**
     * Defines Tool-Server related process names.
     */
    private static final List<String> TS_PROCS = Arrays.asList("Tool-Server.exe");

    /**
     * Instantiates a new {@link ProcessUtil}.
     */
    private ProcessUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Check the ECU-TEST processes and kills them if appropriate.
     *
     * @param kill
     *            specifies whether to task-kill the running processes
     * @return the list of found or killed processes
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    public static List<String> checkETProcesses(final boolean kill) throws IOException {
        return checkProcesses(ET_PROCS, kill);
    }

    /**
     * Check the Tool-Server processes and kills them if appropriate.
     *
     * @param kill
     *            specifies whether to task-kill the running processes
     * @return the list of found or killed processes
     * @throws IOException
     *             signals that an I/O exception has occurred
     */
    public static List<String> checkTSProcess(final boolean kill) throws IOException {
        return checkProcesses(TS_PROCS, kill);
    }

    /**
     * Checks a list of processes and kills them if appropriate.
     *
     * @param processes
     *            the list of processes to check
     * @param kill
     *            specifies whether to task-kill the running processes
     * @return the list of found or killed processes
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    private static List<String> checkProcesses(final List<String> processes, final boolean kill) throws IOException {
        final List<String> found = new ArrayList<String>();
        for (final String process : processes) {
            BufferedReader reader = null;
            try {
                final String cmd = String.format(System.getenv("windir")
                        + "\\system32\\tasklist.exe /fi \"IMAGENAME eq %s\" /fo table /nh", process);
                final Process p = Runtime.getRuntime().exec(cmd);
                if (p.waitFor() == 0) {
                    reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(process)) {
                            found.add(process);
                            if (kill) {
                                killProcess(process);
                            }
                        }
                    }
                }
            } catch (final IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
                throw new IOException(e);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return found;
    }

    /**
     * Kills a specific process.
     *
     * @param process
     *            the process name
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for the completion
     */
    private static void killProcess(final String process) throws IOException, InterruptedException {
        final String cmd = String.format(System.getenv("windir") + "\\system32\\taskkill.exe /f /im %s", process);
        Runtime.getRuntime().exec(cmd).waitFor();
    }

    /**
     * Checks the operating system of a launcher.
     * <p>
     * Most of the builders and publishers implemented by this plugin require to run on Windows.
     *
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if Windows launcher, {@code false} if Unix-based launcher
     */
    public static boolean checkOS(final Launcher launcher, final BuildListener listener) {
        if (launcher.isUnix()) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logError("Trying to build Windows related configuration on an Unix-based system! "
                    + "Restrict the project to be built on a particular Windows slave or master.");
            return false;
        }
        return true;
    }
}
