/**
 * Copyright (c) 2015-2016 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.log;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

import java.util.Arrays;

/**
 * Annotator which adds color highlighting. There are currently three message categories: error, staring with
 * <i>ERROR:</i> prefix, warning starting with <i>WARN:</i>and info, which starts with <i>INFO:</i> prefix.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TTConsoleNote extends ConsoleNote<Object> {

    private static final long serialVersionUID = 1L;

    @Override
    public ConsoleAnnotator<Object> annotate(final Object context, final MarkupText text, final int charPos) {
        final String plainText = text.getText();
        if (plainText.contains("ERROR:")) {
            text.addMarkup(0, text.length(), "<span style=\"font-weight: bold; color:#FF0000\">", "</span>");
        }
        if (plainText.contains("WARN:")) {
            text.addMarkup(0, text.length(), "<span style=\"color:#ED8B00\">", "</span>");
        }
        if (plainText.contains("INFO:")) {
            text.addMarkup(0, text.length(), "<span style=\"color:#208CA3\">", "</span>");
            addResultMarkup(text);
        }
        return null;
    }

    /**
     * Adds markup for the test result part of the console log.
     *
     * @param text
     *            the text
     */
    private void addResultMarkup(final MarkupText text) {
        final String plainText = text.getText();
        for (final String result : Arrays.asList("NONE", "INCONCLUSIVE", "SUCCESS", "FAILED", "ERROR")) {
            if (plainText.contains(result)) {
                final int startPos = plainText.indexOf(result);
                final int endPos = startPos + result.length();
                String color;
                if ("NONE".equals(result)) {
                    color = "#63666A";
                } else if ("INCONCLUSIVE".equals(result)) {
                    color = "#ED8B00";
                } else if ("SUCCESS".equals(result)) {
                    color = "#A1C057";
                } else if ("FAILED".equals(result)) {
                    color = "#F25757";
                } else if ("ERROR".equals(result)) {
                    color = "#B40000";
                } else {
                    color = "#208CA3";
                }
                text.addMarkup(startPos, endPos, "<span style=\"color:" + color + "\">", "</span>");
            }
        }
    }

    /**
     * DescriptorImpl for {@link TTConsoleNote}.
     */
    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {

        @Override
        public String getDisplayName() {
            return "TraceTronic Console Note";
        }
    }
}
