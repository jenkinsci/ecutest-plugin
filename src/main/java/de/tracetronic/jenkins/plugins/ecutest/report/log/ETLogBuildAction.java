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
package de.tracetronic.jenkins.plugins.ecutest.report.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Action to show a link to {@link ETLogReport}s at the build page.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETLogBuildAction extends AbstractETLogAction {

    private final List<ETLogReport> logReports = new ArrayList<ETLogReport>();

    /**
     * @return the log reports
     */
    public List<ETLogReport> getLogReports() {
        return logReports;
    }

    /**
     * Adds a ECU-TEST log report.
     *
     * @param report
     *            the ECU-TEST log report to add
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean add(final ETLogReport report) {
        return getLogReports().add(report);
    }

    /**
     * Adds a bundle of ECU-TEST log reports.
     *
     * @param reports
     *            the collection of ECU-TEST log reports
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean addAll(final Collection<ETLogReport> reports) {
        return getLogReports().addAll(reports);
    }

    /**
     * Returns {@link ETLogReport} specified by the URL.
     *
     * @param token
     *            the URL token
     * @return the {@link ETLogReport} or null if no proper report exists
     */
    public ETLogReport getDynamic(final String token) {
        for (final ETLogReport report : getLogReports()) {
            if (token.equals(report.getId())) {
                return report;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return Messages.ETLogAction_DisplayName();
    }
}
