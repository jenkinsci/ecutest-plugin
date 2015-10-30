/**
 * Copyright (c) 2015 TraceTronic GmbH
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

import hudson.Extension;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

import com.google.common.base.Preconditions;

import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.ToolValidator;

/**
 * Class providing tool-related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@Extension(optional = true)
public class ToolBuilderDslExtension extends AbstractDslExtension {

    /**
     * Validator to check tool-related DSL options.
     */
    private final ToolValidator validator = new ToolValidator();

    /**
     * {@link DslExtensionMethod} providing the start up of ECU-TEST.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @param workspaceDir
     *            the ECU-TEST workspace directory
     * @return the instance of a {@link StartETBuilder}
     * @see StartETBuilder#StartETBuilder(String, String, String, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startET(final String toolName, final String workspaceDir) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);
        Preconditions.checkNotNull(workspaceDir, NOT_NULL_MSG, "workspaceDir");

        final StartETBuilder builder = new StartETBuilder(toolName, workspaceDir, null, false);
        Preconditions.checkNotNull(builder.getToolInstallation(), NO_INSTALL_MSG, toolName);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the start up of ECU-TEST.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link StartETBuilder}
     * @see StartETBuilder#StartETBuilder(String, String, String, boolean)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startET(final Runnable closure) {
        final StartETContext context = new StartETContext();
        executeInContext(closure, context);
        return new StartETBuilder(context.toolName, context.workspaceDir, context.timeout, context.debugMode);
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of ECU-TEST.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StopETBuilder}
     * @see StopETBuilder#StopETBuilder(String, String)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopET(final String toolName) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final StopETBuilder builder = new StopETBuilder(toolName, null);
        Preconditions.checkNotNull(builder.getToolInstallation(), NO_INSTALL_MSG, toolName);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of ECU-TEST.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link StopETBuilder}
     * @see StopETBuilder#StopETBuilder(String, String)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopET(final Runnable closure) {
        final StopETContext context = new StopETContext();
        executeInContext(closure, context);
        return new StopETBuilder(context.toolName, context.timeout);
    }

    /**
     * {@link DslExtensionMethod} providing the start up of the Tool-Server.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StartTSBuilder}
     * @see StartTSBuilder#StartTSBuilder(String, String, String, String)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startTS(final String toolName) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, toolName);

        final StartTSBuilder builder = new StartTSBuilder(toolName, null, null, null);
        Preconditions.checkNotNull(builder.getToolInstallation(), NO_INSTALL_MSG, toolName);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the start up of the Tool-Server.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link StartTSBuilder}
     * @see StartTSBuilder#StartTSBuilder(String, String, String, String)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startTS(final Runnable closure) {
        final StartTSContext context = new StartTSContext();
        executeInContext(closure, context);
        return new StartTSBuilder(context.toolName, context.timeout, context.toolLibsIni, context.tcpPort);
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of the Tool-Server.
     *
     * @param toolName
     *            the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StopTSBuilder}
     * @see StopTSBuilder#StopTSBuilder(String, String)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopTS(final String toolName) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final StopTSBuilder builder = new StopTSBuilder(toolName, null);
        Preconditions.checkNotNull(builder.getToolInstallation(), NO_INSTALL_MSG, toolName);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of the Tool-Server.
     *
     * @param closure
     *            the nested Groovy closure
     * @return the instance of a {@link StopTSBuilder}
     * @see StopTSBuilder#StopTSBuilder(String, String)
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopTS(final Runnable closure) {
        final StopTSContext context = new StopTSContext();
        executeInContext(closure, context);
        return new StopTSBuilder(context.toolName, context.timeout);
    }

    /**
     * {@link Context} class providing ECU-TEST start up methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class StartETContext implements Context {

        private String toolName;
        private String workspaceDir;
        private String timeout = String.valueOf(StartETBuilder.DEFAULT_TIMEOUT);
        private boolean debugMode = false;

        /**
         * Option identifying the {@link ETInstallation} to be used.
         *
         * @param value
         *            the value
         */
        public void toolName(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TOOL_NAME);
            toolName = value;
        }

        /**
         * Option defining the ECU-TEST workspace directory.
         *
         * @param value
         *            the value
         */
        public void workspaceDir(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, "workspaceDir");
            final FormValidation validation = validator.validateWorkspaceDir(value);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            workspaceDir = value;
        }

        /**
         * Option defining the timeout.
         *
         * @param value
         *            the value
         */
        public void timeout(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TIMEOUT);
            final FormValidation validation = validator.validateTimeout(value, StartETBuilder.DEFAULT_TIMEOUT);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            timeout = value;
        }

        /**
         * Option defining the debug mode.
         *
         * @param value
         *            the value
         */
        public void debugMode(final boolean value) {
            debugMode = value;
        }
    }

    /**
     * {@link Context} class providing ECU-TEST shut down methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class StopETContext implements Context {

        private String toolName;
        private String timeout = String.valueOf(StartETBuilder.DEFAULT_TIMEOUT);

        /**
         * Option identifying the {@link ETInstallation} to be used.
         *
         * @param value
         *            the value
         */
        public void toolName(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TOOL_NAME);
            toolName = value;
        }

        /**
         * Option defining the timeout.
         *
         * @param value
         *            the value
         */
        public void timeout(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TIMEOUT);
            final FormValidation validation = validator.validateTimeout(value, StopETBuilder.DEFAULT_TIMEOUT);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            timeout = value;
        }
    }

    /**
     * {@link Context} class providing Tool-Server start up methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class StartTSContext implements Context {

        private String toolName;
        private String toolLibsIni = "";
        private String tcpPort = "";
        private String timeout = String.valueOf(StartTSBuilder.DEFAULT_TIMEOUT);

        /**
         * Option identifying the {@link ETInstallation} to be used.
         *
         * @param value
         *            the value
         */
        public void toolName(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TOOL_NAME);
            toolName = value;
        }

        /**
         * Option defining the custom ToolLibs.ini path.
         *
         * @param value
         *            the value
         */
        public void toolLibsIni(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, "toolLibsIni");
            final FormValidation validation = validator.validateToolLibsIni(value);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            toolLibsIni = value;
        }

        /**
         * Option defining the custom TCP port.
         *
         * @param value
         *            the value
         */
        public void tcpPort(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, "tcpPort");
            final FormValidation validation = validator.validateTcpPort(value);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            tcpPort = value;
        }

        /**
         * Option defining the timeout.
         *
         * @param value
         *            the value
         */
        public void timeout(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TIMEOUT);
            final FormValidation validation = validator.validateTimeout(value, StartTSBuilder.DEFAULT_TIMEOUT);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            timeout = value;
        }
    }

    /**
     * {@link Context} class providing Tool-Server shut down methods for the nested DSL context.
     *
     * @author Christian Pönisch <christian.poenisch@tracetronic.de>
     */
    public class StopTSContext implements Context {

        private String toolName;
        private String timeout = String.valueOf(StartTSBuilder.DEFAULT_TIMEOUT);

        /**
         * Option identifying the {@link ETInstallation} to be used.
         *
         * @param value
         *            the value
         */
        public void toolName(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TOOL_NAME);
            toolName = value;
        }

        /**
         * Option defining the timeout.
         *
         * @param value
         *            the value
         */
        public void timeout(final String value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TIMEOUT);
            final FormValidation validation = validator.validateTimeout(value, StartTSBuilder.DEFAULT_TIMEOUT);
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            timeout = value;
        }
    }
}
