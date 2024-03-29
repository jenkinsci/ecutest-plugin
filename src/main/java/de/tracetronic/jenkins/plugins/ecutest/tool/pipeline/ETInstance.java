/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import com.google.common.collect.Maps;
import de.tracetronic.jenkins.plugins.ecutest.report.generator.ReportGeneratorConfig;
import de.tracetronic.jenkins.plugins.ecutest.report.ta.TraceAnalysisPublisher;
import de.tracetronic.jenkins.plugins.ecutest.report.tms.TMSPublisher;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Class holding ecu.test installation specific settings in order to start and stop instances.
 */
public class ETInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String KEY_TOOL_NAME = "toolName";
    private static final String KEY_INSTALLATION = "installation";
    private static final String KEY_TIMEOUT = "timeout";

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    @Nonnull
    private final transient ETInstallation installation;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient CpsScript script;

    /**
     * Instantiates a new {@link ETInstance}.
     *
     * @param installation the ecu.test installation
     */
    public ETInstance(@Nonnull final ETInstallation installation) {
        this.installation = installation;
    }

    @Nonnull
    @Whitelisted
    public ETInstallation getInstallation() {
        return installation;
    }

    /**
     * Sets the pipeline script.
     *
     * @param script the pipeline script
     */
    public void setScript(final CpsScript script) {
        this.script = script;
    }

    /**
     * Starts ecu.test with default settings.
     */
    @Whitelisted
    public void start() {
        start("", "", StartETBuilder.DEFAULT_TIMEOUT, false, false, false);
    }

    /**
     * Starts ecu.test with given workspace settings.
     *
     * @param workspaceDir the workspace directory
     * @param settingsDir  the settings directory
     */
    @Whitelisted
    public void start(final String workspaceDir, final String settingsDir) {
        start(workspaceDir, settingsDir, StartETBuilder.DEFAULT_TIMEOUT, false, false, false);
    }

    /**
     * Starts ecu.test with all available settings.
     *
     * @param workspaceDir   the workspace directory
     * @param settingsDir    the settings directory
     * @param timeout        the timeout
     * @param debugMode      specifies whether to enable debug mode
     * @param keepInstance   specifies whether to re-use the previous instance
     * @param updateUserLibs specifies whether to update all user libraries
     */
    @Whitelisted
    public void start(final String workspaceDir, final String settingsDir, final int timeout, final boolean debugMode,
                      final boolean keepInstance, final boolean updateUserLibs) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put(KEY_TIMEOUT, String.valueOf(timeout));
        stepVariables.put("workspaceDir", workspaceDir);
        stepVariables.put("settingsDir", settingsDir);
        stepVariables.put("debugMode", debugMode);
        stepVariables.put("keepInstance", keepInstance);
        stepVariables.put("updateUserLibs", updateUserLibs);
        script.invokeMethod("startET", stepVariables);
    }

    /**
     * Starts ecu.test with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void start(final Map<String, Object> settings) {
        invokeMethod("startET", settings);
    }


    /**
     * Stops ecu.test with default settings.
     */
    @Whitelisted
    public void stop() {
        stop(StopETBuilder.DEFAULT_TIMEOUT);
    }

    /**
     * Stops ecu.test with all available settings.
     *
     * @param timeout the timeout
     */
    @Whitelisted
    public void stop(final int timeout) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put(KEY_TIMEOUT, String.valueOf(timeout));
        script.invokeMethod("stopET", stepVariables);
    }

    /**
     * Stops ecu.test with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void stop(final Map<String, Object> settings) {
        invokeMethod("stopET", settings);
    }

    /**
     * Starts Tool-Server with default settings.
     */
    @Whitelisted
    public void startTS() {
        startTS("", StartTSBuilder.DEFAULT_TCP_PORT, StartETBuilder.DEFAULT_TIMEOUT, false);
    }

    /**
     * Starts Tool-Server with all available settings.
     *
     * @param toolLibsIniPath the alternative ToolLibs.ini path
     * @param tcpPort         the alternative TCP port
     * @param timeout         the timeout
     * @param keepInstance    specifies whether to re-use the previous instance
     */
    @Whitelisted
    public void startTS(final String toolLibsIniPath, final int tcpPort,
                        final int timeout, final boolean keepInstance) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put(KEY_TIMEOUT, String.valueOf(timeout));
        stepVariables.put("toolLibsIniPath", toolLibsIniPath);
        stepVariables.put("tcpPort", tcpPort);
        stepVariables.put("keepInstance", keepInstance);
        script.invokeMethod("startTS", stepVariables);
    }

    /**
     * Starts Tool-Server with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void startTS(final Map<String, Object> settings) {
        invokeMethod("startTS", settings);
    }

    /**
     * Stops Tool-Server with default settings.
     */
    @Whitelisted
    public void stopTS() {
        stop(StopTSBuilder.DEFAULT_TIMEOUT);
    }

    /**
     * Stops Tool-Server with all available settings.
     *
     * @param timeout the timeout
     */
    @Whitelisted
    public void stopTS(final int timeout) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put(KEY_TIMEOUT, String.valueOf(timeout));
        script.invokeMethod("stopTS", stepVariables);
    }

    /**
     * Stops Tool-Server with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void stopTS(final Map<String, Object> settings) {
        invokeMethod("stopTS", settings);
    }

    /**
     * Publishes UNIT reports with default settings.
     */
    @Whitelisted
    public void publishUNIT() {
        publishUNIT(0, 0, false, false);
    }

    /**
     * Publishes UNIT reports with all available settings.
     *
     * @param unstableThreshold the unstable threshold
     * @param failedThreshold   the failed threshold
     * @param allowMissing      specifies whether missing reports are allowed
     * @param runOnFailed       specifies whether this publisher even runs on a failed build
     */
    @Whitelisted
    public void publishUNIT(final double unstableThreshold, final double failedThreshold,
                            final boolean allowMissing, final boolean runOnFailed) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put("unstableThreshold", unstableThreshold);
        stepVariables.put("failedThreshold", failedThreshold);
        stepVariables.put("allowMissing", allowMissing);
        stepVariables.put("runOnFailed", runOnFailed);
        script.invokeMethod("publishUNIT", stepVariables);
    }

    /**
     * Publishes UNIT reports with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void publishUNIT(final Map<String, Object> settings) {
        invokeMethod("publishUNIT", settings);
    }

    /**
     * Publishes generator reports with default settings.
     *
     * @param generators       the report generators
     * @param customGenerators the custom report generators
     */
    @Whitelisted
    public void publishGenerators(final List<ReportGeneratorConfig> generators,
                                  final List<ReportGeneratorConfig> customGenerators) {
        publishGenerators(generators, customGenerators, false, false, true, true);
    }

    /**
     * Publishes generator reports with all available settings.
     *
     * @param generators       the report generators
     * @param customGenerators the custom report generators
     * @param allowMissing     specifies whether missing reports are allowed
     * @param runOnFailed      specifies whether this publisher even runs on a failed build
     * @param archiving        specifies whether archiving artifacts is enabled
     * @param keepAll          specifies whether artifacts are archived for all successful builds,
     *                         otherwise only the most recent
     */
    @Whitelisted
    public void publishGenerators(final List<ReportGeneratorConfig> generators,
                                  final List<ReportGeneratorConfig> customGenerators,
                                  final boolean allowMissing, final boolean runOnFailed,
                                  final boolean archiving, final boolean keepAll) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put("generators", generators);
        stepVariables.put("customGenerators", customGenerators);
        stepVariables.put("allowMissing", allowMissing);
        stepVariables.put("runOnFailed", runOnFailed);
        stepVariables.put("archiving", archiving);
        stepVariables.put("keepAll", keepAll);
        script.invokeMethod("publishGenerators", stepVariables);
    }

    /**
     * Publishes generator reports with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void publishGenerators(final Map<String, Object> settings) {
        invokeMethod("publishGenerators", settings);
    }

    /**
     * Publishes reports to a test management system with default settings.
     *
     * @param credentialsId the credentials id
     */
    @Whitelisted
    public void publishTMS(final String credentialsId) {
        publishTMS(credentialsId, TMSPublisher.getDefaultTimeout(), false, false, true, true);
    }

    /**
     * Publishes reports to a test management system with all available settings.
     *
     * @param credentialsId the credentials id
     * @param timeout       the timeout
     * @param allowMissing  specifies whether missing reports are allowed
     * @param runOnFailed   specifies whether this publisher even runs on a failed build
     * @param archiving     specifies whether archiving artifacts is enabled
     * @param keepAll       specifies whether artifacts are archived for all successful builds,
     *                      otherwise only the most recent
     */
    @Whitelisted
    public void publishTMS(final String credentialsId, final int timeout,
                           final boolean allowMissing, final boolean runOnFailed,
                           final boolean archiving, final boolean keepAll) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put(KEY_TIMEOUT, String.valueOf(timeout));
        stepVariables.put("credentialsId", credentialsId);
        stepVariables.put("allowMissing", allowMissing);
        stepVariables.put("runOnFailed", runOnFailed);
        stepVariables.put("archiving", archiving);
        stepVariables.put("keepAll", keepAll);
        script.invokeMethod("publishTMS", stepVariables);
    }

    /**
     * Publishes reports to a test management system with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void publishTMS(final Map<String, Object> settings) {
        invokeMethod("publishTMS", settings);
    }

    /**
     * Runs the trace analyses and publishes the generated reports with default settings.
     */
    @Whitelisted
    public void publishTraceAnalysis() {
        publishTraceAnalysis(true, false, TraceAnalysisPublisher.getDefaultTimeout(), false, false, true, true);
    }

    /**
     * Runs the trace analyses and publishes the generated reports with all available settings.
     *
     * @param mergeReports    specifies whether to merge analysis job reports,
     * @param createReportDir specifies whether to create a new report directory,
     * @param timeout         the timeout
     * @param allowMissing    specifies whether missing reports are allowed
     * @param runOnFailed     specifies whether this publisher even runs on a failed build
     * @param archiving       specifies whether archiving artifacts is enabled
     * @param keepAll         specifies whether artifacts are archived for all successful builds,
     *                        otherwise only the most recent
     */
    @Whitelisted
    public void publishTraceAnalysis(final boolean mergeReports, final boolean createReportDir, final int timeout,
                                     final boolean allowMissing, final boolean runOnFailed,
                                     final boolean archiving, final boolean keepAll) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.put(KEY_TIMEOUT, String.valueOf(timeout));
        stepVariables.put("mergeReports", mergeReports);
        stepVariables.put("createReportDir", createReportDir);
        stepVariables.put("allowMissing", allowMissing);
        stepVariables.put("runOnFailed", runOnFailed);
        stepVariables.put("archiving", archiving);
        stepVariables.put("keepAll", keepAll);
        script.invokeMethod("publishTraceAnalysis", stepVariables);
    }

    /**
     * Runs the trace analyses and publishes the generated reports with given settings as named arguments map.
     *
     * @param settings the settings map
     */
    @Whitelisted
    public void publishTraceAnalysis(final Map<String, Object> settings) {
        invokeMethod("publishTraceAnalysis", settings);
    }

    /**
     * Checks whether the currently selected configurations are started.
     *
     * @return {@code true} if configurations are started, {@code false} otherwise
     */
    @Whitelisted
    public boolean isConfigStarted() {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        return (boolean) script.invokeMethod("isConfigStarted", stepVariables);
    }

    /**
     * Invokes a pipeline step by name with given settings as named arguments map.
     * Settings which are not defined for the pipeline step will be ignored.
     * Missing optional settings are set to their respective default value.
     *
     * @param name     the method name
     * @param settings the settings map
     */
    private void invokeMethod(final String name, final Map<String, Object> settings) {
        final Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put(KEY_TOOL_NAME, installation.getName());
        stepVariables.put(KEY_INSTALLATION, installation);
        stepVariables.putAll(settings);
        script.invokeMethod(name, stepVariables);
    }
}
