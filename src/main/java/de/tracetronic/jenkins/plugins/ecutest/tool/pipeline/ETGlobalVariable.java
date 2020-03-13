/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.tool.pipeline;

import groovy.lang.Binding;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;

/**
 * Binds "ET" keyword as global variable to pipeline executions.
 */
@Extension
public class ETGlobalVariable extends GlobalVariable {

    @Nonnull
    @Override
    public String getName() {
        return "ET";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull final CpsScript cpsScript) {
        final Binding binding = cpsScript.getBinding();
        final Object atx;
        if (binding.hasVariable(getName())) {
            atx = binding.getVariable(getName());
        } else {
            atx = new ETPipeline(cpsScript);
            binding.setVariable(getName(), atx);
        }
        return atx;
    }
}
