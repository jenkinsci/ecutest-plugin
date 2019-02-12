/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.filter.RefFilterLexer;
import de.tracetronic.jenkins.plugins.ecutest.filter.RefFilterParser;
import de.tracetronic.jenkins.plugins.ecutest.test.Messages;
import hudson.util.FormValidation;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator to check test related form fields.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TestValidator extends AbstractValidator {

    /**
     * Validates the test folder.
     *
     * @param testFolder the test folder
     * @return the form validation
     */
    public FormValidation validateTestFolder(final String testFolder) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(testFolder)) {
            returnValue = FormValidation.validateRequired(testFolder);
        } else if (testFolder.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        }
        return returnValue;
    }

    /**
     * Validates the TBC file.
     *
     * @param tbcFile the TBC file
     * @return the form validation
     */
    public FormValidation validateTbcFile(final String tbcFile) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(tbcFile)) {
            returnValue = FormValidation.warning(Messages.TestBuilder_NoTbcFile());
        } else if (tbcFile.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!tbcFile.endsWith(".tbc")) {
            returnValue = FormValidation.error(Messages.TestBuilder_TbcFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the TCF file.
     *
     * @param tcfFile the TCF file
     * @return the form validation
     */
    public FormValidation validateTcfFile(final String tcfFile) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(tcfFile)) {
            returnValue = FormValidation.warning(Messages.TestBuilder_NoTcfFile());
        } else if (tcfFile.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!tcfFile.endsWith(".tcf")) {
            returnValue = FormValidation.error(Messages.TestBuilder_TcfFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the parameter name.
     *
     * @param name the parameter name
     * @return FormValidation
     */
    public FormValidation validateParameterName(final String name) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(name)) {
            returnValue = FormValidation.validateRequired(name);
        } else if (name.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!isValidVariableName(name)) {
            returnValue = FormValidation.error(Messages.PackageParameter_InvalidName());
        }
        return returnValue;
    }

    /**
     * Validates the parameter value.
     *
     * @param value the parameter value
     * @return FormValidation
     */
    public FormValidation validateParameterValue(final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            returnValue = FormValidation.validateRequired(value);
        } else if (value.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        }
        return returnValue;
    }

    /**
     * Validates the global constant name.
     *
     * @param name the global constant name
     * @return FormValidation
     */
    public FormValidation validateGlobalConstantName(final String name) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(name)) {
            returnValue = FormValidation.validateRequired(name);
        } else if (name.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!isValidVariableName(name)) {
            returnValue = FormValidation.error(Messages.GlobalConstant_InvalidName());
        }
        return returnValue;
    }

    /**
     * Validates the global constant value.
     *
     * @param value the global constant value
     * @return FormValidation
     */
    public FormValidation validateGlobalConstantValue(final String value) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(value)) {
            returnValue = FormValidation.validateRequired(value);
        } else if (value.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        }
        return returnValue;
    }

    /**
     * Validates the filter expression.
     *
     * @param filterExpression the filter expression
     * @return the form validation
     */
    public FormValidation validateFilterExpression(final String filterExpression) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isBlank(filterExpression)) {
            if (filterExpression.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
            } else {
                final String trimmedExpression = StringUtils.trimToEmpty(filterExpression).replaceAll("^\\(\\s*", "(")
                    .replaceAll("\\s*\\)", ")");
                final FilterExpressionValidator validator = new FilterExpressionValidator(trimmedExpression);
                validator.validate();
                if (!validator.isValid()) {
                    returnValue = FormValidation.warning(Messages.TestProjectBuilder_InvalidFilterExpression());
                }
            }
        }
        return returnValue;
    }

    /**
     * Checks if the variable name contains valid characters only.
     *
     * @param name the variable name
     * @return {@code true} if variable name is valid, {@code false} otherwise
     */
    private boolean isValidVariableName(final String name) {
        final Pattern pattern = Pattern.compile("^[a-zA-Z_][\\w]*$");
        final Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    /**
     * Validates filter expression with use of a ANTLR grammar.
     */
    private static final class FilterExpressionValidator {

        private final String expression;
        private boolean isValid;

        /**
         * Instantiates a new {@link FilterExpressionValidator}.
         *
         * @param expression the expression to validate
         */
        FilterExpressionValidator(final String expression) {
            this.expression = expression;
        }

        /**
         * Validates the expression by checking lexical and parser errors.
         */
        public void validate() {
            isValid = true;

            final RefFilterLexer lexer = new RefFilterLexer(new ANTLRInputStream(expression));
            final RefFilterParser parser = new RefFilterParser(new CommonTokenStream(lexer));

            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {

                @Override
                public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
                                        final int line, final int charPositionInLine,
                                        final String msg, final RecognitionException e) {
                    isValid = false;
                }
            });

            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {

                @Override
                public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
                                        final int line, final int charPositionInLine,
                                        final String msg, final RecognitionException e) {
                    isValid = false;
                }
            });

            parser.filterExpression();
        }

        /**
         * @return {@code true} if expression is valid, {@code false} otherwise.
         */
        public boolean isValid() {
            return isValid;
        }
    }
}
