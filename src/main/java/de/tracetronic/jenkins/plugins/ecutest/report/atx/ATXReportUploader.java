/**
 * Copyright (c) 2015 TraceTronic GmbH
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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomBooleanSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXCustomTextSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXTextSetting;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;

/**
 * Class providing the generation and upload of {@link ATXReport}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXReportUploader {

    /**
     * Defines the path name containing the ATX reports inside of the test report directory.
     */
    private static final String ATX_TEMPLATE_NAME = "ATX";

    /**
     * Defines the API URL for linking ATX trend reports.
     */
    private static final String ATX_TREND_URL = "wicket/bookmarkable/"
            + "de.tracetronic.ttstm.web.detail.TestReportViewPage?testCase";

    /**
     * File name of the TRF file.
     */
    private static final String TRF_NAME = "report.trf";

    /**
     * Generates and uploads {@link ATXReport}s.
     *
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param installation
     *            the ATX installation
     * @param build
     *            the build
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
    @SuppressWarnings({ "checkstyle:cyclomaticcomplexity", "checkstyle:npathcomplexity" })
    public boolean upload(final boolean allowMissing, final ATXInstallation installation,
            final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
                    throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<ATXReport> atxReports = new ArrayList<ATXReport>();
        final Map<String, Map<String, FilePath>> reportFiles = getReportFiles(build, launcher);
        final List<File> uploadFiles = new ArrayList<File>();

        // Iterate all report files to upload
        for (final Entry<String, Map<String, FilePath>> entry : reportFiles.entrySet()) {
            final String testName = entry.getKey();
            final Map<String, FilePath> reportMap = entry.getValue();
            final Entry<String, FilePath> testType = reportMap.entrySet().iterator().next();
            final FilePath reportFile = testType.getValue();
            if (reportFile.exists()) {
                uploadFiles.add(new File(reportFile.getRemote()));
            } else {
                if (allowMissing) {
                    continue;
                } else {
                    logger.logError(String.format("Specified TRF file '%s' does not exist.", reportFile));
                    return false;
                }
            }

            // Prepare ATX report information
            final String title = String.format("%s/%s", reportFile.getParent().getName(), testName);
            String reportUrl = null;
            String trendReportUrl = null;
            final String atxTestName = ATXUtil.getValidATXName(testName);
            final String from = String.valueOf(build.getStartTimeInMillis());
            final String to = String.valueOf(Calendar.getInstance().getTimeInMillis());
            final String baseUrl = getBaseUrl(installation);
            if (baseUrl == null) {
                logger.logError(String.format(
                        "Error getting base URL for selected TEST-GUIDE installation: %s",
                        installation.getName()));
                return false;
            }
            if (testType.getKey().equals(TestEnvInvisibleAction.TestType.PACKAGE.name())) {
                reportUrl = getPkgReportUrl(baseUrl, atxTestName, from, to);
                trendReportUrl = getPkgTrendReportUrl(baseUrl, atxTestName);
            } else if (testType.getKey().equals(TestEnvInvisibleAction.TestType.PROJECT.name())) {
                reportUrl = getPrjReportUrl(baseUrl, atxTestName, from, to);
            }

            // Add ATX reports
            if (reportUrl != null) {
                atxReports.add(new ATXReport(String.format("%d", atxReports.size() + 1), title, reportUrl));
            }
            if (trendReportUrl != null) {
                atxReports.add(new ATXReport(String.format("%d", atxReports.size() + 1), title,
                        trendReportUrl, true));
            }
        }

        // Upload ATX reports
        final boolean isUploaded = launcher.getChannel().call(
                new UploadReportCallable(installation.getConfig(), uploadFiles, listener));

        // Add action for publishing ATX reports
        if (isUploaded) {
            ATXBuildAction action = build.getAction(ATXBuildAction.class);
            if (action == null) {
                action = new ATXBuildAction();
                build.addAction(action);
            }
            action.addAll(atxReports);
        }

        return isUploaded;
    }

    /**
     * Builds a map containing the test name as key and a test type to test file path map as values.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @return the test map
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private Map<String, Map<String, FilePath>> getReportFiles(final AbstractBuild<?, ?> build,
            final Launcher launcher) throws IOException, InterruptedException {
        final Map<String, Map<String, FilePath>> reportFiles = new LinkedHashMap<String, Map<String, FilePath>>();
        final List<TestEnvInvisibleAction> testEnvActions = build.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final String testName = testEnvAction.getTestName();
            final String testType = testEnvAction.getTestType().name();
            final FilePath reportFilePath = new FilePath(launcher.getChannel(), new File(
                    testEnvAction.getTestReportDir(), TRF_NAME).getPath());
            final Map<String, FilePath> reportFileMap = new LinkedHashMap<String, FilePath>();
            reportFileMap.put(testType, reportFilePath);
            reportFiles.put(testName, reportFileMap);
        }
        return reportFiles;
    }

    /**
     * Gets the base URL of the ATX installation.
     *
     * @param installation
     *            the installation
     * @return the ATX base URL or {@code null} if invalid URL.
     */
    @CheckForNull
    @SuppressWarnings("rawtypes")
    private String getBaseUrl(final ATXInstallation installation) {
        final ATXConfig uploadConfig = installation.getConfig();
        final List<ATXSetting> uploadSettings = uploadConfig.getConfigByName("uploadConfig");
        final Object useHttpsConnection = uploadConfig.getSettingValueByName("useHttpsConnection",
                uploadSettings);
        final String protocol = useHttpsConnection != null && (boolean) useHttpsConnection ? "https" : "http";
        final String serverUrl = (String) uploadConfig.getSettingValueByName("serverURL", uploadSettings);
        final String serverPort = (String) uploadConfig.getSettingValueByName("serverPort", uploadSettings);
        final String contextPath = (String) uploadConfig.getSettingValueByName("serverContextPath",
                uploadSettings);
        if (serverUrl != null && serverPort != null && contextPath != null) {
            return contextPath.isEmpty() ? String.format("%s://%s:%s", protocol, serverUrl, serverPort)
                    : String.format("%s://%s:%s/%s", protocol, serverUrl, serverPort, contextPath);
        }
        return null;
    }

    /**
     * Gets the package trend report URL.
     *
     * @param baseUrl
     *            the base URL
     * @param testName
     *            the test name
     * @return the trend report URL
     */
    private String getPkgTrendReportUrl(final String baseUrl, final String testName) {
        return String.format("%s/%s=%s", baseUrl, ATX_TREND_URL, testName);
    }

    /**
     * Gets the package report URL pre-filtered by a start and end date.
     *
     * @param baseUrl
     *            the base URL
     * @param testName
     *            the test name
     * @param from
     *            the start date
     * @param to
     *            the end date
     * @return the report URL
     */
    private String getPkgReportUrl(final String baseUrl, final String testName, final String from,
            final String to) {
        return String.format("%s/reports?dateFrom=%s&dateTo=%s&testcase=%s", baseUrl, from, to, testName);
    }

    /**
     * Gets the project report URL pre-filtered by a start and end date.
     *
     * @param baseUrl
     *            the base URL
     * @param testName
     *            the test name
     * @param from
     *            the start date
     * @param to
     *            the end date
     * @return the report URL
     */
    private String getPrjReportUrl(final String baseUrl, final String testName, final String from,
            final String to) {
        return String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s", baseUrl, from, to, testName);
    }

    /**
     * {@link Callable} enabling generating and uploading ATX reports remotely.
     */
    private static final class UploadReportCallable implements Callable<Boolean, IOException> {

        private static final long serialVersionUID = 1L;

        /**
         * File name of the error file which is created in case of an ATX upload error.
         */
        private static final String ERROR_FILE_NAME = "error.raw.json";

        private final ATXConfig config;
        private final List<File> uploadFiles;
        private final BuildListener listener;

        /**
         * Instantiates a new {@link UploadReportCallable}.
         *
         * @param config
         *            the ATX configuration
         * @param uploadFiles
         *            the list of TRF files
         * @param listener
         *            the listener
         */
        public UploadReportCallable(final ATXConfig config, final List<File> uploadFiles,
                final BuildListener listener) {
            this.config = config;
            this.uploadFiles = uploadFiles;
            this.listener = listener;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Boolean call() throws IOException {
            boolean isUploaded = true;
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            final Map<String, String> configMap = getConfigMap();
            try (ETComClient comClient = new ETComClient()) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                for (final File uploadFile : uploadFiles) {
                    final List<ATXSetting> uploadSettings = config.getConfigByName("uploadConfig");
                    final Object uploadToServer = config.getSettingValueByName("uploadToServer", uploadSettings);
                    if (uploadToServer != null && (boolean) uploadToServer) {
                        logger.logInfo(String.format("-> Generating and uploading ATX report: %s",
                                uploadFile.getPath()));
                    } else {
                        logger.logInfo(String.format("-> Generating ATX report: %s", uploadFile.getPath()));
                    }
                    final File outDir = new File(uploadFile.getParentFile(), ATX_TEMPLATE_NAME);
                    testEnv.generateTestReportDocumentFromDB(uploadFile.getAbsolutePath(),
                            outDir.getAbsolutePath(), ATX_TEMPLATE_NAME, true, configMap);
                    comClient.waitForIdle(0);

                    // Check error log file and abort the upload if any
                    final File errorFile = new File(outDir, ERROR_FILE_NAME);
                    if (errorFile.exists()) {
                        isUploaded = false;
                        logger.logError("Error uploading ATX report:");
                        final JSONObject jsonObject = (JSONObject) new JsonSlurper().parse(errorFile);
                        final JSONArray jsonArray = jsonObject.optJSONArray("ENTRIES");
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                final String file = jsonArray.getJSONObject(i).getString("FILE");
                                final String status = jsonArray.getJSONObject(i).getString("STATUS");
                                final String text = jsonArray.getJSONObject(i).getString("TEXT");
                                logger.logError(String.format("%s: %s - %s", status, file, text));
                            }
                        }
                        break;
                    }
                }
            } catch (final ETComException e) {
                isUploaded = false;
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isUploaded;
        }

        /**
         * Converts the ATX configuration to a map containing all setting names and their current value.
         *
         * @return the configuration map
         */
        @SuppressWarnings("rawtypes")
        private Map<String, String> getConfigMap() {
            final Map<String, String> configMap = new LinkedHashMap<String, String>();
            for (final List<ATXSetting> settings : config.getConfigMap().values()) {
                for (final ATXSetting setting : settings) {
                    if (setting instanceof ATXBooleanSetting) {
                        configMap.put(setting.getName(),
                                ATXSetting.toString(((ATXBooleanSetting) setting).getCurrentValue()));
                    } else {
                        configMap.put(setting.getName(), ((ATXTextSetting) setting).getCurrentValue());
                    }
                }
            }
            for (final ATXCustomSetting setting : config.getCustomSettings()) {
                if (setting instanceof ATXCustomBooleanSetting) {
                    configMap.put(setting.getName(),
                            ATXSetting.toString(((ATXCustomBooleanSetting) setting).isChecked()));
                } else if (setting instanceof ATXCustomTextSetting) {
                    configMap.put(setting.getName(), ((ATXCustomTextSetting) setting).getValue());
                }
            }
            return configMap;
        }
    }
}
