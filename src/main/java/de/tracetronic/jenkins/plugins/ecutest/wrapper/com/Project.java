/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComProject;
import org.apache.commons.io.FilenameUtils;

/**
 * COM object giving access to the properties of an opened project.
 */
public class Project extends AbstractTestObject implements ComProject {

    /**
     * Instantiates a new {@link Project}.
     *
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public Project(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @Override
    public String getName() throws ETComException {
        return FilenameUtils.getBaseName(performRequest("GetFilename").getString());
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getPackages() throws ETComException {
        return performRequest("GetPackages").getString();
    }
}
