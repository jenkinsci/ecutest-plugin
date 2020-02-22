/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline;

import groovy.lang.Binding;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;

/**
 * Binds "ATX" keyword as global variable to pipeline executions.
 */
@Extension
public class ATXGlobalVariable extends GlobalVariable {

    @Nonnull
    @Override
    public String getName() {
        return "ATX";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull final CpsScript cpsScript) throws Exception {
        final Binding binding = cpsScript.getBinding();
        final Object atx;
        if (binding.hasVariable(getName())) {
            atx = binding.getVariable(getName());
        } else {
            atx = new ATXPipeline(cpsScript);
            binding.setVariable(getName(), atx);
        }
        return atx;
    }
}
