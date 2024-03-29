/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.generator;

import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class providing the report generation with a specific generator.
 */
public class ReportGenerator {

    private final ReportGeneratorConfig config;

    /**
     * Instantiates a new {@link ReportGenerator}.
     *
     * @param config the configuration
     */
    public ReportGenerator(final ReportGeneratorConfig config) {
        super();
        this.config = config;
    }

    public ReportGeneratorConfig getConfig() {
        return config;
    }

    /**
     * Generate reports by calling the {@link ReportGenerator.GenerateReportCallable}.
     *
     * @param reportFiles the report files
     * @param launcher    the launcher
     * @param listener    the listener
     * @return {@code true} if generation succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
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
         * Instantiates a new {@link GenerateReportCallable}.
         *
         * @param config   the template name
         * @param dbFiles  the list of TRF files
         * @param listener the listener
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

            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                logger.logInfo(String.format("- Generating %s test reports...", templateName));
                for (final FilePath dbFile : dbFiles) {
                    logger.logInfo(String.format("-> Generating %s report: %s", templateName, dbFile.getRemote()));
                    if (!generateReport(testEnv, dbFile, templateName)) {
                        isGenerated = false;
                        logger.logError(String.format("Generating %s report failed!", templateName));
                    }
                }
            } catch (final ETComException e) {
                isGenerated = false;
                logger.logComException(e);
            }
            return isGenerated;
        }

        /**
         * Gets the configuration map.
         *
         * @return the configuration map
         */
        private Map<String, String> getConfigMap() {
            final Map<String, String> configMap = new LinkedHashMap<>();
            for (final ReportGeneratorSetting setting : config.getSettings()) {
                configMap.put(setting.getName(), setting.getValue());
            }
            return configMap;
        }

        /**
         * Generates a test report from either predefined template or persisted settings file.
         *
         * @param testEnv      the COM test environment
         * @param dbFile       the path to report file
         * @param templateName the template name
         * @return the configuration map
         */
        private boolean generateReport(final TestEnvironment testEnv, final FilePath dbFile, final String templateName)
                throws ETComException {
            if (config.isUsePersistedSettings()) {
                final FilePath reportDir = dbFile.getParent();
                final FilePath configPath = reportDir.child(templateName + ".xml");
                final TTConsoleLogger logger = new TTConsoleLogger(listener);
                logger.logInfo(String.format("- Using persisted settings from configuration: %s",
                        configPath.getRemote()));
                return testEnv.generateTestReportDocument(
                        dbFile.getRemote(), reportDir.getRemote(), configPath.getRemote(), true);
            } else {
                final FilePath outDir = dbFile.getParent().child(templateName);
                final Map<String, String> configMap = getConfigMap();
                return testEnv.generateTestReportDocumentFromDB(
                        dbFile.getRemote(), outDir.getRemote(), templateName, true, configMap);
            }
        }
    }
}
