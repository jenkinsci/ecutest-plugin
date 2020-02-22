/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ImportPackageClient;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ImportProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportPackageConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectAttributeConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
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
 * Common base class for all import related task builders implemented in this plugin.
 */
public class AbstractImportBuilder extends AbstractTestHelper implements SimpleBuildStep {

    @Nonnull
    private final List<TMSConfig> importConfigs;

    /**
     * Instantiates a new {@link AbstractImportBuilder}.
     *
     * @param importConfigs the list of configured test importers
     */
    @DataBoundConstructor
    public AbstractImportBuilder(@CheckForNull final List<TMSConfig> importConfigs) {
        this.importConfigs = importConfigs == null ? new ArrayList<>()
            : removeEmptyConfigs(importConfigs);
    }

    /**
     * Removes empty import configurations.
     *
     * @param importConfigs the import configurations
     * @return the list of valid import configurations
     */
    private static List<TMSConfig> removeEmptyConfigs(final List<TMSConfig> importConfigs) {
        final List<TMSConfig> validConfigs = new ArrayList<>();
        for (final TMSConfig config : importConfigs) {
            if (config instanceof ImportConfig) {
                final ImportConfig pkgConfig = (ImportConfig) config;
                if (StringUtils.isNotBlank(pkgConfig.getTmsPath())) {
                    validConfigs.add(config);
                }
            } else if (config instanceof ImportAttributeConfig) {
                final ImportAttributeConfig pkgAttrConfig = (ImportAttributeConfig) config;
                if (StringUtils.isNotBlank(pkgAttrConfig.getFilePath())) {
                    validConfigs.add(config);
                }
            }
        }
        return validConfigs;
    }

    /**
     * @return the list of configured test importers
     */
    @Nonnull
    public List<TMSConfig> getImportConfigs() {
        return Collections.unmodifiableList(importConfigs);
    }

    /**
     * @param importConfigs the list of configured test importers
     */
    @DataBoundSetter
    public void setImportConfigs(@CheckForNull final List<TMSConfig> importConfigs) {
        if (importConfigs != null) {
            this.importConfigs.addAll(importConfigs);
        }
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws InterruptedException, IOException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);
        try {
            ProcessUtil.checkOS(launcher);
            final boolean performed = performImport(run, workspace, launcher, listener);
            if (!performed) {
                throw new AbortException("Importing to test management system failed!");
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
     * Performs the test imports.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the build gets interrupted
     */
    private boolean performImport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
                                  final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, listener, false)) {
            logger.logError("No running ECU-TEST instance found, please configure one at first!");
            return false;
        }

        boolean isImported = false;
        for (final TMSConfig importConfig : importConfigs) {
            // Expand import configuration
            final EnvVars buildEnv = run.getEnvironment(listener);
            final TMSConfig expImportConfig = (TMSConfig) importConfig.expand(buildEnv);
            if (importConfig instanceof ImportPackageConfig) {
                // Import package
                final ImportPackageClient importClient = new ImportPackageClient(expImportConfig);
                isImported = importClient.importPackage(run.getParent(), workspace, launcher, listener);
            } else if (importConfig instanceof ImportPackageAttributeConfig) {
                // Import package attributes
                final ImportPackageClient importClient = new ImportPackageClient(
                    expImportConfig);
                isImported = importClient.importPackageAttributes(run.getParent(), workspace, launcher, listener);
            } else if (importConfig instanceof ImportProjectConfig) {
                // Import project
                final ImportProjectClient importClient = new ImportProjectClient(
                    expImportConfig);
                isImported = importClient.importProject(run.getParent(), workspace, launcher, listener);
            } else if (importConfig instanceof ImportProjectAttributeConfig) {
                // Import project attributes
                final ImportProjectClient importClient = new ImportProjectClient(
                    expImportConfig);
                isImported = importClient.importProjectAttributes(run.getParent(), workspace, launcher, listener);
            } else if (importConfig instanceof ImportProjectArchiveConfig) {
                // Import project archive
                final ImportProjectClient importClient = new ImportProjectClient(
                    expImportConfig);
                isImported = importClient.importProjectArchive(launcher, listener);
            } else {
                logger.logError("Unsupported import configuration of type: " + importConfig.getClass());
            }
            if (!isImported) {
                return false;
            }
        }
        return true;
    }

    /**
     * DescriptorImpl for {@link AbstractImportBuilder}.
     */
    public abstract static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Instantiates a {@link AbstractImportBuilder}.
         *
         * @param clazz the {@link AbstractImportBuilder} class name
         */
        public DescriptorImpl(final Class<? extends AbstractImportBuilder> clazz) {
            super(clazz);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
