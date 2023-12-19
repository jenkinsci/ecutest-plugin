/*
 * Copyright (c) 2015-2023 tracetronic GmbH
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
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Contributes additional settings for the {@link ETInstallation}.
 */
public class ETToolProperty extends ToolProperty<ETInstallation> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String progId;
    private final int timeout;
    /**
     * Specifies whether to register the COM server before each start of ecu.test.
     *
     * @since 2.13.0
     */
    private final boolean registerComServer;

    /**
     * Instantiates a new {@link ETToolProperty}.
     *
     * @param progId            the ecu.test COM specific programmatic identifier
     * @param timeout           the current maximum COM response timeout
     * @param registerComServer specifies whether to register the COM server before each start of ecu.test
     */
    @DataBoundConstructor
    public ETToolProperty(final String progId, final int timeout, final boolean registerComServer) {
        super();
        this.progId = StringUtils.defaultIfBlank(progId, ETComProperty.DEFAULT_PROG_ID);
        this.timeout = timeout;
        this.registerComServer = registerComServer;
    }

    public String getProgId() {
        return progId;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isRegisterComServer() {
        return registerComServer;
    }

    @Override
    public Class<ETInstallation> type() {
        return ETInstallation.class;
    }

    /**
     * DescriptorImpl for {@link ETToolProperty}.
     */
    @Symbol("ecuTestProperty")
    @Extension
    public static final class DescriptorImpl extends ToolPropertyDescriptor {

        /**
         * Instantiates a new {@link DescriptorImpl}.
         */
        public DescriptorImpl() {
            super(ETToolProperty.class);
            load();
        }

        public static String getDefaultProgId() {
            return ETComProperty.DEFAULT_PROG_ID;
        }

        public static int getDefaultTimeout() {
            return ETComProperty.DEFAULT_TIMEOUT;
        }

        /**
         * Validates the ecu.test specific programmatic identifier.
         *
         * @param value the programmatic identifier
         * @return the form validation
         */
        public FormValidation doCheckProgId(@QueryParameter final String value) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
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
        public boolean configure(final StaplerRequest req, final JSONObject json) {
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
