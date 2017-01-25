/**
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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.remoting.Callable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction;
import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction.TestType;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;

/**
 * Class providing the generation and upload of {@link ATXReport}s.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXReportUploader extends AbstractATXReportHandler {

    /**
     * Defines the API URL for linking ATX trend reports.
     */
    private static final String ATX_TREND_URL = "wicket/bookmarkable/"
            + "de.tracetronic.ttstm.web.detail.TestReportViewPage?testCase";

    /**
     * Instantiates a new {@code ATXReportUploader}.
     *
     * @param installation
     *            the ATX installation
     */
    public ATXReportUploader(final ATXInstallation installation) {
        super(installation);
    }

    /**
     * Generates and uploads {@link ATXReport}s.
     *
     * @param allowMissing
     *            specifies whether missing reports are allowed
     * @param run
     *            the run
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
    public boolean upload(final boolean allowMissing, final Run<?, ?> run, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<ATXReport> atxReports = new ArrayList<ATXReport>();
        final List<FilePath> uploadFiles = new ArrayList<FilePath>();

        // Prepare ATX report information
        final String baseUrl = ATXUtil.getBaseUrl(getInstallation().getConfig(), run.getEnvironment(listener));
        if (baseUrl == null) {
            logger.logError(String.format("Error getting base URL for selected TEST-GUIDE installation: %s",
                    getInstallation().getName()));
            return false;
        }

        int index = 0;
        final List<TestEnvInvisibleAction> testEnvActions = run.getActions(TestEnvInvisibleAction.class);
        for (final TestEnvInvisibleAction testEnvAction : testEnvActions) {
            final FilePath testReportDir = new FilePath(launcher.getChannel(), testEnvAction.getTestReportDir());
            final FilePath reportFile = AbstractReportPublisher.getFirstReportFile(testReportDir);
            if (reportFile != null && reportFile.exists()) {
                uploadFiles.addAll(Arrays.asList(
                        testReportDir.list(TRFPublisher.TRF_INCLUDES, TRFPublisher.TRF_EXCLUDES)));
                // Prepare ATX report links
                final String from = String.valueOf(run.getTimeInMillis());
                final String to = from;
                final String title = reportFile.getParent().getName();
                final String testName = testEnvAction.getTestName();
                final TestType testType = testEnvAction.getTestType();
                index = traverseReports(atxReports, testReportDir, index, title, baseUrl, from, to, testName, testType);
            } else {
                if (allowMissing) {
                    continue;
                } else {
                    logger.logError(String.format("Specified TRF file '%s' does not exist.", reportFile));
                    return false;
                }
            }
        }

        if (atxReports.isEmpty() && !allowMissing) {
            logger.logError("Empty test results are not allowed, setting build status to FAILURE!");
            return false;
        }

        // Upload ATX reports
        final boolean isUploaded = launcher.getChannel().call(
                new UploadReportCallable(getInstallation().getConfig(), uploadFiles, run.getEnvironment(listener),
                        listener));
        if (isUploaded) {
            // FIXME: hackish way to fix the upload end date
            final String to = String.valueOf(Calendar.getInstance().getTimeInMillis());
            updateReportUrls(atxReports, to);
            addBuildAction(run, atxReports);
        }

        return isUploaded;
    }

    /**
     * Updates the end date of all report URLs.
     *
     * @param reports
     *            the ATX reports
     * @param to
     *            the new end date
     */
    private void updateReportUrls(final List<? extends AbstractTestReport> reports, final String to) {
        for (final AbstractTestReport report : reports) {
            if (report instanceof ATXReport) {
                final ATXReport atxReport = (ATXReport) report;
                final String reportUrl = updateEndDate(atxReport.getReportUrl(), to);
                atxReport.setReportUrl(reportUrl);
                updateReportUrls(atxReport.getSubReports(), to);
            }
        }
    }

    /**
     * Replaces the end date of the report URL.
     *
     * @param reportUrl
     *            the report URL
     * @param to
     *            the new end date
     * @return the replaced report URL
     */
    private String updateEndDate(final String reportUrl, final String to) {
        return reportUrl.replaceFirst("dateTo=\\d+", "dateTo=" + to);
    }

    /**
     * Creates the main report and adds the sub-reports by traversing them recursively.
     *
     * @param atxReports
     *            the ATX reports
     * @param testReportDir
     *            the test report directory
     * @param id
     *            the report id
     * @param title
     *            the report title
     * @param baseUrl
     *            the base URL
     * @param from
     *            the from date
     * @param to
     *            the to date
     * @param testName
     *            the test name
     * @param testType
     *            the test type
     * @return the current report id
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private int traverseReports(final List<ATXReport> atxReports, final FilePath testReportDir, int id,
            final String title, final String baseUrl, final String from, final String to, final String testName,
            final TestType testType) throws IOException, InterruptedException {
        // Prepare ATX report information
        String reportUrl = null;
        String trendReportUrl = null;
        final String atxTestName = ATXUtil.getValidATXName(testName);
        if (testType == TestType.PACKAGE) {
            reportUrl = getPkgReportUrl(baseUrl, from, to, atxTestName);
            trendReportUrl = getPkgTrendReportUrl(baseUrl, atxTestName);
        } else {
            reportUrl = getPrjReportUrl(baseUrl, from, to, atxTestName, null);
        }

        final ATXReport atxReport = new ATXReport(String.format("%d", ++id), title, reportUrl);
        if (trendReportUrl != null) {
            atxReport.addSubReport(new ATXReport(String.format("%d", ++id), title, trendReportUrl, true));
        }
        atxReports.add(atxReport);

        // Search for sub-reports
        final boolean isSingleTestplanMap = ATXUtil.isSingleTestplanMap(getInstallation().getConfig());
        if (isSingleTestplanMap) {
            id = traverseSubReports(atxReport, testReportDir, id, baseUrl, from, to, null);
        } else {
            id = traverseSubReports(atxReport, testReportDir, id, baseUrl, from, to, atxTestName);
        }
        return id;
    }

    /**
     * Builds a list of report files for ATX report generation and upload.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param atxReport
     *            the ATX report
     * @param testReportDir
     *            the main test report directory
     * @param id
     *            the id increment
     * @param baseUrl
     *            the base URL
     * @param from
     *            the from date
     * @param to
     *            the to date
     * @param projectName
     *            the main project name, can be {@code null}
     * @return the current id increment
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private int traverseSubReports(final ATXReport atxReport, final FilePath testReportDir, int id,
            final String baseUrl, final String from, final String to, final String projectName) throws IOException,
            InterruptedException {
        for (final FilePath subDir : testReportDir.listDirectories()) {
            final FilePath reportFile = AbstractReportPublisher.getFirstReportFile(subDir);
            if (reportFile != null && reportFile.exists()) {
                // Prepare ATX report information for sub-report
                final String testName = reportFile.getParent().getName().replaceFirst("^Report\\s", "");
                final String atxTestName = ATXUtil.getValidATXName(testName);
                final String reportUrl = getPrjReportUrl(baseUrl, from, to, atxTestName, projectName);
                final ATXReport subReport = new ATXReport(String.format("%d", ++id), testName, reportUrl);

                atxReport.addSubReport(subReport);
                id = traverseSubReports(subReport, subDir, id, baseUrl, from, to, projectName);
            }
        }
        return id;
    }

    /**
     * Adds the {@link ATXBuildAction} to the build holding the found {@link ATXReport}s.
     *
     * @param run
     *            the run
     * @param atxReports
     *            the list of {@link ATXReport}s to add
     */
    @SuppressWarnings("unchecked")
    private void addBuildAction(final Run<?, ?> run, final List<ATXReport> atxReports) {
        ATXBuildAction<ATXReport> action = run.getAction(ATXBuildAction.class);
        if (action == null) {
            action = new ATXBuildAction<ATXReport>(false);
            run.addAction(action);
        }
        action.addAll(atxReports);
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
     * @param from
     *            the start date
     * @param to
     *            the end date
     * @param testName
     *            the test name
     * @return the report URL
     */
    private String getPkgReportUrl(final String baseUrl, final String from, final String to, final String testName) {
        return String.format("%s/reports?dateFrom=%s&dateTo=%s&testcase=%s", baseUrl, from, to, testName);
    }

    /**
     * Gets the project report URL pre-filtered by a start and end date.
     *
     * @param baseUrl
     *            the base URL
     * @param from
     *            the start date
     * @param to
     *            the end date
     * @param testName
     *            the test name
     * @param projectName
     *            the main project name
     * @return the report URL
     */
    private String getPrjReportUrl(final String baseUrl, final String from, final String to, final String testName,
            final String projectName) {
        String prjReportUrl;
        if (projectName != null) {
            prjReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s&plannedTestCaseFolder=%s*",
                    baseUrl, from, to, projectName, testName);
        } else {
            prjReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s",
                    baseUrl, from, to, testName);
        }

        return prjReportUrl;
    }

    /**
     * {@link Callable} enabling generating and uploading ATX reports remotely.
     */
    private static final class UploadReportCallable extends AbstractReportCallable {

        private static final long serialVersionUID = 1L;

        /**
         * File name of the error file which is created in case of an ATX upload error.
         */
        private static final String ERROR_FILE_NAME = "error.log.raw.json";

        /**
         * Instantiates a new {@link UploadReportCallable}.
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
        UploadReportCallable(final ATXConfig config, final List<FilePath> reportFiles, final EnvVars envVars,
                final TaskListener listener) {
            super(config, reportFiles, envVars, listener);
        }

        @Override
        public Boolean call() throws IOException {
            boolean isUploaded = true;
            final TTConsoleLogger logger = new TTConsoleLogger(getListener());
            final Map<String, String> configMap = getConfigMap(true);
            final String progId = ETComProgId.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                final List<FilePath> uploadFiles = getReportFiles();
                if (uploadFiles.isEmpty()) {
                    logger.logInfo("-> No report files found to upload!");
                } else {
                    for (final FilePath uploadFile : uploadFiles) {
                        logger.logInfo(String.format("-> Generating and uploading ATX report: %s",
                                uploadFile.getRemote()));
                        final FilePath outDir = uploadFile.getParent().child(ATX_TEMPLATE_NAME);
                        testEnv.generateTestReportDocumentFromDB(uploadFile.getRemote(),
                                outDir.getRemote(), ATX_TEMPLATE_NAME, true, configMap);
                        comClient.waitForIdle(0);

                        // Check error log file and abort the upload if any
                        final File errorFile = new File(outDir.getRemote(), ERROR_FILE_NAME);
                        if (errorFile.exists()) {
                            isUploaded = false;
                            logger.logError("Error while uploading ATX report:");
                            try {
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
                            } catch (final JSONException e) {
                                logger.logError("-> Could not parse JSON response: " + e.getMessage());
                            }
                            break; // TODO: no break going on
                        }
                    }
                }
            } catch (final ETComException e) {
                isUploaded = false;
                logger.logError("Caught ComException: " + e.getMessage());
            }
            return isUploaded;
        }
    }
}
