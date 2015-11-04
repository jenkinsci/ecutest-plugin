/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.report.atx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tracetronic.jenkins.plugins.ecutest.report.AbstractTestReport;

/**
 * Action to show a link to {@link ATXReport}s or {@link ATXZipReport}s at the build page.
 *
 * @param <T>
 *            the report type, either {@link ATXReport} or {@link ATXZipReport}
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXBuildAction<T extends AbstractTestReport> extends AbstractATXAction {

    private final List<T> atxReports = new ArrayList<T>();

    /**
     * Gets the ATX reports.
     *
     * @return the ATX reports
     */
    public List<T> getATXReports() {
        return atxReports;
    }

    /**
     * Adds a ATX report.
     *
     * @param report
     *            the ATX report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final T report) {
        return getATXReports().add(report);
    }

    /**
     * Adds a bundle of ATX reports.
     *
     * @param reports
     *            the collection of ATX reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<T> reports) {
        return getATXReports().addAll(reports);
    }

    /**
     * Returns {@link ATXReport} specified by the URL.
     *
     * @param token
     *            the URL token
     * @return the {@link ATXReport} or null if no proper report exists
     */
    public T getDynamic(final String token) {
        for (final T report : getATXReports()) {
            if (token.equals(report.getId())) {
                return report;
            } else {
                final T potentialReport = traverseSubReports(token, report);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Traverses the sub-reports recursively and searches
     * for the {@link ATXReport} matching the given token id.
     *
     * @param token
     *            the token id
     * @param report
     *            the report
     * @return the {@link ATXReport} or null if no proper report exists
     */
    @SuppressWarnings("unchecked")
    private T traverseSubReports(final String token, final T report) {
        for (final AbstractTestReport subReport : report.getSubReports()) {
            if (token.equals(subReport.getId())) {
                return (T) subReport;
            } else {
                final T potentialReport = traverseSubReports(token, (T) subReport);
                if (potentialReport != null) {
                    return potentialReport;
                }
            }
        }
        return null;
    }

    /**
     * Specifies whether this action holds archived {@link ATXZipReport}s.
     *
     * @return {@code true} if has archived reports, {@code false} otherwise
     */
    public boolean hasArchivedReports() {
        return !getATXReports().isEmpty() && getATXReports().get(0) instanceof ATXZipReport;
    }

    @Override
    public String getDisplayName() {
        return Messages.ATXBuildAction_DisplayName();
    }
}
