/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.extension.slave;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
