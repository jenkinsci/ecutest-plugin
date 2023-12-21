/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ExportPackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ExportProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ExportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TMSConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Common base class for all export related task builders implemented in this plugin.
 */
public class AbstractExportBuilder extends AbstractTestHelper implements SimpleBuildStep {

    @Nonnull
    private final List<TMSConfig> exportConfigs;

    /**
     * Instantiates a new {@link AbstractExportBuilder}.
     *
     * @param exportConfigs the list of configured test exporters
     */
    @DataBoundConstructor
    public AbstractExportBuilder(@CheckForNull final List<TMSConfig> exportConfigs) {
        this.exportConfigs = exportConfigs == null ? new ArrayList<>()
            : removeEmptyConfigs(exportConfigs);
    }

    /**
     * Removes empty export configurations.
     *
     * @param exportConfigs the export configurations
     * @return the list of valid export configurations
     */
    private static List<TMSConfig> removeEmptyConfigs(final List<TMSConfig> exportConfigs) {
        final List<TMSConfig> validConfigs = new ArrayList<>();
        for (final TMSConfig config : exportConfigs) {
            if (config instanceof ExportConfig) {
                final ExportConfig pkgConfig = (ExportConfig) config;
                if (StringUtils.isNotBlank(pkgConfig.getFilePath())
                    && StringUtils.isNotBlank(pkgConfig.getExportPath())) {
                    validConfigs.add(config);
                }
            } else if (config instanceof ExportAttributeConfig) {
                final ExportAttributeConfig pkgAttrConfig = (ExportAttributeConfig) config;
                if (StringUtils.isNotBlank(pkgAttrConfig.getFilePath())) {
                    validConfigs.add(config);
                }
            }
        }
        return validConfigs;
    }

    @Nonnull
    public List<TMSConfig> getExportConfigs() {
        return Collections.unmodifiableList(exportConfigs);
    }

    /**
     * Sets the export configurations.
     *
     * @param exportConfigs the list of configured test exporters
     */
    @DataBoundSetter
    public void setExportConfigs(@CheckForNull final List<TMSConfig> exportConfigs) {
        if (exportConfigs != null) {
            this.exportConfigs.addAll(exportConfigs);
        }
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        try {
            ProcessUtil.checkOS(launcher);
            final boolean performed = performExport(run, workspace, launcher, listener);
            if (!performed) {
                throw new AbortException("Exporting to test management system failed!");
            }
        } catch (final IOException e) {
            Util.displayIOException(e, listener);
            throw e;
        } catch (final ETPluginException e) {
            logger.logError(e.getMessage());
            throw new AbortException(e.getMessage());
        }
    }

    /**
     * Performs the test exports.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean performExport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                                  final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ecu.test instance
        if (!checkETInstance(launcher, listener, false)) {
            logger.logError("No running ecu.test instance found, please configure one at first!");
            return false;
        }

        boolean isExported = false;
        for (final TMSConfig exportConfig : exportConfigs) {
            // Expand export configuration
            final EnvVars buildEnv = run.getEnvironment(listener);
            final TMSConfig expExportConfig = (TMSConfig) exportConfig.expand(buildEnv);
            if (exportConfig instanceof ExportPackageConfig) {
                // Export package
                final ExportPackageClient exportClient = new ExportPackageClient(expExportConfig);
                isExported = exportClient.exportPackage(run.getParent(), workspace, launcher, listener);
            } else if (exportConfig instanceof ExportPackageAttributeConfig) {
                // Export package attributes
                final ExportPackageClient exportClient = new ExportPackageClient(
                    expExportConfig);
                isExported = exportClient.exportPackageAttributes(run.getParent(), workspace, launcher, listener);
            } else if (exportConfig instanceof ExportProjectConfig) {
                // Export project
                final ExportProjectClient exportClient = new ExportProjectClient(
                    expExportConfig);
                isExported = exportClient.exportProject(run.getParent(), workspace, launcher, listener);
            } else if (exportConfig instanceof ExportProjectAttributeConfig) {
                // Export project attributes
                final ExportProjectClient exportClient = new ExportProjectClient(
                    expExportConfig);
                isExported = exportClient.exportProjectAttributes(run.getParent(), workspace, launcher, listener);
            } else {
                logger.logError("Unsupported export configuration of type: " + exportConfig.getClass());
            }
            if (!isExported) {
                return false;
            }
        }
        return true;
    }

    /**
     * DescriptorImpl for {@link AbstractExportBuilder}.
     */
    public abstract static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Instantiates a {@link AbstractExportBuilder}.
         *
         * @param clazz the {@link AbstractExportBuilder} class name
         */
        public DescriptorImpl(final Class<? extends AbstractExportBuilder> clazz) {
            super(clazz);
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
