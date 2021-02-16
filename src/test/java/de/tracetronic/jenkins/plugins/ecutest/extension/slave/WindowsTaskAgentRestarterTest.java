/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.slave;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("deprecation")
public class WindowsTaskAgentRestarterTest {

    @Test
    public void deprecatedTaskName() {
        assertThat(new WindowsTaskSlaveRestarter().getTaskName(), is("RESTART_JENKINS_SLAVE"));
    }

    @Test
    public void cannotWork() {
        assertThat(new WindowsTaskSlaveRestarter().canWork(), is(false));
    }
}
