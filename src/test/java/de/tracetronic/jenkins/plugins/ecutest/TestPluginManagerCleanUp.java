/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest;

import hudson.Util;
import hudson.PluginWrapper;

import java.io.IOException;

import org.jvnet.hudson.test.TestPluginManager;

/**
 * Cleanup the temporary directory created by {@link org.jvnet.hudson.test.TestPluginManager}.
 * <p>
 * Call {@link #registerCleanup()} at least once from anywhere.
 */
public class TestPluginManagerCleanUp {

    private static Thread deleteThread = null;

    public static synchronized void registerCleanup() {
        if (deleteThread != null) {
            return;
        }
        deleteThread = new Thread("HOTFIX: cleanup " + TestPluginManager.INSTANCE.rootDir) {

            @Override
            public void run() {
                if (TestPluginManager.INSTANCE != null && TestPluginManager.INSTANCE.rootDir != null
                        && TestPluginManager.INSTANCE.rootDir.exists()) {
                    // Work as PluginManager#stop
                    for (final PluginWrapper p : TestPluginManager.INSTANCE.getPlugins()) {
                        p.stop();
                        p.releaseClassLoader();
                    }
                    TestPluginManager.INSTANCE.getPlugins().clear();
                    System.gc();
                    try {
                        Util.deleteRecursive(TestPluginManager.INSTANCE.rootDir);
                    } catch (final IOException x) {
                        x.printStackTrace();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(deleteThread);
    }
}
