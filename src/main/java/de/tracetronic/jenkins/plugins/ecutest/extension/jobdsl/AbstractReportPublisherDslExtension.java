/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import com.google.common.base.Preconditions;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.JUnitValidator;
import hudson.EnvVars;
import javaposse.jobdsl.dsl.Context;
import org.apache.commons.lang.StringUtils;

/**
 * Common base class providing report-related DSL extensions.
 */
public abstract class AbstractReportPublisherDslExtension extends AbstractDslExtension {

    /**
     * Validator to check UNIT report related DSL options.
     */
    protected final JUnitValidator validator = new JUnitValidator();

    /**
     * Checks whether a tool installation identified by given name exists.
     *
     * @param toolName  the tool name
     * @param publisher the builder
     */
    protected void checkToolInstallation(final String toolName, final AbstractReportPublisher publisher) {
        if (StringUtils.containsNone(toolName, "$")) {
            Preconditions.checkNotNull(publisher.getToolInstallation(toolName, new EnvVars()),
                NO_INSTALL_MSG, toolName);
        }
    }

    /**
     * {@link Context} class providing common report related methods for the nested DSL context.
     */
    public abstract static class AbstractReportContext implements Context {

        /**
         * The allow missing reports setting.
         */
        protected boolean allowMissing;

        /**
         * The run on failed setting.
         */
        protected boolean runOnFailed;

        /**
         * The archiving setting.
         */
        protected boolean archiving = true;

        /**
         * The keep all archives setting.
         */
        protected boolean keepAll = true;

        /**
         * Option defining whether missing reports are allowed.
         *
         * @param value the value
         */
        public void allowMissing(final boolean value) {
            allowMissing = value;
        }

        /**
         * Option defining whether this publisher even runs on a failed build.
         *
         * @param value the value
         */
        public void runOnFailed(final boolean value) {
            runOnFailed = value;
        }

        /**
         * Option defining whether archiving artifacts is enabled.
         *
         * @param value the value
         */
        public void archiving(final boolean value) {
            archiving = value;
        }

        /**
         * Option defining whether artifacts are archived for all successful builds.
         *
         * @param value the value
         */
        public void keepAll(final boolean value) {
            keepAll = value;
        }
    }
}
