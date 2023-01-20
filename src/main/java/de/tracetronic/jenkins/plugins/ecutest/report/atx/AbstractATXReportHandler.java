/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomTextSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSecretSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Common base class for {@link ATXReportGenerator} and {@link ATXReportUploader}.
 */
public abstract class AbstractATXReportHandler {

    /**
     * Defines the path name containing the ATX reports inside of the test report directory.
     */
    protected static final String ATX_TEMPLATE_NAME = "ATX";

    private final ATXInstallation installation;

    /**
     * Instantiates a new {@code ATXReportUploader}.
     *
     * @param installation the ATX installation
     */
    public AbstractATXReportHandler(final ATXInstallation installation) {
        this.installation = installation;
    }

    protected ATXInstallation getInstallation() {
        return installation;
    }

    /**
     * Common {@link Callable} enabling generating and uploading ATX reports remotely.
     *
     * @param <T> the generic {@code Callable} return type
     */
    protected abstract static class AbstractReportCallable<T> extends MasterToSlaveCallable<T, IOException> {

        private static final long serialVersionUID = 1L;

        protected final ATXConfig config;
        private final List<FilePath> reportFiles;
        private final EnvVars envVars;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link AbstractReportCallable}.
         *
         * @param config      the ATX configuration
         * @param reportFiles the list of TRF files
         * @param envVars     the environment variables
         * @param listener    the listener
         */
        public AbstractReportCallable(final ATXConfig config, final List<FilePath> reportFiles, final EnvVars envVars,
                                      final TaskListener listener) {
            this.config = config;
            this.reportFiles = reportFiles;
            this.envVars = envVars;
            this.listener = listener;
        }

        public List<FilePath> getReportFiles() {
            return reportFiles;
        }

        public TaskListener getListener() {
            return listener;
        }

        /**
         * Converts the ATX configuration to a map containing all setting names and their current value. Parameterized
         * values are expanded by given environment variables.
         *
         * @param uploadToServer  specifies whether ATX upload is enabled or not
         * @param injectBuildVars specifies whether to inject common build variables as ATX constants
         * @return the configuration map
         */
        protected Map<String, String> getConfigMap(final boolean uploadToServer, final boolean injectBuildVars) {
            final Map<String, String> configMap = new LinkedHashMap<>();
            for (final ATXSetting<?> setting : config.getSettings()) {
                if (setting instanceof ATXBooleanSetting) {
                    if ("uploadToServer".equals(setting.getName())) {
                        configMap.put(setting.getName(), ATXSetting.toString(uploadToServer));
                    } else {
                        configMap.put(setting.getName(),
                            ATXSetting.toString(((ATXBooleanSetting) setting).getValue()));
                    }
                } else if (setting instanceof ATXSecretSetting) {
                    configMap.put(setting.getName(), envVars.expand(((ATXSecretSetting) setting).getSecretValue()));
                } else {
                    configMap.put(setting.getName(), envVars.expand(((ATXTextSetting) setting).getValue()));
                }
            }
            for (final ATXCustomSetting setting : config.getCustomSettings()) {
                if (setting instanceof ATXCustomBooleanSetting) {
                    configMap.put(setting.getName(),
                        ATXSetting.toString(((ATXCustomBooleanSetting) setting).isChecked()));
                } else if (setting instanceof ATXCustomTextSetting) {
                    configMap.put(setting.getName(), envVars.expand(((ATXCustomTextSetting) setting).getValue()));
                }
            }
            if (injectBuildVars) {
                final List<String> constants = new ArrayList<>(Arrays.asList(
                    formatConstant("BUILD_NUMBER"),
                    formatConstant("BUILD_URL"),
                    formatConstant("JOB_NAME")));
                final String customConstants = configMap.get("setConstants");

                if (!customConstants.isEmpty()) {
                    constants.add(customConstants);
                }
                configMap.replace("setConstants", String.join(";", constants));
            }
            return configMap;
        }

        /**
         * Formats the ATX constant to be added as key-value pair (TT_JENKINS_&lt;VAR&gt;=&lt;VAR&gt;).
         *
         * @param variableName the variable name
         * @return the formatted ATX constant
         */
        private String formatConstant(final String variableName) {
            return String.format("%s=%s", "TT_JENKINS_" + variableName, envVars.get(variableName));
        }
    }
}
