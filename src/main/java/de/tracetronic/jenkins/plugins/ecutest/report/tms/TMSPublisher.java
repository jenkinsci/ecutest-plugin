/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TMSValidator;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Publisher providing the export of reports to a test management system.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TMSPublisher extends AbstractReportPublisher {

    /**
     * Defines the default timeout for importing a project.
     */
    private static final int DEFAULT_TIMEOUT = 60;

    @Nonnull
    private final String toolName;
    @Nonnull
    private final String credentialsId;
    private String timeout = String.valueOf(getDefaultTimeout());

    /**
     * Instantiates a new {@link TMSPublisher}.
     *
     * @param toolName      the tool name identifying the {@link ETInstallation} to be used
     * @param credentialsId the credentials id
     */
    @DataBoundConstructor
    public TMSPublisher(@Nonnull final String toolName, @Nonnull final String credentialsId) {
        super();
        this.toolName = StringUtils.trimToEmpty(toolName);
        this.credentialsId = StringUtils.trimToEmpty(credentialsId);
    }

    /**
     * @return the default timeout
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the {@link ETInstallation} name
     */
    @Nonnull
    public String getToolName() {
        return toolName;
    }

    /**
     * @return the credentials id used for authentication
     */
    @Nonnull
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * @return the export timeout
     */
    @Nonnull
    public String getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout
     */
    @DataBoundSetter
    public void setTimeout(@CheckForNull final String timeout) {
        this.timeout = StringUtils.defaultIfBlank(timeout, String.valueOf(getDefaultTimeout()));
    }

    @Override
    public void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                              final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final TTConsoleLogger logger = getLogger();
        logger.logInfo("Publishing reports to test management system...");

        if (isSkipped(true, run, launcher)) {
            return;
        }

        final List<FilePath> reportFiles = getReportFiles(run, workspace, launcher);
        if (reportFiles.isEmpty() && !isAllowMissing()) {
            throw new ETPluginException("Empty test results are not allowed, setting build status to FAILURE!");
        }

        boolean isPublished = false;
        if (isETRunning(launcher)) {
            isPublished = publishReports(reportFiles, run.getParent(), workspace, launcher, listener);
        } else {
            final ETClient etClient = getToolClient(toolName, run, workspace, launcher, listener);
            if (etClient.start(false, workspace, launcher, listener)) {
                isPublished = publishReports(reportFiles, run.getParent(), workspace, launcher, listener);
            } else {
                logger.logError(String.format("Starting %s failed.", toolName));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", toolName));
            }
        }

        if (isPublished) {
            logger.logInfo("Reports published successfully to test management system.");
        } else {
            logger.logInfo("Failed publishing report to test management system.");
            run.setResult(Result.FAILURE);
        }
    }

    /**
     * Publishes the reports to the test management system.
     *
     * @param reportFiles the report files
     * @param project     the project
     * @param workspace   the workspace
     * @param launcher    the launcher
     * @param listener    the listener
     * @return {@code true}, if upload succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean publishReports(final List<FilePath> reportFiles, final Item project, final FilePath workspace,
                                   final Launcher launcher, final TaskListener listener)
        throws IOException, InterruptedException {
        return new TMSReportUploader().upload(reportFiles, credentialsId, timeout, project, workspace, launcher,
            listener);
    }

    @Override
    protected String getUrlName() {
        throw new NotImplementedException();
    }

    /**
     * DescriptorImpl for {@link TMSPublisher}.
     */
    @Symbol("publishTMS")
    @Extension(ordinal = 10001)
    public static final class DescriptorImpl extends AbstractReportDescriptor {

        /**
         * Validator to check form fields.
         */
        private final TMSValidator tmsValidator = new TMSValidator();

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
        }

        /**
         * @return the default timeout
         */
        public static int getDefaultTimeout() {
            return DEFAULT_TIMEOUT;
        }

        /**
         * Validates the timeout.
         *
         * @param value the timeout
         * @return the form validation
         */
        public FormValidation doCheckTimeout(@QueryParameter final String value) {
            return tmsValidator.validateTimeout(value, getDefaultTimeout());
        }

        /**
         * Fills the credentials drop-down menu.
         *
         * @param item          the item
         * @param credentialsId the credentials id
         * @return the credentials items
         */
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath final Item item,
                                                     @QueryParameter final String credentialsId) {
            final StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(credentialsId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(credentialsId);
                }
            }
            return result
                .includeEmptyValue()
                .includeMatchingAs(ACL.SYSTEM, item, StandardCredentials.class,
                    Collections.emptyList(),
                    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.TMSPublisher_DisplayName();
        }
    }
}
