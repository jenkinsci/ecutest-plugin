/*
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
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jenkins.security.MasterToSlaveCallable;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;

/**
 * Class providing the report generation with a specific generator.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportGenerator {

    private final ReportGeneratorConfig config;

    /**
     * Instantiates a new {@link ReportGenerator}.
     *
     * @param config
     *            the configuration
     */
    public ReportGenerator(final ReportGeneratorConfig config) {
        super();
        this.config = config;
    }

    /**
     * @return the configuration
     */
    public ReportGeneratorConfig getConfig() {
        return config;
    }

    /**
     * Generate reports by calling the {@link GenerateReportCallable}.
     *
     * @param reportFiles
     *            the report files
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if generation succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    public boolean generate(final List<FilePath> reportFiles, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        return launcher.getChannel().call(new GenerateReportCallable(config, reportFiles, listener));
    }

    /**
     * {@link Callable} enabling generation of reports with specific generator remotely.
     */
    private static final class GenerateReportCallable extends MasterToSlaveCallable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        private final ReportGeneratorConfig config;
        private final List<FilePath> dbFiles;
        private final TaskListener listener;

        /**
         * Instantiates a new {@link GenerateUnitReportCallable}.
         *
         * @param config
         *            the template name
         * @param dbFiles
         *            the list of TRF files
         * @param listener
         *            the listener
         */
        GenerateReportCallable(final ReportGeneratorConfig config, final List<FilePath> dbFiles,
                final TaskListener listener) {
            this.config = config;
            this.dbFiles = dbFiles;
            this.listener = listener;
        }

        @Override
        public Boolean call() throws IOException {
            boolean isGenerated = true;
            final String templateName = config.getName();
            final Map<String, String> configMap = getConfigMap();
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                logger.logInfo(String.format("- Generating %s test reports...", templateName));
                for (final FilePath dbFile : dbFiles) {
                    logger.logInfo(String.format("-> Generating %s report: %s", templateName, dbFile.getRemote()));
                    final File outDir = new File(dbFile.getParent().getRemote(), templateName);
                    if (!testEnv.generateTestReportDocumentFromDB(dbFile.getRemote(),
                            outDir.getAbsolutePath(), templateName, true, configMap)) {
                        isGenerated = false;
                        logger.logError(String.format("Generating %s report failed!", templateName));
                    }
                }
            } catch (final ETComException e) {
                isGenerated = false;
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isGenerated;
        }

        /**
         * Gets the configuration map.
         *
         * @return the configuration map
         */
        private Map<String, String> getConfigMap() {
            final Map<String, String> configMap = new LinkedHashMap<String, String>();
            for (final ReportGeneratorSetting setting : config.getSettings()) {
                configMap.put(setting.getName(), setting.getValue());
            }
            return configMap;
        }
    }
}
