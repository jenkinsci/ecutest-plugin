/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.jobdsl;

import org.junit.Test;

/**
 * Integration tests for parameterized {@link TestBuilderDslExtension}.
 */
public class TestBuilderParamDslExtensionIT extends TestBuilderDslExtensionIT {

    public static final String JOB_NAME = "testBuilderParam";
    public static final String SCRIPT_NAME = "testBuilderParam.groovy";

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
    public void testPackageWithParamDsl() throws Exception {
        testPackageWithDsl();
    }

    @Test
    public void testProjectWithParamDsl() throws Exception {
        testProjectWithDsl();
    }

    @Test
    public void testFolderWithParamDsl() throws Exception {
        testFolderWithDsl();
    }

    @Test
    public void testImportPackageWithParamDsl() throws Exception {
        testImportPackageWithDsl();
    }

    @Test
    public void testImportProjectWithParamDsl() throws Exception {
        testImportProjectWithDsl();
    }
}
