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
import de.tracetronic.jenkins.plugins.ecutest.tool.AbstractToolBuilder;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;
import hudson.EnvVars;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;

/**
 * Common base class providing tool-related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractToolBuilderDslExtension extends AbstractDslExtension {

    /**
     * Validator to check tool related DSL options.
     */
    protected final ToolValidator validator = new ToolValidator();

    /**
     * Checks whether a tool installation identified by given name exists.
     *
     * @param toolName
     *            the tool name
     * @param builder
     *            the builder
     */
    protected void checkToolInstallation(final String toolName, final AbstractToolBuilder builder) {
        if (!toolName.isEmpty() && !toolName.contains("$")) {
            Preconditions.checkNotNull(builder.getToolInstallation(new EnvVars()), NO_INSTALL_MSG, toolName);
        }
    }

    /**
     * {@link Context} class providing common test related methods for the nested DSL context.
     */
    public abstract class AbstractToolContext implements Context {

        /**
         * The timeout for tool interaction.
         */
        protected String timeout = String.valueOf(getDefaultTimeout());

        /**
         * Defines the default timeout.
         *
         * @return the default timeout
         */
        protected abstract int getDefaultTimeout();

        /**
         * Option defining the timeout.
         *
         * @param value
         *            the value as String
         */
        public void timeout(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TIMEOUT);
            final FormValidation validation = validator.validateTimeout(value.toString(), getDefaultTimeout());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            timeout = value.toString();
        }

        /**
         * Option defining the timeout.
         *
         * @param value
         *            the value as Integer
         */
        public void timeout(final int value) {
            timeout(String.valueOf((Object) value));
        }
    }
}
