/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import org.junit.Test;

/**
 * Integration tests for parameterized {@link ToolBuilderDslExtension}.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ToolBuilderParamDslExtensionIT extends ToolBuilderDslExtensionIT {

    public static final String JOB_NAME = "toolBuilderParam";
    public static final String SCRIPT_NAME = "toolBuilderParam.groovy";

    @Override
    protected String getJobName() {
        return JOB_NAME;
    }

    @Override
    protected String getDslScript() {
        return SCRIPT_NAME;
    }

    @Test
    public void testBuildersWithParamDsl() throws Exception {
        testBuildersWithDsl();
    }

    @Test
    public void testStartETWithParamDsl() throws Exception {
        testStartETWithDsl();
    }

    @Test
    public void testStopETWithParamDsl() throws Exception {
        testStopETWithDsl();
    }

    @Test
    public void testStartTSWithParamDsl() throws Exception {
        testStartTSWithDsl();
    }

    @Test
    public void testStopTSWithParamDsl() throws Exception {
        testStopTSWithDsl();
    }
}
