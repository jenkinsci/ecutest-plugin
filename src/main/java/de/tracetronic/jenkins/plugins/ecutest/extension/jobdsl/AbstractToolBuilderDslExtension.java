/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import com.google.common.base.Preconditions;
import de.tracetronic.jenkins.plugins.ecutest.tool.AbstractToolBuilder;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;

/**
 * Common base class providing tool-related DSL extensions.
 */
public abstract class AbstractToolBuilderDslExtension extends AbstractDslExtension {

    /**
     * Validator to check tool related DSL options.
     */
    protected final ToolValidator validator = new ToolValidator();

    /**
     * Checks whether a tool installation identified by given name exists.
     *
     * @param toolName the tool name
     * @param builder  the builder
     */
    protected void checkToolInstallation(final String toolName, final AbstractToolBuilder builder) {
        if (!toolName.isEmpty() && !toolName.contains("$")) {
            Preconditions.checkNotNull(builder.getToolInstallation(new EnvVars()), NO_INSTALL_MSG, toolName);
        }
    }

    /**
     * {@link Context} class providing common test related methods for the nested DSL context.
     */
    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR", justification = "Never used in a "
        + "critical way. Do not change in working legacy code.")
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
         * @param value the value as String
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
         * @param value the value as Integer
         */
        public void timeout(final int value) {
            timeout(String.valueOf((Object) value));
        }
    }
}
