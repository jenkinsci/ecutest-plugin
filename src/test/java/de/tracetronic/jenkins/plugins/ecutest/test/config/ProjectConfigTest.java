/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.test.config;

import de.tracetronic.jenkins.plugins.ecutest.test.config.ProjectConfig.JobExecutionMode;
import hudson.EnvVars;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ProjectConfig}.
 */
public class ProjectConfigTest {

    @Test
    public void testNullConstructor() {
        final ProjectConfig config = new ProjectConfig(true, null, JobExecutionMode.SEQUENTIAL_EXECUTION);
        assertTrue(config.isExecInCurrentPkgDir());
        assertThat(config.getFilterExpression(), is(""));
        assertThat(config.getJobExecMode(), is(JobExecutionMode.SEQUENTIAL_EXECUTION));
    }

    @Test
    public void testExpand() {
        final ProjectConfig config = new ProjectConfig(true, "${FILTER}", JobExecutionMode.SEQUENTIAL_EXECUTION);
        final EnvVars envVars = new EnvVars();
        envVars.put("FILTER", "filter");
        assertThat(config.expand(envVars).getFilterExpression(), is("filter"));
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ProjectConfig.class).verify();
    }
}
