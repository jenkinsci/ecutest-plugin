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
package de.tracetronic.jenkins.plugins.ecutest.util.validation;

import hudson.util.FormValidation;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import de.tracetronic.jenkins.plugins.ecutest.tool.Messages;
import de.tracetronic.jenkins.plugins.ecutest.tool.StartTSBuilder;

/**
 * Validator to check tool-related form fields.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolValidator extends AbstractValidator {

    /**
     * Validates the workspace directory.
     *
     * @param wsDir
     *            the workspace directory.
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
     * Validates the ToolLibs.ini path.
     *
     * @param toolLibsIni
     *            the ToolLibs.ini path
     * @return the form validation
     */
    public FormValidation validateToolLibsIni(final String toolLibsIni) {
        FormValidation returnValue = FormValidation.ok();
        if (!StringUtils.isBlank(toolLibsIni)) {
            if (toolLibsIni.contains(PARAMETER)) {
                returnValue = FormValidation.warning(Messages.Builder_NoValidatedValue());
            } else {
                final File file = new File(toolLibsIni);
                if (!file.isAbsolute() || file.isDirectory()) {
                    returnValue = FormValidation.error(Messages.StartTSBuilder_NoAbsolutePath());
                }
            }
        }

        return returnValue;
    }

    /**
     * Validates the TCP port.
     *
     * @param tcpPort
     *            the TCP port
     * @return the form validation
     */
    public FormValidation validateTcpPort(final String tcpPort) {
        FormValidation returnValue = FormValidation.ok();
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
