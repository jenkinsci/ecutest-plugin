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
package de.tracetronic.jenkins.plugins.ecutest.report.tms;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.security.ACL;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFReport;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTMSClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;

/**
 * Class providing the upload of {@link TRFReport}s to a test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TMSReportUploader extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to work properly.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(6, 5, 0, 0);

    /**
     * Uploads the reports to the test management system.
     *
     * @param reportFiles
     *            the report files
     * @param credentialsId
     *            the credentials id
     * @param timeout
     *            the export timeout
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if upload succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean upload(final List<FilePath> reportFiles, final String credentialsId, final String timeout,
            final FilePath workspace, final Launcher launcher, final TaskListener listener)
                    throws IOException, InterruptedException {
        boolean isUploaded = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = getCredentials(credentialsId);
                if (login(credentials, launcher, listener)) {
                    isUploaded = launcher.getChannel().call(
                            new UploadReportCallable(reportFiles, timeout, listener));
                }
            } finally {
                logout(launcher, listener);
            }
        }
        return isUploaded;
    }

    /**
     * Gets the credentials providing access to user name and password.
     *
     * @param credentialsId
     *            the credentials id
     * @return the credentials
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             the interrupted exception
     */
    @CheckForNull
    public StandardUsernamePasswordCredentials getCredentials(final String credentialsId) throws IOException,
            InterruptedException {
        final List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
                Collections.<DomainRequirement> emptyList());
        return CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(credentialsId));
    }

    /**
     * {@link Callable} enabling remote access to export reports to test management system.
     */
    private static final class UploadReportCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final List<FilePath> reportFiles;
        private final String timeout;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link UploadReportCallable}.
         *
         * @param reportFiles
         *            the list of TRF files
         * @param timeout
         *            the export timeout
         * @param listener
         *            the listener
         */
        UploadReportCallable(final List<FilePath> reportFiles, final String timeout,
                final TaskListener listener) {
            this.reportFiles = reportFiles;
            this.timeout = timeout;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isUploaded = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestManagement tm = (TestManagement) comClient.getTestManagement();
                for (final FilePath reportFile : reportFiles) {
                    logger.logInfo(String.format("-> Publishing TRF report: %s", reportFile.getRemote()));
                    if (!tm.exportReport(reportFile.getRemote(), Integer.parseInt(timeout))) {
                        isUploaded = false;
                        logger.logError("Publishing TRF report failed!");
                    }
                }
            } catch (final ETComException e) {
                isUploaded = false;
                logger.logComException(e.getMessage());
            }
            return isUploaded;
        }
    }
}
