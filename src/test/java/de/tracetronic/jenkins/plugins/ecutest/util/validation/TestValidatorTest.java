/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TestValidator}.
 */
public class TestValidatorTest {

    private TestValidator testValidator;

    @Before
    public void setUp() throws Exception {
        testValidator = new TestValidator();
    }

    // Validation of package file
    @Test
    public void testEmptyPackageFile() {
        final FormValidation validation = testValidator.validatePackageFile("");
        assertEquals("Error if empty package file", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullPackageFile() {
        final FormValidation validation = testValidator.validatePackageFile(null);
        assertEquals("Error if package file not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedPackageFile() {
        final FormValidation validation = testValidator.validatePackageFile("${TEST}");
        assertEquals("Warning if parameterized package file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidPackageFile() {
        final FormValidation validation = testValidator.validatePackageFile("test.pkg");
        assertEquals("Valid package file has .pkg extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidPackageFile() {
        final FormValidation validation = testValidator.validatePackageFile("test");
        assertEquals("Error if invalid package file", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of project file
    @Test
    public void testEmptyProjectFile() {
        final FormValidation validation = testValidator.validateProjectFile("");
        assertEquals("Error if empty project file", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullProjectFile() {
        final FormValidation validation = testValidator.validateProjectFile(null);
        assertEquals("Error if project file not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedProjectFile() {
        final FormValidation validation = testValidator.validateProjectFile("${TEST}");
        assertEquals("Warning if parameterized project file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidProjectFile() {
        final FormValidation validation = testValidator.validateProjectFile("test.prj");
        assertEquals("Valid project file has .prj extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidProjectFile() {
        final FormValidation validation = testValidator.validateProjectFile("test");
        assertEquals("Error if invalid project file", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of test folder
    @Test
    public void testEmptyTestFolder() {
        final FormValidation validation = testValidator.validateTestFolder("");
        assertEquals("Error if empty test folder", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullTestFolder() {
        final FormValidation validation = testValidator.validateTestFolder(null);
        assertEquals("Error if test folder not defined", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedTestFolder() {
        final FormValidation validation = testValidator.validateTestFolder("${FOLDER}");
        assertEquals("Warning if parameterized test folder", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidTestFolder() {
        final FormValidation validation = testValidator.validateTestFolder("test");
        assertEquals("Valid test folder", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of TBC file
    @Test
    public void testEmptyTbcFile() {
        final FormValidation validation = testValidator.validateTbcFile("");
        assertEquals("Warning if empty tbc file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNullTbcFile() {
        final FormValidation validation = testValidator.validateTbcFile(null);
        assertEquals("Warning if tbc file not defined", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedTbcFile() {
        final FormValidation validation = testValidator.validateTbcFile("${TBC}");
        assertEquals("Warning if parameterized tbc file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidTbcFile() {
        final FormValidation validation = testValidator.validateTbcFile("test.tbc");
        assertEquals("Valid tbc file has .tbc extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidTbcFile() {
        final FormValidation validation = testValidator.validateTbcFile("test");
        assertEquals("Error if invalid tbc file", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of TCF file
    @Test
    public void testEmptyTcfFile() {
        final FormValidation validation = testValidator.validateTcfFile("");
        assertEquals("Warning if empty tcf file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testNullTcfFile() {
        final FormValidation validation = testValidator.validateTcfFile(null);
        assertEquals("Warning if tcf file not defined", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testParameterizedTcfFile() {
        final FormValidation validation = testValidator.validateTcfFile("${TCF}");
        assertEquals("Warning if parameterized tcf file", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidTcfFile() {
        final FormValidation validation = testValidator.validateTcfFile("test.tcf");
        assertEquals("Valid tcf file has .tcf extension", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidTcfFile() {
        final FormValidation validation = testValidator.validateTcfFile("test");
        assertEquals("Error if invalid tcf file", FormValidation.Kind.ERROR, validation.kind);
    }

    // Validation of filter expression
    @Test
    public void testEmptyExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("");
        assertEquals("Valid if empty filter expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNullExpression() {
        final FormValidation validation = testValidator.validateFilterExpression(null);
        assertEquals("Valid if no filter expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testParameterizedExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("${FILTER}");
        assertEquals("Warning if parameterized filter expression", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testWildCardExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("'test*123?abc'");
        assertEquals("Valid if wildcards used", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testEqualsOpStringExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name='test'");
        assertEquals("Valid if equals operator used with string", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testUnequalsOpStringExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name != 'test'");
        assertEquals("Valid if unequals operator used with string", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testEqualsOpNumberExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("'Estimated Duration [min]'=1");
        assertEquals("Valid if equals operator used with number", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testEqualsOpBooleanExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("VersionCounter=True");
        assertEquals("Valid if equals operator used with boolean", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testRelOpNumberExpression() {
        FormValidation validation = testValidator.validateFilterExpression("VersionCounter < 1");
        assertEquals("Valid if less than operator used with number", FormValidation.Kind.OK, validation.kind);
        validation = testValidator.validateFilterExpression("VersionCounter > 1");
        assertEquals("Valid if greater than operator used with number", FormValidation.Kind.OK, validation.kind);
        validation = testValidator.validateFilterExpression("VersionCounter <= 1");
        assertEquals("Valid if less than equals operator used with number", FormValidation.Kind.OK, validation.kind);
        validation = testValidator.validateFilterExpression("VersionCounter >= 1");
        assertEquals("Valid if greater than equals operator used with number", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testRelOpNotNumberExpression() {
        FormValidation validation = testValidator.validateFilterExpression("Name > 'test'");
        assertEquals("Invalid if relational operator used with string", FormValidation.Kind.WARNING, validation.kind);
        validation = testValidator.validateFilterExpression("VersionCounter >= False'");
        assertEquals("Invalid if relational operator used with boolean", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testStringExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name='test'");
        assertEquals("Valid string filter expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testBooleanFilterExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("VersionCounter=True");
        assertEquals("Valid boolean filter expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testNumericFilterExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("'Estimated Duration [min]'=1");
        assertEquals("Valid numeric filter expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testHasOpStringExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name has 'test'");
        assertEquals("Valid if has operator used with string", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testHasnotOpStringExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name hasnot 'test'");
        assertEquals("Valid if hasnot operator used with string", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testHasOpNonStringExpression() {
        FormValidation validation = testValidator.validateFilterExpression("VersionCounter has 1");
        assertEquals("Invalid if has operator used with number", FormValidation.Kind.WARNING, validation.kind);
        validation = testValidator.validateFilterExpression("VersionCounter has True");
        assertEquals("Invalid if has operator used with boolean", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testSingleQuotedExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("'Test Comment' = 'test'");
        assertEquals("Valid if single quotes used with string", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testDoubleQuotedExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name = \"test\"");
        assertEquals("Valid if double quotes used with string", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testMissingQuoteExpression() {
        FormValidation validation = testValidator.validateFilterExpression("Name = 'test");
        assertEquals("Invalid if closing quotation mark is missing", FormValidation.Kind.WARNING, validation.kind);
        validation = testValidator.validateFilterExpression("Name = test\"");
        assertEquals("Invalid if opening quotation mark is missing", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testMissingBracketExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("(Name='test'");
        assertEquals("Error if bracket missing in filter expression", FormValidation.Kind.WARNING, validation.kind);
    }

    @Ignore("Regression since antlr 4.7")
    @Test
    public void testMissingLogicOperatorExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("Name='test' Status='FAILED'");
        assertEquals("Error if logical operator missing in filter expression", FormValidation.Kind.WARNING,
            validation.kind);
    }

    @Test
    public void testValidComplexExpression() {
        final FormValidation validation = testValidator
            .validateFilterExpression("(Name=\"test\" and (Status='FAILED' or 'Test Comment' has 't?st*') "
                + "or 'Execution Priority'>=1 and VersionCounter=True)");
        assertEquals("Valid complex filter expression", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidExpression() {
        final FormValidation validation = testValidator.validateFilterExpression("invalid filter");
        assertEquals("Error if invalid filter expression", FormValidation.Kind.WARNING, validation.kind);
    }

    // Validation of package parameters
    @Test
    public void testEmptyParameterName() {
        final FormValidation validation = testValidator.validateParameterName("");
        assertEquals("Error if empty parameter name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullParameterName() {
        final FormValidation validation = testValidator.validateParameterName(null);
        assertEquals("Error if empty parameter name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedParameterName() {
        final FormValidation validation = testValidator.validateParameterName("${PARAM}");
        assertEquals("Warning if parameterized parameter name", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidParameterName() {
        final FormValidation validation = testValidator.validateParameterName("param_123");
        assertEquals("Valid parameter name starts with a letter char", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidParameterName() {
        final FormValidation validation = testValidator.validateParameterName("123param");
        assertEquals("Invalid parameter name starts with non-letter char", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testInvalidAlphanumericParameterName() {
        final FormValidation validation = testValidator.validateGlobalConstantName("!param123");
        assertEquals("Error if parameter name is not alphanumeric", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testEmptyParameterValue() {
        final FormValidation validation = testValidator.validateParameterValue("");
        assertEquals("Error if empty parameter value", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullParameterValue() {
        final FormValidation validation = testValidator.validateParameterValue(null);
        assertEquals("Error if empty parameter value", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedParameterValue() {
        final FormValidation validation = testValidator.validateParameterValue("${VALUE}");
        assertEquals("Warning if parameterized parameter value", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidParameterValue() {
        final FormValidation validation = testValidator.validateParameterValue("value");
        assertEquals("Valid parameter value starts with a letter char", FormValidation.Kind.OK, validation.kind);
    }

    // Validation of global constants parameters
    @Test
    public void testEmptyConstantName() {
        final FormValidation validation = testValidator.validateGlobalConstantName("");
        assertEquals("Error if empty global constant name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullConstantName() {
        final FormValidation validation = testValidator.validateGlobalConstantName(null);
        assertEquals("Error if empty global constant name", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedConstantName() {
        final FormValidation validation = testValidator.validateGlobalConstantName("${CONSTANT}");
        assertEquals("Warning if parameterized global constant name", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidConstantName() {
        final FormValidation validation = testValidator.validateGlobalConstantName("constant_123");
        assertEquals("Valid global constant name starts with a letter char", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidConstantName() {
        final FormValidation validation = testValidator.validateGlobalConstantName("123constant");
        assertEquals("Invalid global constant name starts with non-letter char", FormValidation.Kind.ERROR,
            validation.kind);
    }

    @Test
    public void testInvalidAlphanumericConstantName() {
        final FormValidation validation = testValidator.validateGlobalConstantName("!constant123");
        assertEquals("Error if global constant name is not alphanumeric", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testEmptyConstantValue() {
        final FormValidation validation = testValidator.validateGlobalConstantValue("");
        assertEquals("Error if empty global constant value", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testNullConstantValue() {
        final FormValidation validation = testValidator.validateGlobalConstantValue(null);
        assertEquals("Error if empty global constant value", FormValidation.Kind.ERROR, validation.kind);
    }

    @Test
    public void testParameterizedConstantValue() {
        final FormValidation validation = testValidator.validateGlobalConstantValue("${VALUE}");
        assertEquals("Warning if parameterized global constant value", FormValidation.Kind.WARNING, validation.kind);
    }

    @Test
    public void testValidConstantValue() {
        final FormValidation validation = testValidator.validateGlobalConstantValue("value");
        assertEquals("Valid global constant value starts with a letter char", FormValidation.Kind.OK, validation.kind);
    }
}
