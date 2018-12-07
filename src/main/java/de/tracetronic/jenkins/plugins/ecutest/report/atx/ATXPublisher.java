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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import hudson.CopyOnWrite;
import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.remoting.Callable;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import de.tracetronic.jenkins.plugins.ecutest.ETPlugin;
import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportDescriptor;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline.ATXPublishStep;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.ETClient;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;

/**
 * Publisher providing the generation and upload of {@link ATXReport}s to TEST-GUIDE.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXPublisher extends AbstractReportPublisher {

    /**
     * The URL name to {@link ATXTZipReport}s holding by {@link AbstractATXAction}.
     */
    protected static final String URL_NAME = "atx-reports";

    @Nonnull
    private final String atxName;
    private transient ATXInstallation atxInstallation;

    /**
     * Instantiates a new {@link ATXPublisher}.
     *
     * @param atxName
     *            the tool name identifying the {@link ATXInstallation} to be used
     */
    @DataBoundConstructor
    public ATXPublisher(@Nonnull final String atxName) {
        super();
        this.atxName = StringUtils.trimToEmpty(atxName);
    }

    /**
     * Instantiates a new {@link ATXPublisher} for direct use from {@link ATXPublishStep}.
     *
     * @param atxInstallation
     *            the {@link ATXInstallation}
     */
    public ATXPublisher(@Nonnull final ATXInstallation atxInstallation) {
        super();
        atxName = StringUtils.trimToEmpty(atxInstallation.getName());
        this.atxInstallation = atxInstallation;
    }

    /**
     * @return the {@link ATXInstallation} name
     */
    @Nonnull
    public String getAtxName() {
        return atxName;
    }

    @Override
    public void performReport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException, ETPluginException {
        final TTConsoleLogger logger = getLogger();
        logger.logInfo(String.format("Publishing ATX reports to %s...", atxName));
        ProcessUtil.checkOS(launcher);

        if (isSkipped(true, run, launcher)) {
            return;
        }

        if (atxInstallation == null) {
            atxInstallation = getInstallation(run.getEnvironment(listener));
            if (atxInstallation == null) {
                throw new ETPluginException("Selected TEST-GUIDE installation is not configured!");
            }
        }

        boolean isPublished = false;
        if (isETRunning(launcher)) {
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
     * Publishes the ATX reports by first generating them and depending
     * on whether ATX upload is enabled also starting the upload.
     *
     * @param installation
     *            the installation
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if ATX processing is successful, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean publishReports(final ATXInstallation installation, final Run<?, ?> run, final FilePath workspace,
            final Launcher launcher, final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = getLogger();
        final List<FilePath> reportDirs = getReportDirs(run, workspace, launcher);
        final boolean isUploadEnabled = isUploadEnabled(installation);
        final boolean isServerReachable = isServerReachable(installation, launcher, run.getEnvironment(listener));
        if (isUploadEnabled && isServerReachable) {
            logger.logInfo("- Generating and uploading ATX reports...");
            final ATXReportUploader uploader = new ATXReportUploader(installation);
            return uploader.upload(reportDirs, isAllowMissing(), run, launcher, listener);
        } else {
            logger.logInfo("- Generating ATX reports...");
            if (isUploadEnabled && !isServerReachable) {
                logger.logWarn("-> ATX upload will be skipped because selected TEST-GUIDE server is not reachable!");
            }
            final FilePath archiveTarget = getArchiveTarget(run);

            final ATXReportGenerator generator = new ATXReportGenerator(installation);
            return generator.generate(archiveTarget, reportDirs, isAllowMissing(), isArchiving(), isKeepAll(), run,
                    launcher, listener);
        }
    }

    /**
     * Checks whether the ATX upload setting is enabled.
     *
     * @param installation
     *            the ATX installation
     * @return {@code true} if upload is possible, {@code false} otherwise
     */
    @SuppressWarnings("rawtypes")
    private boolean isUploadEnabled(final ATXInstallation installation) {
        final ATXConfig config = installation.getConfig();
        final List<ATXSetting> uploadSettings = config.getConfigByName("uploadConfig");
        final Object uploadToServer = config.getSettingValueByName("uploadToServer", uploadSettings);
        return uploadToServer != null && (boolean) uploadToServer;
    }

    /**
     * Checks whether the selected TEST-GUIDE server is reachable.
     *
     * @param installation
     *            the ATX installation
     * @param launcher
     *            the launcher
     * @param envVars
     *            the the environment variables
     * @return {@code true} if server is reachable, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean isServerReachable(final ATXInstallation installation, final Launcher launcher,
            final EnvVars envVars) throws IOException,
            InterruptedException {
        final ATXConfig config = installation.getConfig();
        return launcher.getChannel().call(new TestConnectionCallable(config, envVars));
    }

    /**
     * {@link Callable} providing remote access to test the TEST-GUIDE server availability.
     */
    private static final class TestConnectionCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ATXConfig config;
        private final EnvVars envVars;

        /**
         * Instantiates a new {@link TestConnectionCallable}.
         *
         * @param config
         *            the ATX configuration
         * @param envVars
         *            the environment variables
         */
        TestConnectionCallable(final ATXConfig config, final EnvVars envVars) {
            this.config = config;
            this.envVars = envVars;
        }

        @Override
        public Boolean call() throws IOException {
            final String baseUrl = ATXUtil.getBaseUrl(config, envVars);
            final ATXValidator validator = new ATXValidator();

            boolean ignoreSSL = false;
            final ATXBooleanSetting sslSetting = (ATXBooleanSetting) config.getSettingByName("ignoreSSL");
            if (sslSetting != null) {
                ignoreSSL = sslSetting.getCurrentValue();
            }

            final FormValidation validation = validator.testConnection(baseUrl, ignoreSSL);
            return validation.kind.equals(FormValidation.Kind.OK);
        }
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
     * @param envVars
     *            the environment variables
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
     * DescriptorImpl for {@link ATXPublisher}.
     */
    @SuppressWarnings("rawtypes")
    @Symbol("publishATX")
    @Extension(ordinal = 10007)
    public static class DescriptorImpl extends AbstractReportDescriptor {

        @CopyOnWrite
        private volatile ATXInstallation[] installations = new ATXInstallation[0];

        private final transient ATXConfig defaultConfig;

        /**
         * Validator to check form fields.
         */
        private final transient ATXValidator atxValidator;

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
            load();
            atxValidator = new ATXValidator();
            defaultConfig = new ATXConfig();
        }

        /**
         * @return the list of ATX installations
         */
        public ATXInstallation[] getInstallations() {
            return installations.clone();
        }

        /**
         * Sets the installations.
         *
         * @param installations
         *            the new installations
         */
        public void setInstallations(final ATXInstallation... installations) {
            // Remove empty installations
            final List<ATXInstallation> inst = new ArrayList<ATXInstallation>();
            if (installations != null) {
                Collections.addAll(inst, installations);
                for (final ATXInstallation installation : installations) {
                    if (StringUtils.isBlank(installation.getName())) {
                        inst.remove(installation);
                    }
                }
            }
            this.installations = inst.toArray(new ATXInstallation[inst.size()]);
            save();
        }

        /**
         * @return the default ATX configuration
         */
        public ATXConfig getDefaultConfig() {
            return defaultConfig;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) {
            final List<ATXInstallation> list = new ArrayList<ATXInstallation>();
            final JSONArray instArray = new JSONArray();
            final JSONArray inst = json.optJSONArray("installation");
            if (inst == null) {
                instArray.add(json.getJSONObject("installation"));
            } else {
                instArray.addAll(inst);
            }

            // Parse installations
            for (final Object instObject : instArray) {
                if (instObject instanceof JSONObject) {
                    final JSONObject instJson = (JSONObject) instObject;
                    final String name = instJson.getString("name");
                    final String toolName = instJson.getString("toolName");
                    final Map<String, List<ATXSetting>> configMap = getDefaultConfig().getConfigMap();

                    // Update custom settings
                    List<ATXCustomSetting> customSettings = req.bindJSONToList(ATXCustomSetting.class,
                            instJson.get("customSettings"));

                    // Remove duplicates of default configuration
                    final ListIterator<ATXCustomSetting> iterator = customSettings.listIterator();
                    while (iterator.hasNext()) {
                        if (getDefaultConfig().getSettingByName(iterator.next().getName()) != null) {
                            iterator.remove();
                        }
                    }

                    // Make unique list
                    customSettings = new ArrayList<ATXCustomSetting>(new LinkedHashSet<ATXCustomSetting>(
                            customSettings));

                    // Update current values
                    final ATXConfig config = new ATXConfig(updateCurrentValues(instJson, configMap),
                            customSettings);

                    // Fill installations
                    final ATXInstallation installation = new ATXInstallation(name, toolName, config);
                    list.add(installation);
                }
            }

            setInstallations(list.toArray(new ATXInstallation[list.size()]));
            return true;
        }

        /**
         * Synchronizes current ATX configuration with default configuration
         * by overriding their current values and saving them as new ATX installation.
         *
         * This method will be automatically called by {@link ETPlugin#syncATXConfiguration()} to
         * avoid circular dependencies while loading other plugins.
         */
        @SuppressWarnings("unchecked")
        public void syncWithDefaultConfig() {
            final List<ATXInstallation> list = new ArrayList<ATXInstallation>();
            for (final ATXInstallation installation : installations.clone()) {
                final ATXConfig currentConfig = installation.getConfig();
                final ATXConfig newConfig = defaultConfig.clone();

                // Synchronize settings
                if (currentConfig != null) {
                    for (final Entry<String, List<ATXSetting>> newConfigMap : newConfig.getConfigMap()
                            .entrySet()) {
                        for (final ATXSetting newSetting : newConfigMap.getValue()) {
                            final ATXSetting currentSetting = currentConfig.getSettingByName(newSetting
                                    .getName());
                            if (currentSetting != null) {
                                newSetting.setCurrentValue(currentSetting.getCurrentValue());
                            }
                        }
                    }
                    final List<ATXCustomSetting> customSettings = currentConfig.getCustomSettings();
                    newConfig.setCustomSettings(customSettings == null ?
                            new ArrayList<ATXCustomSetting>() : customSettings);
                }

                // Fill installations
                final ATXInstallation inst = new ATXInstallation(installation.getName(),
                        installation.getToolName(), newConfig);
                list.add(inst);
            }
            setInstallations(list.toArray(new ATXInstallation[list.size()]));
            load(); // Reload from disk
        }

        /**
         * Updates the current values for each ATX setting.
         *
         * @param instJson
         *            the JSONObject representing one installation
         * @param configMap
         *            the default ATX configuration
         * @return the updated ATX configuration
         */
        @SuppressWarnings("unchecked")
        private Map<String, List<ATXSetting>> updateCurrentValues(final JSONObject instJson,
                final Map<String, List<ATXSetting>> configMap) {
            final Map<String, List<ATXSetting>> newConfigMap = new LinkedHashMap<String, List<ATXSetting>>();
            for (final Entry<String, List<ATXSetting>> entry : configMap.entrySet()) {
                final List<ATXSetting> newSettings = new ArrayList<ATXSetting>();
                final List<ATXSetting> defaultSettings = entry.getValue();

                // Deep copy setting list
                for (final ATXSetting defaultSetting : defaultSettings) {
                    newSettings.add(defaultSetting.clone());
                }

                // Update each setting
                final JSONObject configObject = instJson.optJSONObject(entry.getKey());
                if (configObject != null) {
                    for (final ATXSetting newSetting : newSettings) {
                        final Object configSetting = configObject.opt(newSetting.getName());
                        if (configSetting != null) {
                            newSetting.setCurrentValue(configSetting);
                        }
                    }
                }

                // Fill configuration
                newConfigMap.put(entry.getKey(), newSettings);
            }
            return newConfigMap;
        }

        /**
         * Gets the ATX version that this ATX configuration is based on.
         *
         * @return the related ATX version
         */
        public static String getATXVersion() {
            return ETPlugin.ATX_VERSION.toMicroString();
        }

        /**
         * Gets the custom settings of a given ATX installation.
         *
         * @param installation
         *            the installation
         * @return the custom settings list
         */
        public List<ATXCustomSetting> getCustomSettings(final ATXInstallation installation) {
            return installation == null ?
                    new ArrayList<ATXCustomSetting>() : installation.getConfig().getCustomSettings();
        }

        /**
         * Gets the applicable custom settings.
         *
         * @return the applicable custom settings
         */
        public List<Descriptor<? extends ATXCustomSetting>> getApplicableCustomSettings() {
            final List<Descriptor<? extends ATXCustomSetting>> list = new ArrayList<>();
            final DescriptorExtensionList<ATXCustomSetting, Descriptor<ATXCustomSetting>> settings = ATXCustomSetting
                    .all();
            if (settings != null) {
                for (final Descriptor<? extends ATXCustomSetting> setting : settings) {
                    list.add(setting);
                }
            }
            return list;
        }

        @Override
        public String getDisplayName() {
            return Messages.ATXPublisher_DisplayName();
        }

        /**
         * Validates the TEST-GUIDE name which is a required field.
         *
         * @param value
         *            the name
         * @return the form validation
         */
        public FormValidation doCheckName(@QueryParameter final String value) {
            return atxValidator.validateName(value);
        }

        /**
         * Validates the current setting field.
         *
         * @param name
         *            the field name
         * @param value
         *            the field value
         * @return the form validation
         */
        public FormValidation doCheckSetting(@QueryParameter final String name,
                @QueryParameter final String value) {
            return atxValidator.validateSetting(name, value);
        }

        /**
         * Tests the server connection.
         *
         * @param serverURL
         *            the server URL
         * @param serverPort
         *            the server port
         * @param serverContextPath
         *            the server context path
         * @param useHttpsConnection
         *            if secure connection is used
         * @param ignoreSSL
         *            specifies whether to ignore SSL issues
         * @return the form validation
         */
        @RequirePOST
        public FormValidation doTestConnection(@QueryParameter final String serverURL,
                @QueryParameter final String serverPort, @QueryParameter final String serverContextPath,
                @QueryParameter final boolean useHttpsConnection, @QueryParameter final boolean ignoreSSL) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            return atxValidator.testConnection(serverURL, serverPort, serverContextPath, useHttpsConnection, ignoreSSL);
        }
    }
}
