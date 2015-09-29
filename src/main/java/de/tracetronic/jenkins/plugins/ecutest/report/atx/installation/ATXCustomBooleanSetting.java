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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Class holding the information of an additional boolean ATX setting.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ATXCustomBooleanSetting extends ATXCustomSetting {

    private static final long serialVersionUID = 1L;

    private final boolean checked;

    /**
     * Instantiates a new {@link ATXCustomBooleanSetting}.
     *
     * @param name
     *            the name of the setting
     * @param checked
     *            the checkbox status
     */
    @DataBoundConstructor
    public ATXCustomBooleanSetting(final String name, final boolean checked) {
        super(name);
        this.checked = checked;
    }

    /**
     * @return {@code true} if the checkbox is checked, {@code false} otherwise
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * DescriptorImpl for {@link ATXBooleanSetting}.
     */
    @Extension
    public static class DescriptorImpl extends ATXCustomSetting.DescriptorImpl {

        @Override
        public String getDisplayName() {
            return Messages.ATXCustomBooleanSetting_DisplayName();
        }
    }
}
