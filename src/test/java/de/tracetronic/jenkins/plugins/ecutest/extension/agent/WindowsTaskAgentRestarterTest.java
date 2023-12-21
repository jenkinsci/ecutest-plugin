/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.agent;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
