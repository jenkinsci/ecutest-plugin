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
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import hudson.Extension;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.FormValidation;

import java.io.Serializable;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProgId;

/**
 * Contributes additional settings for the {@link ETInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETToolProperty extends ToolProperty<ETInstallation> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String progId;

    /**
     * Instantiates a new {@link ETToolProperty}.
     *
     * @param progId
     *            the programmatic identifier
     */
    @DataBoundConstructor
    public ETToolProperty(final String progId) {
        super();
        this.progId = StringUtils.defaultIfBlank(progId, String.valueOf(ETComProgId.DEFAULT_PROG_ID));
    }

    /**
     * Gets the ECU-TEST COM specific programmatic identifier.
     *
     * @return the progId
     */
    public String getProgId() {
        return progId;
    }

    @Override
    public Class<ETInstallation> type() {
        return ETInstallation.class;
    }

    /**
     * DescriptorImpl for {@link ETToolProperty}.
     */
    @Extension
    public static final class DescriptorImpl extends ToolPropertyDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(ETToolProperty.class);
            load();
        }

        /**
         * @return the default COM specific programmatic identifier
         */
        public static String getDefaultProgId() {
            return ETComProgId.DEFAULT_PROG_ID;
        }

        /**
         * Validates the ECU-TEST specific programmatic identifier.
         *
         * @param value
         *            the programmatic identifier
         * @return the form validation
         */
        public FormValidation doCheckProgId(@QueryParameter final String value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            FormValidation returnValue = FormValidation.ok();
            if (!StringUtils.isEmpty(value)) {
                final String pattern = "^(ECU-TEST\\.Application(\\.\\d+.\\d+)?|ECU-TEST6?\\.Application)";
                if (!Pattern.matches(pattern, value)) {
                    returnValue = FormValidation.error(Messages.ETToolProperty_InvalidProgID(value));
                }
            }
            return returnValue;
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ETToolProperty_DisplayName();
        }
    }
}
