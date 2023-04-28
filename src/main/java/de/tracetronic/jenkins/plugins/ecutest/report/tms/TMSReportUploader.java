/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.tms;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFReport;
import de.tracetronic.jenkins.plugins.ecutest.test.client.AbstractTMSClient;
import de.tracetronic.jenkins.plugins.ecutest.util.ToolVersion;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestManagement;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.security.ACL;
import jenkins.security.MasterToSlaveCallable;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Class providing the upload of {@link TRFReport}s to a test management system.
 */
public class TMSReportUploader extends AbstractTMSClient {

    /**
     * Defines the minimum required ECU-TEST version for this client to work properly.
     */
    private static final ToolVersion ET_MIN_VERSION = new ToolVersion(2021, 1, 0);

    /**
     * Uploads the reports to the test management system.
     *
     * @param reportFiles   the report files
     * @param credentialsId the credentials id
     * @param timeout       the export timeout
     * @param project       the project
     * @param workspace     the workspace
     * @param launcher      the launcher
     * @param listener      the listener
     * @return {@code true} if upload succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean upload(final List<FilePath> reportFiles, final String credentialsId, final String timeout,
                          final Item project, final FilePath workspace, final Launcher launcher,
                          final TaskListener listener) throws IOException, InterruptedException {
        boolean isUploaded = false;
        if (isCompatible(ET_MIN_VERSION, workspace, launcher, listener)) {
            try {
                final StandardUsernamePasswordCredentials credentials = getCredentials(credentialsId, project);
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
     * @param credentialsId the credentials id
     * @param project       the project
     * @return the credentials
     */
    @CheckForNull
    private StandardUsernamePasswordCredentials getCredentials(final String credentialsId, final Item project) {
        final List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider
            .lookupCredentials(StandardUsernamePasswordCredentials.class, project, ACL.SYSTEM,
                Collections.emptyList());
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
         * @param reportFiles the list of TRF files
         * @param timeout     the export timeout
         * @param listener    the listener
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
                logger.logComException(e);
            }
            return isUploaded;
        }
    }
}
