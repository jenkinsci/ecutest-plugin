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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.Callable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomTextSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;

/**
 * Common base class for {@link ATXReportGenerator} and {@link ATXReportUploader}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractATXReportHandler {

    /**
     * Defines the path name containing the ATX reports inside of the test report directory.
     */
    protected static final String ATX_TEMPLATE_NAME = "ATX";

    /**
     * Common {@link Callable} enabling generating and uploading ATX reports remotely.
     */
    protected abstract static class AbstractReportCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ATXConfig config;
        private final List<FilePath> reportFiles;
        private final EnvVars envVars;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link AbstractReportCallable}.
         *
         * @param config
         *            the ATX configuration
         * @param reportFiles
         *            the list of TRF files
         * @param envVars
         *            the environment variables
         * @param listener
         *            the listener
         */
        public AbstractReportCallable(final ATXConfig config, final List<FilePath> reportFiles, final EnvVars envVars,
                final BuildListener listener) {
            this.config = config;
            this.reportFiles = reportFiles;
            this.envVars = envVars;
            this.listener = listener;
        }

        /**
         * @return the reportFiles
         */
        public List<FilePath> getReportFiles() {
            return reportFiles;
        }

        /**
         * @return the listener
         */
        public BuildListener getListener() {
            return listener;
        }

        /**
         * Converts the ATX configuration to a map containing all setting names and their current value.
         * Parameterized values are expanded by given environment variables.
         *
         * @param uploadToServer
         *            specifies whether ATX upload is enabled or not
         * @return the configuration map
         */
        @SuppressWarnings("rawtypes")
        protected Map<String, String> getConfigMap(final boolean uploadToServer) {
            final Map<String, String> configMap = new LinkedHashMap<String, String>();
            for (final List<ATXSetting> settings : config.getConfigMap().values()) {
                for (final ATXSetting setting : settings) {
                    if (setting instanceof ATXBooleanSetting) {
                        if ("uploadToServer".equals(setting.getName())) {
                            configMap.put(setting.getName(), ATXSetting.toString(uploadToServer));
                        } else {
                            configMap.put(setting.getName(),
                                    ATXSetting.toString(((ATXBooleanSetting) setting).getCurrentValue()));
                        }
                    } else {
                        configMap.put(setting.getName(), envVars.expand(((ATXTextSetting) setting).getCurrentValue()));
                    }
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
            return configMap;
        }
    }
}
