/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.EnvUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.util.FormValidation;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Publisher providing the generation and upload of {@link ATXReport}s to test.guide.
 */
public class ATXPublisher extends AbstractReportPublisher {

    /**
     * The URL name to {@link ATXZipReport}s holding by {@link AbstractATXAction}.
     */
    protected static final String URL_NAME = "atx-reports";

    @Nonnull
    private final String atxName;
    private ATXInstallation atxInstallation;
    /**
     * Specifies whether to fail the build if upload is enabled and the server is offline.
     *
     * @since 2.11
     */
    private boolean failOnOffline;
    /**
     * Specifies whether to use report generator settings from persisted configurations file (XML).
     *
     * @since 2.14
     */
    private boolean usePersistedSettings;
    /**
     * Specifies whether to inject common build variables as ATX constants.
     *
     * @since 2.15
     */
    private boolean injectBuildVars;

    /**
     * Instantiates a new {@link ATXPublisher}.
     *
     * @param atxName the tool name identifying the {@link ATXInstallation} to be used
     */
    @DataBoundConstructor
    public ATXPublisher(@Nonnull final String atxName) {
        super();
        this.atxName = StringUtils.trimToEmpty(atxName);
    }

    @Nonnull
    public String getAtxName() {
        return atxName;
    }

    public ATXInstallation getAtxInstallation() {
        return atxInstallation;
    }

    /**
     * Sets the ATX installation and the derived name.
     *
     * @param atxInstallation the ATX installation
     */
    @DataBoundSetter
    public void setAtxInstallation(final ATXInstallation atxInstallation) {
        this.atxInstallation = atxInstallation;
    }

    public boolean isFailOnOffline() {
        return failOnOffline;
    }

    @DataBoundSetter
    public void setFailOnOffline(final boolean failOnOffline) {
        this.failOnOffline = failOnOffline;
    }

    public boolean isUsePersistedSettings() {
        return usePersistedSettings;
    }

    @DataBoundSetter
    public void setUsePersistedSettings(final boolean usePersistedSettings) {
        this.usePersistedSettings = usePersistedSettings;
    }

    public boolean isInjectBuildVars() {
        return injectBuildVars;
    }

    @DataBoundSetter
    public void setInjectBuildVars(final boolean injectBuildVars) {
        this.injectBuildVars = injectBuildVars;
    }

    @Override
    public void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                              final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final EnvVars envVars = run.getEnvironment(listener);
        ProcessUtil.checkOS(launcher);

        if (isSkipped(true, run, launcher)) {
            return;
        }

        if (atxInstallation == null) {
            atxInstallation = getInstallation(envVars);
            if (atxInstallation == null) {
                throw new ETPluginException("Selected test.guide installation is not configured!");
            }
        }

        final TTConsoleLogger logger = getLogger();
        final String serverName = EnvUtil.expandEnvVar(getAtxName(), envVars, atxInstallation.getName());
        logger.logInfo(String.format("Publishing ATX reports to %s...", serverName));

        boolean isPublished = false;
        if (isETRunning(launcher, listener)) {
            isPublished = publishReports(atxInstallation, run, workspace, launcher, listener);
        } else {
            final String toolName = atxInstallation.getToolName();
            final ETClient etClient = getToolClient(toolName, run, workspace, launcher, listener);
            if (etClient.start(false, workspace, launcher, listener)) {
                isPublished = publishReports(atxInstallation, run, workspace, launcher, listener);
            } else {
                logger.logError(String.format("Starting %s failed.", toolName));
            }
            if (!etClient.stop(true, workspace, launcher, listener)) {
                logger.logError(String.format("Stopping %s failed.", toolName));
            }
        }

        if (isPublished) {
            logger.logInfo("ATX reports published successfully.");
        } else {
            run.setResult(Result.FAILURE);
        }
    }

    /**
     * Publishes the ATX reports by first generating them and depending on whether ATX upload is enabled also starting
     * the upload.
     *
     * @param installation the installation
     * @param run          the run
     * @param workspace    the workspace
     * @param launcher     the launcher
     * @param listener     the listener
     * @return {@code true} if ATX processing is successful, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean publishReports(final ATXInstallation installation, final Run<?, ?> run, final FilePath workspace,
                                   final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = getLogger();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        final boolean isUploadEnabled = isUploadEnabled(installation);
        final boolean isResourceAdapterUploadEnabled = isResourceAdapterUploadEnabled(installation);
        final boolean isServerReachable = isServerReachable(installation, run, launcher, listener);
        if (isUploadEnabled && (isResourceAdapterUploadEnabled || isServerReachable)) {
            logger.logInfo("- Generating and uploading ATX reports...");
            final ATXReportUploader uploader = new ATXReportUploader(installation);
            return uploader.upload(reportDirs, isUsePersistedSettings(), isInjectBuildVars(), isAllowMissing(),
                run, launcher, listener);
        } else if (isUploadEnabled && failOnOffline) {
            logger.logError("-> test.guide server is not reachable, setting build status to FAILURE!");
            return false;
        } else {
            logger.logInfo("- Generating ATX reports...");
            if (!isServerReachable) {
                logger.logWarn("-> ATX upload will be skipped because selected test.guide server is not reachable!");
            }
            final FilePath archiveTarget = getArchiveTarget(run);

            final ATXReportGenerator generator = new ATXReportGenerator(installation);
            return generator.generate(archiveTarget, reportDirs, isUsePersistedSettings(), isInjectBuildVars(),
                    isAllowMissing(), isArchiving(), isKeepAll(), run, launcher, listener);
        }
    }

    /**
     * Checks whether the ATX upload setting is enabled.
     *
     * @param installation the ATX installation
     * @return {@code true} if upload is enabled, {@code false} otherwise
     */
    private boolean isUploadEnabled(final ATXInstallation installation) {
        final ATXConfig config = installation.getConfig();
        final Optional<ATXSetting<?>> uploadSetting = config.getSettingByName("uploadToServer");
        return uploadSetting.isPresent() && ((ATXBooleanSetting) uploadSetting.get()).getValue();
    }

    /**
     * Checks whether the ATX upload through ResourceAdapter setting is configured.
     *
     * @param installation the ATX installation
     * @return {@code true} if upload through ResourceAdapter is configured, {@code false} otherwise
     */
    private boolean isResourceAdapterUploadEnabled(final ATXInstallation installation) {
        final ATXConfig config = installation.getConfig();
        final Optional<ATXSetting<?>> uploadSetting = config.getSettingByName("uploadThroughResourceAdapter");
        if (uploadSetting.isPresent()) {
            final String value = ((ATXTextSetting) uploadSetting.get()).getValue();
            return Integer.parseInt(value) > 0;
        }
        return false;
    }

    /**
     * Checks whether the selected test.guide server is reachable.
     *
     * @param installation the ATX installation
     * @param run          the run
     * @param launcher     the launcher
     * @param listener     the listener
     * @return {@code true} if server is reachable, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean isServerReachable(final ATXInstallation installation, final Run<?, ?> run, final Launcher launcher,
                                      final TaskListener listener) throws IOException, InterruptedException {
        final ATXConfig config = installation.getConfig();
        final EnvVars envVars = run.getEnvironment(listener);
        return launcher.getChannel().call(new TestConnectionCallable(config, envVars, listener));
    }

    /**
     * Gets the {@link ATXInstallation} by descriptor and name.
     *
     * @return the {@link ATXInstallation}
     */
    @CheckForNull
    public ATXInstallation getInstallation() {
        return getInstallation(new EnvVars());
    }

    /**
     * Gets the {@link ATXInstallation} by descriptor and expanded name.
     *
     * @param envVars the environment variables
     * @return the {@link ATXInstallation}
     */
    @CheckForNull
    public ATXInstallation getInstallation(final EnvVars envVars) {
        final String expandedName = envVars.expand(atxName);
        return ATXInstallation.get(expandedName);
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    /**
     * {@link Callable} providing remote access to test the test.guide server availability.
     */
    private static final class TestConnectionCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ATXConfig config;
        private final EnvVars envVars;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link TestConnectionCallable}.
         *
         * @param config   the ATX configuration
         * @param envVars  the environment variables
         * @param listener the listener
         */
        TestConnectionCallable(final ATXConfig config, final EnvVars envVars, final TaskListener listener) {
            this.config = config;
            this.envVars = envVars;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            final Object ignoreSSL = config.getSettingValueByGroup("ignoreSSL", ATXSetting.SettingsGroup.CONNECTION);
            if (ignoreSSL != null) {
                final String baseUrl = ATXUtil.getBaseUrl(config, envVars);
                final String proxyUrl = ATXUtil.getProxyUrl(config, envVars);
                final ATXValidator validator = new ATXValidator();
                final FormValidation validation = validator.testConnection(baseUrl, proxyUrl, (boolean) ignoreSSL);
                if (validation.kind.equals(FormValidation.Kind.WARNING)) {
                    final TTConsoleLogger logger = new TTConsoleLogger(listener);
                    logger.logWarn(validation.getMessage());
                    return true;
                }
                return validation.kind.equals(FormValidation.Kind.OK);
            }
            return false;
        }
    }

    /**
     * DescriptorImpl for {@link ATXPublisher}.
     */
    @Symbol("publishATX")
    @Extension(ordinal = 10007)
    public static class DescriptorImpl extends AbstractReportDescriptor {

        /**
         * Gets all configured {@link ATXInstallation}s.
         *
         * @return the list of {@link ATXInstallation}s
         */
        public ATXInstallation[] getInstallations() {
            return ATXInstallation.all();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ATXPublisher_DisplayName();
        }
    }
}
