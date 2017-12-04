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
package de.tracetronic.jenkins.plugins.ecutest.tool.installation;

import static org.junit.Assert.assertEquals;
import hudson.util.FormValidation;

import org.junit.Test;

import de.tracetronic.jenkins.plugins.ecutest.IntegrationTestBase;

/**
 * Integration tests for {@link ETToolProperty}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
        final FormValidation validation = toolDescriptor.doCheckProgId("ECU-TEST.Application");
        assertEquals("Valid if default ProgID", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testVersionedProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
                .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("ECU-TEST.Application.6.5");
        assertEquals("Valid if versioned ProgID", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testSpecialProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
                .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("ECU-TEST6.Application");
        assertEquals("Valid if special ProgID for ECU-TEST 6", FormValidation.Kind.OK, validation.kind);
    }

    @Test
    public void testInvalidProgId() throws Exception {
        final ETToolProperty.DescriptorImpl toolDescriptor = jenkins.jenkins
                .getDescriptorByType(ETToolProperty.DescriptorImpl.class);
        final FormValidation validation = toolDescriptor.doCheckProgId("invalid");
        assertEquals("Error if invalid ProgID", FormValidation.Kind.ERROR, validation.kind);
    }
}
