/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import com.google.common.base.Preconditions;
import de.tracetronic.jenkins.plugins.ecutest.report.AbstractReportPublisher;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.JUnitValidator;
import hudson.EnvVars;
import javaposse.jobdsl.dsl.Context;
import org.apache.commons.lang.StringUtils;

/**
 * Common base class providing report-related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
    public abstract class AbstractReportContext implements Context {

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
