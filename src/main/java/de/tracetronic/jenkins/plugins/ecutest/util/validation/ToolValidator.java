/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import de.tracetronic.jenkins.plugins.ecutest.tool.Messages;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;
import hudson.util.FormValidation;
import hudson.util.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Validator to check tool related form fields.
 */
public class ToolValidator extends AbstractValidator {

    /**
     * Validates the workspace directory.
     *
     * @param wsDir the workspace directory.
     * @return the form validation
     */
    public FormValidation validateWorkspaceDir(final String wsDir) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isBlank(wsDir) && wsDir.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        }
        return returnValue;
    }

    /**
     * Validates the settings directory.
     *
     * @param settingsDir the settings directory.
     * @return the form validation
     */
    public FormValidation validateSettingsDir(final String settingsDir) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isBlank(settingsDir) && settingsDir.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        }
        return returnValue;
    }

    /**
     * Validates the ToolLibs.ini path.
     *
     * @param toolLibsIni the ToolLibs.ini path
     * @return the form validation
     */
    public FormValidation validateToolLibsIni(final String toolLibsIni) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isBlank(toolLibsIni)) {
            if (toolLibsIni.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
            } else {
                if (!IOUtils.isAbsolute(toolLibsIni) || FilenameUtils.getExtension(toolLibsIni).isEmpty()) {
                    returnValue = FormValidation.error(Messages.StartTSBuilder_NoAbsolutePath());
                }
            }
        }
        return returnValue;
    }

    /**
     * Validates the TCP port.
     *
     * @param tcpPort the TCP port
     * @return the form validation
     */
    public FormValidation validateTcpPort(final String tcpPort) {
        FormValidation returnValue;
        if (StringUtils.isBlank(tcpPort)) {
            returnValue = FormValidation.warning(Messages
                .StartTSBuilder_NoTcpPort(StartTSBuilder.DEFAULT_TCP_PORT));
        } else if (tcpPort.contains(PARAMETER)) {
            returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
        } else {
            returnValue = FormValidation.validatePositiveInteger(tcpPort);
        }
        return returnValue;
    }
}
