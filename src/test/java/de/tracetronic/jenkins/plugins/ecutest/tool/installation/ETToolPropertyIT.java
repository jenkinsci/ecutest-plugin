/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;
import hudson.util.FormValidation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for {@link ETToolProperty}.
 */
public class ETToolPropertyIT extends IntegrationTestBase {

    @Test
    public void testEmptyProdId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
            .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("");
        assertEquals("Valid if empty ProgID", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testDefaultProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
            .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("ecu.test.Application");
        assertEquals("Valid if default ProgID", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testVersionedProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
            .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("ecu.test.Application.2024.1");
        assertEquals("Valid if versioned new ProgID for >= ecu.test 2024.1", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testOldProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
            .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("ECU-TEST.Application");
        assertEquals("Valid if new ProgID for >= ecu.test 2024.1", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testVersionedOldProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
            .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("ECU-TEST.Application.2023.1");
        assertEquals("Valid if versioned ProgID", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
            .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("invalid");
        assertEquals("Error if invalid ProgID", FormValidation.Kind.ERROR, validation.kind);
    }
}
