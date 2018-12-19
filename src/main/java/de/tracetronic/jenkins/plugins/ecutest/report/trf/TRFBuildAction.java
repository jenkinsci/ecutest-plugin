/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.tracetronic.jenkins.plugins.ecutest.report.trf;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action to show a link to {@link TRFReport}s at the build page.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TRFBuildAction extends AbstractTRFAction implements SimpleBuildStep.LastBuildAction {

    private final List<TRFReport> trfReports = new ArrayList<>();

    /**
     * Instantiates a new {@link TRFBuildAction}.
     *
     * @param projectLevel specifies whether archiving is restricted to project level only
     */
    public TRFBuildAction(final boolean projectLevel) {
        super(projectLevel);
    }

    /**
     * Gets the TRF reports.
     *
     * @return the TRF reports
     */
    public List<TRFReport> getTRFReports() {
        return trfReports;
    }

    /**
     * Adds a TRF report.
     *
     * @param report the TRF report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final TRFReport report) {
        return getTRFReports().add(report);
    }

    /**
     * Adds a bundle of TRF reports.
     *
     * @param reports the collection of TRF reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<TRFReport> reports) {
        return getTRFReports().addAll(reports);
    }

    /**
     * Returns {@link TRFReport} specified by the URL.
     *
     * @param token the URL token
     * @return the {@link TRFReport} or {@code null} if no proper report exists
     */
    public AbstractTestReport getDynamic(final String token) {
        for (final TRFReport report : getTRFReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final TRFReport potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link TRFReport} matching the given token id.
     *
     * @param token  the token id
     * @param report the report
     * @return the {@link TRFReport} or {@code null} if no proper report exists
     */
    private TRFReport traverseSubReports(final String token, final TRFReport report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (TRFReport) subReport;
            } else {
                final TRFReport potentialReport = traverseSubReports(token, (TRFReport) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.TRFBuildAction_DisplayName();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new TRFProjectAction(isProjectLevel()));
    }
}
