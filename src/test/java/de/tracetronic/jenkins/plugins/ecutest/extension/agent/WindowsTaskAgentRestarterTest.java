/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.agent;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class WindowsTaskAgentRestarterTest {

    @Test
    public void taskName() {
        assertThat(new WindowsTaskAgentRestarter().getTaskName(), is("RESTART_JENKINS_AGENT"));
    }

    @Test
    public void cannotWork() {
        assertThat(new WindowsTaskAgentRestarter().canWork(), is(false));
    }
}
