/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.ETComProperty;
import hudson.Extension;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Contributes additional settings for the {@link ETInstallation}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETToolProperty extends ToolProperty<ETInstallation> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String progId;
    private final int timeout;

    /**
     * Instantiates a new {@link ETToolProperty}.
     *
     * @param progId  the programmatic identifier
     * @param timeout the timeout
     */
    @DataBoundConstructor
    public ETToolProperty(final String progId, final int timeout) {
        super();
        this.progId = StringUtils.defaultIfBlank(progId, ETComProperty.DEFAULT_PROG_ID);
        this.timeout = timeout;
    }

    /**
     * Gets the ECU-TEST COM specific programmatic identifier.
     *
     * @return the progId
     */
    public String getProgId() {
        return progId;
    }

    /**
     * Gets the current maximum COM response timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
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
            return ETComProperty.DEFAULT_PROG_ID;
        }

        /**
         * @return the default COM response timeout
         */
        public static int getDefaultTimeout() {
            return ETComProperty.DEFAULT_TIMEOUT;
        }

        /**
         * Validates the ECU-TEST specific programmatic identifier.
         *
         * @param value the programmatic identifier
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.ETToolProperty_DisplayName();
        }
    }
}
