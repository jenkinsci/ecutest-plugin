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
package de.tracetronic.jenkins.plugins.ecutest;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import hudson.Functions;
import hudson.model.Label;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import hudson.util.VersionNumber;

import java.io.File;
import java.net.URLConnection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 * Base class for all Jenkins related system tests.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class SystemTestBase {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule() {

        private boolean origDefaultUseCache = true;

        @Override
        public void before() throws Throwable {
            if (Functions.isWindows()) {
                // To avoid JENKINS-4409
                final URLConnection connection = new File(".").toURI().toURL().openConnection();
                origDefaultUseCache = connection.getDefaultUseCaches();
                connection.setDefaultUseCaches(false);
            }
            super.before();
        }

        @Override
        public void after() throws Exception {
            super.after();
            if (Functions.isWindows()) {
                final URLConnection connection = new File(".").toURI().toURL().openConnection();
                connection.setDefaultUseCaches(origDefaultUseCache);
            }
        }
    };

    /**
     * Setups the JenkinsRule for a test.
     *
     * @throws Exception
     *             signals that an exception has occurred
     */
    @Before
    public void setUp() throws Exception {
        // Check if test is probably annotated with @WithoutJenkins
        if (jenkins.jenkins != null) {
            jenkins.jenkins.setSecurityRealm(jenkins.createDummySecurityRealm());
        }
    }

    /**
     * @return the web client
     */
    protected WebClient getWebClient() {
        final WebClient webClient = jenkins.createWebClient();
        webClient.setIncorrectnessListener(new SilentIncorrectnessListener());
        webClient.setCssErrorHandler(new QuietCssErrorHandler());
        return webClient;
    }

    /**
     * Creates a dumb slave and assumes that it runs on a Windows machine.
     *
     * @return the dumb slave
     * @throws Exception
     *             signals that an exception has occurred
     */
    protected DumbSlave assumeWindowsSlave() throws Exception {
        // Windows only
        final DumbSlave slave = jenkins.createOnlineSlave(Label.get("slaves"));
        final SlaveComputer computer = slave.getComputer();
        assumeFalse("Test is Windows only!", computer.isUnix());
        return slave;
    }

    /**
     * To use the @Symbol annotation in tests, minimum workflow-cps version 2.10 is required.
     * This dependency comes with other dependency version requirements, as stated by this method.
     * To run tests restricted by this method, type
     *
     * <pre>
     * mvn clean install -Djenkins.version=1.642.1 -Djava.level=7 -Dworkflow-aggregator.version=2.3 -Dworkflow-basic-steps.version=2.1 -Dworkflow-cps.version=2.10 -Dworkflow-step-api.version=2.3
     * </pre>
     */
    protected static void assumeSymbolDependencies() {
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("jenkins.version"), "1.642.1");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("java.level"), "7");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-aggregator.version"), "2.3");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-basic-steps.version"), "2.1");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-cps.version"), "2.10");
        assumePropertyIsGreaterThanOrEqualTo(System.getProperty("workflow-step-api.version"), "2.3");
    }

    /**
     * Checks if the given property is not null, and if it's greater than or equal to the given version.
     *
     * @param property
     *            the property to be checked
     * @param version
     *            the version on which the property is checked against
     */
    private static void assumePropertyIsGreaterThanOrEqualTo(@CheckForNull final String property,
            @Nonnull final String version) {
        assumeThat(property, notNullValue());
        assumeThat(new VersionNumber(property).compareTo(new VersionNumber(version)), is(greaterThanOrEqualTo(0)));
    }
}
