/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import de.tracetronic.jenkins.plugins.ecutest.env.TestEnvInvisibleAction.TestType;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXInstallation;
import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting;
import de.tracetronic.jenkins.plugins.ecutest.report.trf.TRFPublisher;
import de.tracetronic.jenkins.plugins.ecutest.util.ATXUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ATXValidator;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComClient;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComException;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.TestEnvironment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class providing the generation and upload of {@link ATXReport}s.
 */
public class ATXReportUploader extends AbstractATXReportHandler {

    /**
     * Defines the API URL for linking ATX trend reports.
     */
    private static final String ATX_TREND_URL = "wicket/bookmarkable/"
        + "de.tracetronic.ttstm.web.detail.TestCaseDetailPage?testCase";

    /**
     * Instantiates a new {@code ATXReportUploader}.
     *
     * @param installation the ATX installation
     */
    public ATXReportUploader(final ATXInstallation installation) {
        super(installation);
    }

    /**
     * Generates and uploads {@link ATXReport}s.
     *
     * @param reportDirs           the report directories
     * @param usePersistedSettings specifies whether to use report generator settings from persisted configurations
     *                             file
     * @param injectBuildVars      specifies whether to inject common build variables as ATX constants
     * @param allowMissing         specifies whether missing reports are allowed
     * @param run                  the run
     * @param launcher             the launcher
     * @param listener             the listener
     * @return {@code true} if upload succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    public boolean upload(final List<FilePath> reportDirs, final boolean usePersistedSettings,
                          final boolean injectBuildVars, final boolean allowMissing,
                          final Run<?, ?> run, final Launcher launcher, final TaskListener listener)
            throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        final List<ATXReport> atxReports = new ArrayList<>();

        // Prepare ATX report information
        final EnvVars envVars = run.getEnvironment(listener);
        final ATXConfig config = getInstallation().getConfig();
        final String projectId = ATXUtil.getProjectId(config, envVars);
        final String baseUrl = ATXUtil.getBaseUrl(config, envVars);
        if (baseUrl == null) {
            logger.logError(String.format("Error getting base URL for selected test.guide installation: %s",
                getInstallation().getName()));
            return false;
        }

        checkForWarning(config, logger);

        for (final FilePath reportDir : reportDirs) {
            final FilePath reportFile = AbstractReportPublisher.getFirstReportFile(reportDir);
            if (reportFile != null && reportFile.exists()) {
                final List<FilePath> uploadFiles = Arrays.asList(
                    reportDir.list(TRFPublisher.TRF_INCLUDES, TRFPublisher.TRF_EXCLUDES));

                // Upload ATX reports
                final UploadInfoHolder uploadInfo = launcher.getChannel().call(
                    new UploadReportCallable(config, uploadFiles, usePersistedSettings, injectBuildVars,
                            envVars, listener));

                if (uploadInfo.isUploaded()) {
                    // Prepare ATX report links
                    final String title = reportFile.getParent().getName();
                    if (uploadInfo.getTestInfo() == null) {
                        uploadInfo.setTestInfo(launcher.getChannel().call(
                            new ParseTRFCallable(reportFile.getRemote())));
                    }
                    traverseReports(atxReports, reportDir, title, baseUrl, uploadInfo.getTestInfo(), projectId);
                }
            } else {
                if (!allowMissing) {
                    logger.logError(String.format("Specified TRF file '%s' does not exist.", reportFile));
                    return false;
                }
            }
        }

        if (atxReports.isEmpty() && !allowMissing) {
            logger.logError("Empty test results are not allowed, setting build status to FAILURE!");
            return false;
        }

        addBuildAction(run, atxReports);
        return true;
    }

    /**
     * Checks whether to log warning when settings <i>cleanAfterSuccessUpload</i> is enabled.
     *
     * @param config the ATX configuration
     * @param logger the logger
     */
    private void checkForWarning(final ATXConfig config, final TTConsoleLogger logger) {
        if ((boolean) config.getSettingValueByName("cleanAfterSuccessUpload")) {
            logger.logWarn("-> In order to generate ATX report links with unique ATX identifiers "
                    + "disable the upload setting 'Clean After Success Upload' in the test.guide configuration.");
        }
    }

    /**
     * Creates the main report and adds the sub-reports by traversing them recursively.
     *
     * @param atxReports    the ATX reports
     * @param testReportDir the test report directory
     * @param title         the report title
     * @param baseUrl       the base URL
     * @param testInfo      the test info
     * @param projectId     the project id
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private void traverseReports(final List<ATXReport> atxReports, final FilePath testReportDir,
                                 final String title, final String baseUrl, final TestInfoHolder testInfo,
                                 final String projectId) throws IOException, InterruptedException {
        // Prepare ATX report information
        final String reportUrl;
        String trendReportUrl = null;
        final TestType testType = testInfo.getTestType();
        if (testType == TestType.PACKAGE) {
            reportUrl = getPkgReportUrl(baseUrl, testInfo, projectId);
            trendReportUrl = getPkgTrendReportUrl(baseUrl, testInfo, projectId);
        } else {
            reportUrl = getPrjReportUrl(baseUrl, testInfo, null, projectId);
        }

        final ATXReport atxReport = new ATXReport(AbstractReportPublisher.randomId(), title, reportUrl);
        if (trendReportUrl != null) {
            atxReport.addSubReport(new ATXReport(AbstractReportPublisher.randomId(), title, trendReportUrl, true));
        }
        atxReports.add(atxReport);

        // Search for sub-reports
        final boolean isSingleTestplanMap = ATXUtil.isSingleTestplanMap(getInstallation().getConfig());
        if (isSingleTestplanMap) {
            traverseSubReports(atxReport, testReportDir, baseUrl, testInfo, null, projectId);
        } else {
            traverseSubReports(atxReport, testReportDir, baseUrl, testInfo, testInfo.getTestName(), projectId);
        }
    }

    /**
     * Builds a list of report files for ATX report generation and upload.
     * Includes the report files generated during separate sub-project execution.
     *
     * @param atxReport     the ATX report
     * @param testReportDir the main test report directory
     * @param baseUrl       the base URL
     * @param testInfo      the test info
     * @param projectName   the main project name, can be {@code null}
     * @param projectId     the project id
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private void traverseSubReports(final ATXReport atxReport, final FilePath testReportDir,
                                    final String baseUrl, final TestInfoHolder testInfo, final String projectName,
                                    final String projectId) throws IOException, InterruptedException {
        for (final FilePath subDir : testReportDir.listDirectories()) {
            final FilePath reportFile = AbstractReportPublisher.getFirstReportFile(subDir);
            if (reportFile != null && reportFile.exists()) {
                final String testName;
                // Ensure compatibility with ecu.test 6.x using report.trf as report name
                if ("report.trf".equals(reportFile.getName())) {
                    testName = reportFile.getParent().getName().replaceFirst("^Report\\s", "");
                } else {
                    testName = reportFile.getBaseName();
                }
                final String subTestName = ATXUtil.getValidATXName(testName);
                final String reportUrl = getPrjSubReportUrl(baseUrl, testInfo, subTestName, projectName, projectId);
                final ATXReport subReport = new ATXReport(AbstractReportPublisher.randomId(), testName, reportUrl);
                atxReport.addSubReport(subReport);
                traverseSubReports(subReport, subDir, baseUrl, testInfo, projectName, projectId);
            }
        }
    }

    /**
     * Adds the {@link ATXBuildAction} to the build holding the found {@link ATXReport}s.
     *
     * @param run        the run
     * @param atxReports the list of {@link ATXReport}s to add
     */
    @SuppressWarnings("unchecked")
    private void addBuildAction(final Run<?, ?> run, final List<ATXReport> atxReports) {
        ATXBuildAction<ATXReport> action = run.getAction(ATXBuildAction.class);
        if (action == null) {
            action = new ATXBuildAction<>(false);
            run.addAction(action);
        }
        action.addAll(atxReports);
    }

    /**
     * Gets the package trend report URL.
     *
     * @param baseUrl   the base URL
     * @param testInfo  the test info
     * @param projectId the project id
     * @return the trend report URL
     */
    private String getPkgTrendReportUrl(final String baseUrl, final TestInfoHolder testInfo, final String projectId) {
        final String testName = ATXUtil.getValidATXName(testInfo.getTestName());
        String pkgTrendReportUrl = String.format("%s/%s=%s", baseUrl, ATX_TREND_URL, testName);
        if (projectId != null) {
            pkgTrendReportUrl = String.format("%s&projectId=%s", pkgTrendReportUrl, projectId);
        }
        return pkgTrendReportUrl;
    }

    /**
     * Gets the package report URL pre-filtered by a start and end date.
     *
     * @param baseUrl   the base URL
     * @param testInfo  the test info
     * @param projectId the project id
     * @return the report URL
     */
    private String getPkgReportUrl(final String baseUrl, final TestInfoHolder testInfo, final String projectId) {
        if (testInfo.getLink() != null) {
            return testInfo.getLink();
        }

        final String testName = testInfo.getTestName();
        final String from = String.valueOf(testInfo.getFrom());
        final String to = String.valueOf(testInfo.getTo());
        final String atxTestName = ATXUtil.getValidATXName(testName);
        String pkgReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testcase=%s",
                baseUrl, from, to, atxTestName);
        if (projectId != null) {
            pkgReportUrl = String.format("%s&projectId=%s", pkgReportUrl, projectId);
        }
        return pkgReportUrl;
    }

    /**
     * Gets the project report URL pre-filtered by a start and end date.
     *
     * @param baseUrl     the base URL
     * @param testInfo    the test info
     * @param projectName the main project name
     * @param projectId   the project id
     * @return the report URL
     */
    private String getPrjReportUrl(final String baseUrl, final TestInfoHolder testInfo,
                                   final String projectName, final String projectId) {
        if (testInfo.getLink() != null) {
            return testInfo.getLink();
        }

        final String testName = ATXUtil.getValidATXName(testInfo.getTestName());
        final String from = String.valueOf(testInfo.getFrom());
        final String to = String.valueOf(testInfo.getTo());
        String prjReportUrl;
        if (projectName != null) {
            prjReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s&plannedTestCaseFolder=%s*",
                baseUrl, from, to, projectName, testName);
        } else {
            prjReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s",
                baseUrl, from, to, testName);
        }
        if (projectId != null) {
            prjReportUrl = String.format("%s&projectId=%s", prjReportUrl, projectId);
        }

        return prjReportUrl;
    }

    /**
     * Gets the project report URL pre-filtered by a start and end date.
     *
     * @param baseUrl     the base URL
     * @param testInfo    the test info
     * @param subTestName the sub test name
     * @param projectName the main project name
     * @param projectId   the project id
     * @return the report URL
     */
    private String getPrjSubReportUrl(final String baseUrl, final TestInfoHolder testInfo,
                                      final String subTestName, final String projectName, final String projectId) {
        final String from = String.valueOf(testInfo.getFrom());
        final String to = String.valueOf(testInfo.getTo());
        String prjReportUrl;
        if (projectName != null) {
            prjReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s&plannedTestCaseFolder=%s*",
                baseUrl, from, to, ATXUtil.getValidATXName(projectName), subTestName);
        } else {
            prjReportUrl = String.format("%s/reports?dateFrom=%s&dateTo=%s&testexecplan=%s",
                baseUrl, from, to, subTestName);
        }
        if (projectId != null) {
            prjReportUrl = String.format("%s&projectId=%s", prjReportUrl, projectId);
        }

        return prjReportUrl;
    }

    /**
     * {@link Callable} enabling generating and uploading ATX reports remotely.
     */
    private static final class UploadReportCallable extends AbstractReportCallable<UploadInfoHolder> {

        private static final long serialVersionUID = 1L;

        /**
         * File name of the error file which is created in case of an ATX upload error.
         */
        private static final String ERROR_FILE_NAME = "error.raw.json";

        /**
         * File name of the success file which is created in case of a regular ATX upload.
         * Will only be written by test.guide 1.53.0 and above.
         */
        private static final String SUCCESS_FILE_NAME = "success.json";

        private final boolean usePersistedSettings;
        private final boolean injectBuildVars;

        /**
         * Instantiates a new {@link UploadReportCallable}.
         *
         * @param config               the ATX configuration
         * @param reportFiles          the list of TRF files
         * @param usePersistedSettings specifies whether to use read settings from persisted configurations file
         * @param injectBuildVars      specifies whether to inject common build variables as ATX constants
         * @param envVars              the environment variables
         * @param listener             the listener
         */
        UploadReportCallable(final ATXConfig config, final List<FilePath> reportFiles,
                             final boolean usePersistedSettings, final boolean injectBuildVars,
                             final EnvVars envVars, final TaskListener listener) {
            super(config, reportFiles, envVars, listener);
            this.usePersistedSettings = usePersistedSettings;
            this.injectBuildVars = injectBuildVars;
        }

        @Override
        public UploadInfoHolder call() throws IOException {
            final UploadInfoHolder uploadInfo = new UploadInfoHolder(false);
            final TTConsoleLogger logger = new TTConsoleLogger(getListener());
            final Map<String, String> configMap = getConfigMap(true, injectBuildVars);
            final String progId = ETComProperty.getInstance().getProgId();
            try (ETComClient comClient = new ETComClient(progId)) {
                final TestEnvironment testEnv = (TestEnvironment) comClient.getTestEnvironment();
                final List<FilePath> reportFiles = getReportFiles();
                if (reportFiles.isEmpty()) {
                    logger.logInfo("-> No report files found to upload!");
                } else {
                    for (final FilePath reportFile : reportFiles) {
                        logger.logInfo(String.format("-> Generating and uploading ATX report: %s",
                            reportFile.getRemote()));
                        final FilePath outDir = reportFile.getParent().child(ATX_TEMPLATE_NAME);
                        if (usePersistedSettings) {
                            final FilePath reportDir = reportFile.getParent();
                            final FilePath configPath = reportDir.child(ATX_TEMPLATE_NAME + ".xml");
                            logger.logInfo(String.format("- Using persisted settings from configuration: %s",
                                configPath.getRemote()));
                            testEnv.generateTestReportDocument(reportFile.getRemote(),
                                reportDir.getRemote(), configPath.getRemote(), true);
                        } else {
                            testEnv.generateTestReportDocumentFromDB(reportFile.getRemote(),
                                outDir.getRemote(), ATX_TEMPLATE_NAME, true, configMap);
                        }
                        comClient.waitForIdle(0);

                        final FilePath errorFile = outDir.child(ERROR_FILE_NAME);
                        if (checkErrorLog(errorFile, logger)) {
                            final FilePath successFile = outDir.child(SUCCESS_FILE_NAME);
                            final boolean uploadAsync = configMap.get("uploadAsync").equals("True");
                            uploadInfo.setTestInfo(checkSuccessLog(successFile, reportFile, uploadAsync, logger));
                            uploadInfo.setUploaded(true);
                        }
                    }
                }
            } catch (final ETComException e) {
                logger.logComException(e);
            }
            return uploadInfo;
        }

        /**
         * Checks the success log file and parse upload information.
         * The success log file will only be written by test.guide 1.53.0 and above.
         *
         * @param successFile the success file
         * @param uploadFile  the upload file
         * @param uploadAsync specifies whether asynchronous upload is enabled
         * @param logger      the logger
         * @return the parsed test information
         * @throws IOException                  signals that an I/O exception has occurred
         * @throws MalformedURLException        in case of a malformed URL
         * @throws UnsupportedEncodingException in case of an unsupported encoding
         */
        private TestInfoHolder checkSuccessLog(final FilePath successFile, final FilePath uploadFile,
                                               final boolean uploadAsync, final TTConsoleLogger logger)
                throws IOException {
            TestInfoHolder testInfo = null;
            try {
                if (successFile.exists()) {
                    logger.logDebug("ATX report uploaded successfully.");
                    final String content = successFile.readToString();
                    logger.logDebug(String.format("Response: %s", content));

                    final JSONObject jsonObject = (JSONObject) new JsonSlurper().parseText(content);
                    final JSONArray jsonArray = jsonObject.optJSONArray(uploadAsync ? "messages" : "ENTRIES");
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            final String status = jsonArray.getJSONObject(i).getString(
                                uploadAsync ? "statusCode" : "STATUS");
                            if ("200".equals(status)) {
                                final String text = jsonArray.getJSONObject(i).getString(uploadAsync ? "body" : "TEXT");
                                final URL location = resolveRedirect(text);
                                testInfo = parseTestInfo(location, uploadFile);
                                if (testInfo != null) {
                                    testInfo.setLink(text);
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (final JSONException | InterruptedException | UnsupportedEncodingException
                    | KeyManagementException | NoSuchAlgorithmException | MalformedURLException e) {
                logger.logError("-> Could not parse ATX JSON response: " + e.getMessage());
            }
            return testInfo;
        }

        /**
         * Checks the error log file and aborts the upload if any.
         *
         * @param errorFile the error file
         * @param logger    the logger
         * @return whether error log file does not exists or no errors found inside
         * @throws IOException signals that an I/O exception has occurred
         */
        private boolean checkErrorLog(final FilePath errorFile, final TTConsoleLogger logger) throws IOException {
            boolean hasNoErrors = true;
            try {
                if (errorFile.exists()) {
                    hasNoErrors = false;
                    logger.logError("Error while uploading ATX report!");
                    final String content = errorFile.readToString();
                    logger.logDebug(String.format("Response: %s", content));

                    final JSONObject jsonObject = (JSONObject) new JsonSlurper().parseText(content);
                    final JSONArray jsonArray = jsonObject.optJSONArray("ENTRIES");
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            final String file = jsonArray.getJSONObject(i).getString("FILE");
                            final String status = jsonArray.getJSONObject(i).getString("STATUS");
                            final String text = jsonArray.getJSONObject(i).getString("TEXT");
                            logger.logError(String.format("%s: %s - %s", status, file, text));
                        }
                    }
                }
            } catch (final JSONException | InterruptedException e) {
                logger.logError("-> Could not parse ATX JSON response: " + e.getMessage());
                hasNoErrors = false;
            }
            return hasNoErrors;
        }

        /**
         * Parses the test information from the URL parameters.
         *
         * @param url        the URL location
         * @param uploadFile the upload file
         * @return the test info holder
         * @throws UnsupportedEncodingException the unsupported encoding exception
         */
        private TestInfoHolder parseTestInfo(final URL url, final FilePath uploadFile)
            throws UnsupportedEncodingException {
            final Map<String, String> params = splitQuery(url);
            if (params.isEmpty()) {
                return null;
            }

            String testName = params.get("testexecplan");
            TestType testType = TestType.PROJECT;
            if ("SinglePackageExecution".equals(testName)) {
                testType = TestType.PACKAGE;
                testName = uploadFile.getBaseName();
            }
            final long from = Long.parseLong(params.get("dateFrom"));
            final long to = Long.parseLong(params.get("dateTo"));

            return new TestInfoHolder(testName, testType, from, to);
        }

        /**
         * Returns the URL query parameters as map.
         *
         * @param url the URL
         * @return the parameter map
         * @throws UnsupportedEncodingException in case of an unsupported encoding
         */
        private Map<String, String> splitQuery(final URL url) throws UnsupportedEncodingException {
            final Map<String, String> queryMap = new LinkedHashMap<>();
            final String query = url.getQuery();
            final String[] pairs = query.split("&");
            for (final String pair : pairs) {
                final int idx = pair.indexOf('=');
                queryMap.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
            return queryMap;
        }

        /**
         * Resolves the given URL redirect.
         *
         * @param redirect the redirect URL
         * @return the resolved URL redirect
         * @throws MalformedURLException    in case of a malformed URL
         * @throws NoSuchAlgorithmException in case of a missing algorithm
         * @throws KeyManagementException   in case of a key management exception
         * @throws IOException              signals that an I/O exception has occurred
         */
        private URL resolveRedirect(final String redirect)
            throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
            final HttpURLConnection connection;
            final URL url = new URL(redirect);

            // Handle SSL connection
            final Object ignoreSSL = config.getSettingValueByGroup(
                "ignoreSSL", ATXSetting.SettingsGroup.CONNECTION);

            if (redirect.startsWith("https://") && ignoreSSL != null && (boolean) ignoreSSL) {
                connection = (HttpsURLConnection) url.openConnection();
                ATXValidator.ignoreSSLIssues((HttpsURLConnection) connection);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            final String location = connection.getHeaderField("Location");
            return new URL(location);
        }
    }

    /**
     * {@link Callable} parsing the test name, type and execution times of a TRF remotely.
     */
    private static final class ParseTRFCallable extends MasterToSlaveCallable<TestInfoHolder, IOException> {

        private static final long serialVersionUID = 1L;

        private static final String QUERY_INFO = "SELECT execution_time, duration from info";
        private static final String QUERY_PRJ = "SELECT name FROM prj";
        private static final String QUERY_PKG = "SELECT name FROM pkg";

        private final String trfFile;

        /**
         * Instantiates a new {@link ParseTRFCallable}.
         *
         * @param trfFile the TRF file path
         */
        ParseTRFCallable(final String trfFile) {
            this.trfFile = trfFile;
        }

        @Override
        public TestInfoHolder call() throws IOException {
            try (SQLite sql = new SQLite(trfFile)) {
                ResultSet rs = sql.query(QUERY_INFO);
                final String execTime = rs.getString("execution_time");
                final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final Date date = fmt.parse(execTime);
                final float duration = rs.getFloat("duration") * 1000.0f;
                final long from = date.getTime();
                final long to = from + (long) duration;

                rs = sql.query(QUERY_PRJ);
                final String prjName = rs.getString("name");
                if ("$$$_PACKAGE_$$$".equals(prjName)) {
                    rs = sql.query(QUERY_PKG);
                    final String pkgName = rs.getString("name");
                    return new TestInfoHolder(pkgName, TestType.PACKAGE, from, to);
                } else {
                    return new TestInfoHolder(prjName, TestType.PROJECT, from, to);
                }
            } catch (final ClassNotFoundException | SQLException | ParseException e) {
                throw new IOException(e);
            }
        }

        /**
         * Parser for SQLite databases like TRF reports.
         */
        private static class SQLite implements AutoCloseable {

            private final Connection connection;
            private PreparedStatement statement;

            /**
             * Instantiates a new {@link SQLite}.
             *
             * @param sqlFile the path to database file
             * @throws ClassNotFoundException in case the JDBC class cannot be located
             * @throws SQLException           in case of a SQL exception
             */
            SQLite(final String sqlFile) throws ClassNotFoundException, SQLException {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + sqlFile);
            }

            /**
             * Queries the database with given SQL statement.
             *
             * @param sql the SQL statement
             * @return the result set
             * @throws SQLException in case of a SQL exception
             */
            @SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
                                justification = "Constant query statements used only")
            public ResultSet query(final String sql) throws SQLException {
                statement = connection.prepareStatement(sql);
                return statement.executeQuery();
            }

            @Override
            public void close() throws SQLException {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    /**
     * Helper class storing information about the report upload.
     * Used as data model for {@link UploadReportCallable}.
     */
    private static final class UploadInfoHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        private boolean uploaded;
        private TestInfoHolder testInfo;

        /**
         * Instantiates a new {@link UploadInfoHolder}.
         *
         * @param uploaded the uploaded
         */
        UploadInfoHolder(final boolean uploaded) {
            this.uploaded = uploaded;
        }

        public boolean isUploaded() {
            return uploaded;
        }

        /**
         * Sets the upload state.
         *
         * @param uploaded specifies whether the report upload was successful
         */
        public void setUploaded(final boolean uploaded) {
            this.uploaded = uploaded;
        }

        public TestInfoHolder getTestInfo() {
            return testInfo;
        }

        /**
         * Sets test information.
         *
         * @param testInfo the test info
         */
        public void setTestInfo(final TestInfoHolder testInfo) {
            this.testInfo = testInfo;
        }
    }

    /**
     * Helper class storing information about the test name and type.
     * Used as data model for {@link ParseTRFCallable}.
     */
    private static final class TestInfoHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String testName;
        private final TestType testType;
        private final long from;
        private final long to;
        private String link;

        /**
         * Instantiates a new {@link TestInfoHolder}.
         *
         * @param testName the test name
         * @param testType the test type
         * @param from     the starting execution time
         * @param to       the finishing execution time
         */
        TestInfoHolder(final String testName, final TestType testType, final long from, final long to) {
            this.testName = testName;
            this.testType = testType;
            this.from = from;
            this.to = to;
            setLink(null);
        }

        public String getTestName() {
            return testName;
        }

        public TestType getTestType() {
            return testType;
        }

        public long getFrom() {
            return from;
        }

        public long getTo() {
            return to;
        }

        public String getLink() {
            return link;
        }

        /**
         * Sets the redirect link.
         *
         * @param link the redirect link
         */
        public void setLink(final String link) {
            this.link = link;
        }
    }
}
