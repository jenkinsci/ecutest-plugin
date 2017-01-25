/*
 * Copyright (c) 2015-2016 TraceTronic GmbH
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

import hudson.util.FormValidation;

import java.util.ArrayList;
import java.util.List;

import javaposse.jobdsl.dsl.Context;

import com.google.common.base.Preconditions;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ExecutionConfig;
import de.tracetronic.jenkins.plugins.ecutest.test.config.GlobalConstant;
import de.tracetronic.jenkins.plugins.ecutest.test.config.TestConfig;
import de.tracetronic.jenkins.plugins.ecutest.util.validation.TestValidator;

/**
 * Common base class providing test-related DSL extensions.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public abstract class AbstractTestBuilderDslExtension extends AbstractDslExtension {

    /**
     * Option name for the test file.
     */
    protected static final String OPT_TEST_FILE = "testFile";

    /**
     * Validator to check test related DSL options.
     */
    protected final TestValidator validator = new TestValidator();

    /**
     * {@link Context} class providing common test related methods for the nested DSL context.
     */
    public abstract class AbstractTestContext implements Context {

        /**
         * The test configuration settings.
         */
        protected TestConfig testConfig;

        /**
         * The test execution settings.
         */
        protected ExecutionConfig executionConfig;

        /**
         * Option defining the test configuration.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void testConfig(final Runnable closure) {
            final TestConfigContext context = new TestConfigContext();
            executeInContext(closure, context);
            testConfig = new TestConfig(context.tbcFile, context.tcfFile, context.forceReload, context.loadOnly,
                    context.constants);
        }

        /**
         * Option defining the test execution configuration.
         *
         * @param closure
         *            the nested Groovy closure
         */
        public void executionConfig(final Runnable closure) {
            final ExecutionConfigContext context = new ExecutionConfigContext();
            executeInContext(closure, context);
            executionConfig = new ExecutionConfig(context.timeout, context.stopOnError, context.checkTestFile);
        }

        /**
         * {@link Context} class providing test configuration methods for the nested DSL context.
         */
        public class TestConfigContext implements Context {

            private static final String OPT_TBC_FILE = "tbcFile";
            private static final String OPT_TCF_FILE = "tcfFile";
            private static final String OPT_CONSTANT_NAME = "constant name";
            private static final String OPT_CONSTANT_VALUE = "constant value";

            private String tbcFile;
            private String tcfFile;
            private boolean forceReload;
            private boolean loadOnly;
            private List<GlobalConstant> constants;

            /**
             * Option defining the test bench configuration file.
             *
             * @param value
             *            the value
             */
            public void tbcFile(final CharSequence value) {
                Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TBC_FILE);
                final FormValidation validation = validator.validateTbcFile(value.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                tbcFile = value.toString();
            }

            /**
             * Option defining the test configuration file.
             *
             * @param value
             *            the value
             */
            public void tcfFile(final CharSequence value) {
                Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TCF_FILE);
                final FormValidation validation = validator.validateTcfFile(value.toString());
                Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                tcfFile = value.toString();
            }

            /**
             * Option defining whether to force reloading the current test configuration.
             *
             * @param value
             *            the value
             */
            public void forceReload(final boolean value) {
                forceReload = value;
            }

            /**
             * Option defining whether to load the test configuration only.
             *
             * @param value
             *            the value
             */
            public void loadOnly(final boolean value) {
                loadOnly = value;
            }

            /**
             * Option defining the global constants.
             *
             * @param closure
             *            the nested Groovy closure
             */
            public void constants(final Runnable closure) {
                final GlobalConstantsContext context = new GlobalConstantsContext();
                executeInContext(closure, context);
                constants = context.constants;
            }

            /**
             * {@link Context} class providing global constants methods for the nested DSL context.
             */
            public class GlobalConstantsContext implements Context {

                private final List<GlobalConstant> constants = new ArrayList<GlobalConstant>();

                /**
                 * Option defining the global constant.
                 *
                 * @param name
                 *            the global constant name
                 * @param value
                 *            the global constant value
                 */
                public void constant(final CharSequence name, final CharSequence value) {
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_CONSTANT_NAME);
                    Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_CONSTANT_VALUE);

                    FormValidation validation = validator.validateGlobalConstantName(name.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());
                    validation = validator.validateGlobalConstantValue(value.toString());
                    Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR, validation.getMessage());

                    constants.add(new GlobalConstant(name.toString(), value.toString()));
                }

                /**
                 * Option defining the global constant.
                 *
                 * @param closure
                 *            the nested Groovy closure
                 */
                public void constant(final Runnable closure) {
                    final GlobalConstantContext context = new GlobalConstantContext();
                    executeInContext(closure, context);
                    constants.add(new GlobalConstant(context.name, context.value));
                }

                /**
                 * {@link Context} class providing single global constant methods for the nested DSL context.
                 */
                public class GlobalConstantContext implements Context {

                    private String name;
                    private String value;

                    /**
                     * Option defining the global constant name.
                     *
                     * @param value
                     *            the value
                     */
                    public void name(final CharSequence value) {
                        Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_CONSTANT_NAME);
                        final FormValidation validation = validator.validateGlobalConstantName(value.toString());
                        Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                                validation.getMessage());
                        name = value.toString();
                    }

                    /**
                     * Option defining the global constant value.
                     *
                     * @param value
                     *            the value
                     */
                    public void value(final CharSequence value) {
                        Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_CONSTANT_VALUE);
                        final FormValidation validation = validator.validateGlobalConstantValue(value.toString());
                        Preconditions.checkArgument(validation.kind != FormValidation.Kind.ERROR,
                                validation.getMessage());
                        this.value = value.toString();
                    }
                }
            }
        }

        /**
         * {@link Context} class providing test execution methods for the nested DSL context.
         */
        public class ExecutionConfigContext implements Context {

            private String timeout;
            private boolean stopOnError = true;
            private boolean checkTestFile = true;

            /**
             * Option defining the timeout.
             *
             * @param value
             *            the value as String
             */
            public void timeout(final CharSequence value) {
                Preconditions.checkNotNull(value, NOT_NULL_MSG, OPT_TIMEOUT);
                final FormValidation validation = validator.validateTimeout(value.toString(),
                        ExecutionConfig.getDefaultTimeout());
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

            /**
             * Option defining whether to stop ECU-TEST and Tool-Server instances if an error occurred.
             *
             * @param value
             *            the value
             */
            public void stopOnError(final boolean value) {
                stopOnError = value;
            }

            /**
             * Option defining whether to pre-check the package and project files.
             *
             * @param value
             *            the value
             */
            public void checkTestFile(final boolean value) {
                checkTestFile = value;
            }
        }
    }
}
