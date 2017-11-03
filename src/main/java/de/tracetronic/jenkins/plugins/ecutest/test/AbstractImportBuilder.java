/*
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
package de.tracetronic.jenkins.plugins.ecutest.test;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jenkins.tasks.SimpleBuildStep;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

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

/**
 * Common base class for all import related task builders implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class AbstractImportBuilder extends AbstractTestHelper implements SimpleBuildStep {

    @Nonnull
    private final List<TMSConfig> importConfigs;

    /**
     * Instantiates a new {@link AbstractImportBuilder}.
     *
     * @param importConfigs
     *            the list of configured test importers
     */
    @DataBoundConstructor
    public AbstractImportBuilder(@CheckForNull final List<TMSConfig> importConfigs) {
        this.importConfigs = (List<TMSConfig>) (importConfigs == null ? new ArrayList<TMSConfig>()
                : removeEmptyConfigs(importConfigs));
    }

    /**
     * @return the list of configured test importers
     */
    @Nonnull
    public List<TMSConfig> getImportConfigs() {
        return Collections.unmodifiableList(importConfigs);
    }

    /**
     * @param importConfigs
     *            the list of configured test importers
     */
    @DataBoundSetter
    public void setImportConfigs(@CheckForNull final List<TMSConfig> importConfigs) {
        this.importConfigs.addAll(importConfigs);
    }

    /**
     * Removes empty import configurations.
     *
     * @param importConfigs
     *            the import configurations
     * @return the list of valid import configurations
     */
    private static List<TMSConfig> removeEmptyConfigs(final List<TMSConfig> importConfigs) {
        final List<TMSConfig> validConfigs = new ArrayList<TMSConfig>();
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

    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
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
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if import succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean performImport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, false)) {
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
                final ImportPackageClient importClient = new ImportPackageClient((ImportPackageConfig) expImportConfig);
                isImported = importClient.importPackage(workspace, launcher, listener);
            } else if (importConfig instanceof ImportPackageAttributeConfig) {
                // Import package attributes
                final ImportPackageClient importClient = new ImportPackageClient(
                        (ImportPackageAttributeConfig) expImportConfig);
                isImported = importClient.importPackageAttributes(workspace, launcher, listener);
            } else if (importConfig instanceof ImportProjectConfig) {
                // Import project
                final ImportProjectClient importClient = new ImportProjectClient(
                        (ImportProjectConfig) expImportConfig);
                isImported = importClient.importProject(workspace, launcher, listener);
            } else if (importConfig instanceof ImportProjectAttributeConfig) {
                // Import project attributes
                final ImportProjectClient importClient = new ImportProjectClient(
                        (ImportProjectAttributeConfig) expImportConfig);
                isImported = importClient.importProjectAttributes(workspace, launcher, listener);
            } else if (importConfig instanceof ImportProjectArchiveConfig) {
                // Import project archive
                final ImportProjectClient importClient = new ImportProjectClient(
                        (ImportProjectArchiveConfig) expImportConfig);
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
         * @param clazz
         *            the {@link AbstractImportBuilder} class name
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
