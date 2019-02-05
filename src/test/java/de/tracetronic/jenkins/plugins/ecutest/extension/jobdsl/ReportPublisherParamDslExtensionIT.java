/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import org.junit.Test;

/**
 * Integration tests for {@link ReportPublisherDslExtension}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ReportPublisherParamDslExtensionIT extends ReportPublisherDslExtensionIT {

    public static final String JOB_NAME = "reportPublisherParam";
    public static final String SCRIPT_NAME = "reportPublisherParam.groovy";

    @Override
    protected String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected String getDslScript() {
        return SCRIPT_NAME;
    }

    @Test
    public void testPublishersWithParamDsl() throws Exception {
        testPublishersWithDsl();
    }

    @Test
    public void testPublishATXWithParamDsl() throws Exception {
        testPublishATXWithDsl();
    }

    @Test
    public void testTRFPublisherWithParamDsl() throws Exception {
        testTRFPublisherWithDsl();
    }

    @Test
    public void testUNITPublisherWithParamDsl() throws Exception {
        testUNITPublisherWithDsl();
    }

    @Test
    public void testETLogPublisherWithParamDsl() throws Exception {
        testETLogPublisherWithDsl();
    }

    @Test
    public void testGeneratorsPublisherWithParamDsl() throws Exception {
        testGeneratorPublisherWithDsl();
    }

    @Test
    public void testTMSPublisherWithParamDsl() throws Exception {
        testTMSPublisherWithDsl();
    }
}
