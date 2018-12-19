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
import de.tracetronic.jenkins.plugins.ecutest.tool.StartETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopETBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.StopTSBuilder;
import de.tracetronic.jenkins.plugins.ecutest.tool.installation.ETInstallation;
import hudson.Extension;
import hudson.util.FormValidation;
import javaposse.jobdsl.dsl.Context;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/**
 * Class providing tool related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@Extension(optional = true)
public class ToolBuilderDslExtension extends AbstractToolBuilderDslExtension {

    private static final String OPT_WORKSPACE_DIR = "workspaceDir";
    private static final String OPT_SETTINGS_DIR = "settingsDir";

    /**
     * {@link DslExtensionMethod} providing the start up of ECU-TEST.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @param closure  the nested Groovy closure
     * @return the instance of a {@link StartETBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startET(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final StartETContext context = new StartETContext();
        executeInContext(closure, context);

        final StartETBuilder builder = new StartETBuilder(toolName.toString());
        builder.setWorkspaceDir(context.workspaceDir);
        builder.setSettingsDir(context.settingsDir);
        builder.setTimeout(context.timeout);
        builder.setDebugMode(context.debugMode);
        builder.setKeepInstance(context.keepInstance);
        checkToolInstallation(toolName.toString(), builder);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the start up of ECU-TEST with default settings.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StartETBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startET(final CharSequence toolName) {
        return startET(toolName, null);
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of ECU-TEST.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @param closure  the nested Groovy closure
     * @return the instance of a {@link StopETBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopET(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final StopETContext context = new StopETContext();
        executeInContext(closure, context);

        final StopETBuilder builder = new StopETBuilder(toolName.toString());
        builder.setTimeout(context.timeout);
        checkToolInstallation(toolName.toString(), builder);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of ECU-TEST with default settings.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StopETBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopET(final CharSequence toolName) {
        return stopET(toolName, null);
    }

    /**
     * {@link DslExtensionMethod} providing the start up of the Tool-Server.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @param closure  the nested Groovy closure
     * @return the instance of a {@link StartTSBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startTS(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final StartTSContext context = new StartTSContext();
        executeInContext(closure, context);

        final StartTSBuilder builder = new StartTSBuilder(toolName.toString());
        builder.setToolLibsIni(context.toolLibsIni);
        builder.setTcpPort(context.tcpPort);
        builder.setTimeout(context.timeout);
        builder.setKeepInstance(context.keepInstance);
        checkToolInstallation(toolName.toString(), builder);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the start up of the Tool-Server with default settings.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StartTSBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object startTS(final CharSequence toolName) {
        return startTS(toolName, null);
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of the Tool-Server.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @param closure  the nested Groovy closure
     * @return the instance of a {@link StopTSBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopTS(final CharSequence toolName, final Runnable closure) {
        Preconditions.checkNotNull(toolName, NOT_NULL_MSG, OPT_TOOL_NAME);

        final StopTSContext context = new StopTSContext();
        executeInContext(closure, context);

        final StopTSBuilder builder = new StopTSBuilder(toolName.toString());
        builder.setTimeout(context.timeout);
        checkToolInstallation(toolName.toString(), builder);
        return builder;
    }

    /**
     * {@link DslExtensionMethod} providing the shut down of the Tool-Server with default settings.
     *
     * @param toolName the tool name identifying the {@link ETInstallation} to be used
     * @return the instance of a {@link StopTSBuilder}
     */
    @DslExtensionMethod(context = StepContext.class)
    public Object stopTS(final CharSequence toolName) {
        return stopTS(toolName, null);
    }

    /**
     * {@link Context} class providing ECU-TEST start up methods for the nested DSL context.
     */
    public class StartETContext extends AbstractToolContext {

        private String workspaceDir;
        private String settingsDir;
        private boolean debugMode;
        private boolean keepInstance;

        @Override
        protected int getDefaultTimeout() {
            return StartETBuilder.DEFAULT_TIMEOUT;
        }

        /**
         * Option defining the ECU-TEST workspace directory.
         *
         * @param value the value
         */
        public void workspaceDir(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_WORKSPACE_DIR);
            final FormValidation validation = validator.validateWorkspaceDir(value.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            workspaceDir = value.toString();
        }

        /**
         * Option defining the ECU-TEST settings directory.
         *
         * @param value the value
         */
        public void settingsDir(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_SETTINGS_DIR);
            final FormValidation validation = validator.validateSettingsDir(value.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            settingsDir = value.toString();
        }

        /**
         * Option defining the debug mode.
         *
         * @param value the value
         */
        public void debugMode(final boolean value) {
            debugMode = value;
        }

        /**
         * Option defining whether to re-use the previous instance.
         *
         * @param value the value
         */
        public void keepInstance(final boolean value) {
            keepInstance = value;
        }
    }

    /**
     * {@link Context} class providing ECU-TEST shut down methods for the nested DSL context.
     */
    public class StopETContext extends AbstractToolContext {

        @Override
        protected int getDefaultTimeout() {
            return StopETBuilder.DEFAULT_TIMEOUT;
        }
    }

    /**
     * {@link Context} class providing Tool-Server start up methods for the nested DSL context.
     */
    public class StartTSContext extends AbstractToolContext {

        private static final String OPT_TOOLLIBS_INI = "toolLibsIni";
        private static final String OPT_TCP_PORT = "tcpPort";

        private String toolLibsIni;
        private String tcpPort;
        private boolean keepInstance;

        @Override
        protected int getDefaultTimeout() {
            return StartTSBuilder.DEFAULT_TIMEOUT;
        }

        /**
         * Option defining the custom ToolLibs.ini path.
         *
         * @param value the value
         */
        public void toolLibsIni(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TOOLLIBS_INI);
            final FormValidation validation = validator.validateToolLibsIni(value.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            toolLibsIni = value.toString();
        }

        /**
         * Option defining the custom TCP port.
         *
         * @param value the value as String
         */
        public void tcpPort(final CharSequence value) {
            Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TCP_PORT);
            final FormValidation validation = validator.validateTcpPort(value.toString());
            Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
            tcpPort = value.toString();
        }

        /**
         * Option defining the custom TCP port.
         *
         * @param value the value as Integer
         */
        public void tcpPort(final int value) {
            tcpPort(String.valueOf((Object) value));
        }

        /**
         * Option defining whether to re-use the previous instance.
         *
         * @param value the value
         */
        public void keepInstance(final boolean value) {
            keepInstance = value;
        }
    }

    /**
     * {@link Context} class providing Tool-Server shut down methods for the nested DSL context.
     */
    public class StopTSContext extends AbstractToolContext {

        @Override
        protected int getDefaultTimeout() {
            return StopTSBuilder.DEFAULT_TIMEOUT;
        }
    }
}
