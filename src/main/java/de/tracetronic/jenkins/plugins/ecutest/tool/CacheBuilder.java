/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool;

import de.tracetronic.jenkins.plugins.ecutest.ETPluginException;
import de.tracetronic.jenkins.plugins.ecutest.log.TTConsoleLogger;
import de.tracetronic.jenkins.plugins.ecutest.tool.client.CacheClient;
import de.tracetronic.jenkins.plugins.ecutest.util.PathUtil;
import de.tracetronic.jenkins.plugins.ecutest.util.ProcessUtil;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder providing generation of ECU-TEST file caches.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class CacheBuilder extends Builder implements SimpleBuildStep {

    private final List<CacheConfig> caches;

    /**
     * Instantiates a new {@link CacheBuilder}.
     *
     * @param caches the list of configured caches
     */
    @DataBoundConstructor
    public CacheBuilder(final List<CacheConfig> caches) {
        super();
        this.caches = caches == null ? new ArrayList<>() : removeEmptyCaches(caches);
    }

    public List<CacheConfig> getCaches() {
        return caches;
    }

    /**
     * Removes empty cache configurations.
     *
     * @param caches the cache configurations
     * @return the list of valid cache configurations
     */
    private static List<CacheConfig> removeEmptyCaches(final List<CacheConfig> caches) {
        final List<CacheConfig> validCaches = new ArrayList<>();
        for (final CacheConfig cache : caches) {
            if (StringUtils.isNotBlank(cache.getFilePath())) {
                validCaches.add(cache);
            }
        }
        return validCaches;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws InterruptedException, IOException {
        try {
            ProcessUtil.checkOS(launcher);
            if (CacheClient.isCompatible(workspace, launcher, listener)) {
                performCache(run, workspace, launcher, listener);
            } else {
                run.setResult(Result.FAILURE);
            }
        } catch (final IOException e) {
            Util.displayIOException(e, listener);
            throw e;
        } catch (final ETPluginException e) {
            final TTConsoleLogger logger = new TTConsoleLogger(listener);
            logger.logError(e.getMessage());
            throw new AbortException(e.getMessage());
        }
    }

    /**
     * Performs the cache file generation for all cache configurations.
     *
     * @param run       the run
     * @param workspace the workspace
     * @param launcher  the launcher
     * @param listener  the listener
     * @throws IOException          signals that an I/O exception has occurred
     * @throws InterruptedException if the current thread is interrupted while waiting for the completion
     * @throws ETPluginException    in case of cache operation errors
     */
    private void performCache(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace,
                              @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
        throws IOException, InterruptedException, ETPluginException {
        // Expand build parameters
        final EnvVars buildEnvVars = run.getEnvironment(listener);
        for (CacheConfig cache : caches) {
            // Absolutize database file path and channel, if not absolute assume relative to build workspace
            CacheConfig expCache = cache.expand(buildEnvVars);
            String expFilePath = PathUtil.makeAbsolutePath(expCache.getFilePath(), workspace);
            String expDbChannel = PathUtil.makeAbsolutePath(expCache.getDbChannel(), workspace);

            CacheClient client = new CacheClient(expCache.getType(), expFilePath, expDbChannel, expCache.isClear());
            client.generateCache(launcher, listener);
        }
    }

    /**
     * DescriptorImpl for {@link CacheBuilder}.
     */
    @Symbol("generateCache")
    @Extension(ordinal = 10009)
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.CacheBuilder_DisplayName();
        }
    }
}
