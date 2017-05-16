/*
 * Copyright (c) 2015-2017 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.test.Messages;

/**
 * Validator to check project exporter related form fields.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TMSValidator extends AbstractValidator {

    /**
     * Validates the package file.
     *
     * @param testFile
     *            the test file
     * @return the form validation
     */
    public FormValidation validatePackageFile(final String testFile) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(testFile)) {
            returnValue = FormValidation.validateRequired(testFile);
        } else if (testFile.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!testFile.endsWith(".pkg")) {
            returnValue = FormValidation.error(Messages.TestBuilder_PkgFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the project file.
     *
     * @param testFile
     *            the test file
     * @return the form validation
     */
    public FormValidation validateProjectFile(final String testFile) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(testFile)) {
            returnValue = FormValidation.validateRequired(testFile);
        } else if (testFile.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!testFile.endsWith(".prj")) {
            returnValue = FormValidation.error(Messages.TestBuilder_PrjFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the export target path.
     *
     * @param exportPath
     *            the export path
     * @return the form validation
     */
    public FormValidation validateExportPath(final String exportPath) {
        return validateRequiredParamValue(exportPath);
    }

    /**
     * Validates the test path to import.
     *
     * @param testPath
     *            the test path to import
     * @return the form validation
     */
    public FormValidation validateTestPath(final String testPath) {
        return validateRequiredParamValue(testPath);
    }

    /**
     * Validates the project archive path to import.
     *
     * @param archivePath
     *            the project archive to import
     * @return the form validation
     */
    public FormValidation validateArchivePath(final String archivePath) {
        FormValidation returnValue = FormValidation.ok();
        if (StringUtils.isBlank(archivePath)) {
            returnValue = FormValidation.validateRequired(archivePath);
        } else if (archivePath.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else if (!archivePath.endsWith(".prz")) {
            returnValue = FormValidation.error(Messages.ImportProjectBuilder_PrzFileExtension());
        }
        return returnValue;
    }

    /**
     * Validates the import target path.
     *
     * @param importPath
     *            the import path
     * @return the form validation
     */
    public FormValidation validateImportPath(final String importPath) {
        return validateParameterizedValue(importPath);
    }

    /**
     * Validates the import configuration target path.
     *
     * @param importConfigPath
     *            the import configuration path
     * @return the form validation
     */
    public FormValidation validateImportConfigPath(final String importConfigPath) {
        return validateParameterizedValue(importConfigPath);
    }
}
