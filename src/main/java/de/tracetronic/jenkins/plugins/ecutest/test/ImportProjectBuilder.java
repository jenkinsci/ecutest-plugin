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
package de.tracetronic.jenkins.plugins.ecutest.test;

import hudson.AbortException;
import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
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
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.test.client.ImportProjectClient;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectArchiveConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.ImportProjectConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;

/**
 * Builder providing the import of one or multiple ECU-TEST projects.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ImportProjectBuilder extends AbstractTestHelper implements SimpleBuildStep {

    @Nonnull
    private final List<ImportProjectConfig> importConfigs;

    /**
     * Instantiates a new {@link ImportProjectBuilder}.
     *
     * @param importConfigs
     *            the list of configured project importers
     */
    @DataBoundConstructor
    public ImportProjectBuilder(@CheckForNull final List<ImportProjectConfig> importConfigs) {
        this.importConfigs = importConfigs == null ? new ArrayList<ImportProjectConfig>()
                : removeEmptyConfigs(importConfigs);
    }

    /**
     * @return the list of configured project importers
     */
    @Nonnull
    public List<ImportProjectConfig> getImportConfigs() {
        return Collections.unmodifiableList(importConfigs);
    }

    /**
     * @param importConfigs
     *            the list of configured project importers
     */
    @DataBoundSetter
    public void setImportConfig(@CheckForNull final List<ImportProjectConfig> importConfigs) {
        this.importConfigs.addAll(importConfigs);
    }

    /**
     * Removes empty import configurations.
     *
     * @param importConfigs
     *            the import configurations
     * @return the list of valid import configurations
     */
    private static List<ImportProjectConfig> removeEmptyConfigs(final List<ImportProjectConfig> importConfigs) {
        final List<ImportProjectConfig> validConfigs = new ArrayList<ImportProjectConfig>();
        for (final ImportProjectConfig config : importConfigs) {
            if (StringUtils.isNotBlank(config.getProjectPath())) {
                validConfigs.add(config);
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
            final boolean performed = performImport(run, workspace, launcher, listener);
            if (!performed) {
                throw new AbortException("Importing projects failed!");
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
     * Performs the project imports.
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

        for (final ImportProjectConfig importConfig : importConfigs) {
            // Expand import configuration
            final EnvVars buildEnv = run.getEnvironment(listener);
            ImportProjectConfig expImportConfig = (ImportProjectConfig) importConfig.expand(buildEnv);

            // Absolutize archive path, if not absolute assume relative to build workspace
            if (expImportConfig instanceof ImportProjectArchiveConfig) {
                final String expArchivePath = PathUtil.makeAbsolutePath(expImportConfig.getProjectPath(), workspace);
                final ImportProjectArchiveConfig archiveConfig = (ImportProjectArchiveConfig) expImportConfig;
                expImportConfig = new ImportProjectArchiveConfig(expArchivePath, archiveConfig.getImportPath(),
                        archiveConfig.getImportConfigPath(), archiveConfig.isReplaceFiles(),
                        archiveConfig.getTimeout());
            }

            // Import project
            final ImportProjectClient importClient = new ImportProjectClient(expImportConfig);
            if (!importClient.importProject(workspace, launcher, listener)) {
                return false;
            }
        }
        return true;
    }

    /**
     * DescriptorImpl for {@link ImportProjectBuilder}.
     */
    @Symbol("importProjects")
    @Extension(ordinal = 1003)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super();
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.ImportProjectBuilder_DisplayName();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Gets the applicable project importers.
         *
         * @return the applicable project importers
         */
        public List<Descriptor<? extends ImportProjectConfig>> getApplicableImporters() {
            final List<Descriptor<? extends ImportProjectConfig>> list = new ArrayList<>();
            final DescriptorExtensionList<ImportProjectConfig, Descriptor<ImportProjectConfig>> configs =
                    ImportProjectConfig.all();
            if (configs != null) {
                list.addAll(configs);
            }
            return list;
        }
    }
}
