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

/**
 * Common base class for all export related task builders implemented in this plugin.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class AbstractExportBuilder extends AbstractTestHelper implements SimpleBuildStep {

    @Nonnull
    private final List<TMSConfig> exportConfigs;

    /**
     * Instantiates a new {@link AbstractExportBuilder}.
     *
     * @param exportConfigs
     *            the list of configured test exporters
     */
    @DataBoundConstructor
    public AbstractExportBuilder(@CheckForNull final List<TMSConfig> exportConfigs) {
        this.exportConfigs = (List<TMSConfig>) (exportConfigs == null ? new ArrayList<TMSConfig>()
                : removeEmptyConfigs(exportConfigs));
    }

    /**
     * @return the list of configured test exporters
     */
    @Nonnull
    public List<TMSConfig> getExportConfigs() {
        return Collections.unmodifiableList(exportConfigs);
    }

    /**
     * @param exportConfigs
     *            the list of configured test exporters
     */
    @DataBoundSetter
    public void setExportConfigs(@CheckForNull final List<TMSConfig> exportConfigs) {
        this.exportConfigs.addAll(exportConfigs);
    }

    /**
     * Removes empty export configurations.
     *
     * @param exportConfigs
     *            the export configurations
     * @return the list of valid export configurations
     */
    private static List<TMSConfig> removeEmptyConfigs(final List<TMSConfig> exportConfigs) {
        final List<TMSConfig> validConfigs = new ArrayList<TMSConfig>();
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

    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        // FIXME: workaround because pipeline node allocation does not create the actual workspace directory
        if (!workspace.exists()) {
            workspace.mkdirs();
        }

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
     * @param run
     *            the run
     * @param workspace
     *            the workspace
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return {@code true} if export succeeded, {@code false} otherwise
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws InterruptedException
     *             if the build gets interrupted
     */
    private boolean performExport(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws IOException, InterruptedException {
        final TTConsoleLogger logger = new TTConsoleLogger(listener);

        // Check for running ECU-TEST instance
        if (!checkETInstance(launcher, false)) {
            logger.logError("No running ECU-TEST instance found, please configure one at first!");
            return false;
        }

        boolean isExported = false;
        for (final TMSConfig exportConfig : exportConfigs) {
            // Expand export configuration
            final EnvVars buildEnv = run.getEnvironment(listener);
            final TMSConfig expExportConfig = (TMSConfig) exportConfig.expand(buildEnv);
            if (exportConfig instanceof ExportPackageConfig) {
                // Export package
                final ExportPackageClient exportClient = new ExportPackageClient((ExportPackageConfig) expExportConfig);
                isExported = exportClient.exportPackage(workspace, launcher, listener);
            } else if (exportConfig instanceof ExportPackageAttributeConfig) {
                // Export package attributes
                final ExportPackageClient exportClient = new ExportPackageClient(
                        (ExportPackageAttributeConfig) expExportConfig);
                isExported = exportClient.exportPackageAttributes(workspace, launcher, listener);
            } else if (exportConfig instanceof ExportProjectConfig) {
                // Export project
                final ExportProjectClient exportClient = new ExportProjectClient(
                        (ExportProjectConfig) expExportConfig);
                isExported = exportClient.exportProject(workspace, launcher, listener);
            } else if (exportConfig instanceof ExportProjectAttributeConfig) {
                // Export project attributes
                final ExportProjectClient exportClient = new ExportProjectClient(
                        (ExportProjectAttributeConfig) expExportConfig);
                isExported = exportClient.exportProjectAttributes(workspace, launcher, listener);
            } else {
                logger.logError("Unsupported export configuration of type:" + exportConfig.getClass());
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
         * @param clazz
         *            the {@link AbstractExportBuilder} class name
         */
        public DescriptorImpl(final Class<? extends AbstractExportBuilder> clazz) {
            super(clazz);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
